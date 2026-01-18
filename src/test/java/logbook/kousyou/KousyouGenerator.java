package logbook.kousyou;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class KousyouGenerator {

    public static void main(String[] args) throws IOException {
        String url = "https://wikiwiki.jp/kancolle/%E6%94%B9%E4%BF%AE%E8%A1%A8";
        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").maxBodySize(0).get();

        List<Map<String, Object>> result = new ArrayList<>();

        Elements tables = doc.select("table");

        for (Element table : tables) {
            // Check headers
            Elements headers = table.select("thead tr th");
            if (headers.isEmpty()) {
                Element firstRow = table.select("tbody tr").first();
                if (firstRow != null) {
                    headers = firstRow.select("th");
                }
            }

            String headerText = headers.text();
            // Validate headers
            boolean valid = headerText.contains("改修する装備")
                    && headerText.contains("曜日")
                    && headerText.contains("二番艦");

            if (!valid) {
                continue;
            }

            parseTable(table, result);
        }

        // Post-process to add aggregated days
        List<String> dayOrder = List.of("日", "月", "火", "水", "木", "金", "土");
        for (Map<String, Object> item : result) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> improvements = (List<Map<String, Object>>) item.get("Improvement");
            if (improvements != null) {
                List<String> allDays = improvements.stream()
                        .flatMap(imp -> ((List<String>) imp.get("DayOfWeek")).stream())
                        .distinct()
                        .sorted((d1, d2) -> Integer.compare(dayOrder.indexOf(d1), dayOrder.indexOf(d2)))
                        .collect(Collectors.toList());
                item.put("AllAvailableDays", allDays);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File file = new File("src/main/resources/logbook/kousyou/kousyou.json");
        // Ensure parent directory exists
        file.getParentFile().mkdirs();

        mapper.writeValue(file, result);
        System.out.println("JSON output written to: " + file.getAbsolutePath());

        // Verify against Master Data
        verifyAgainstMaster(result);
    }

    private static void verifyAgainstMaster(List<Map<String, Object>> result) throws IOException {
        File masterFile = new File("src/test/resources/logbook/config/logbook.bean.SlotitemMstCollection.json");
        ObjectMapper mapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> root = mapper.readValue(masterFile, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> slotitemMap = (Map<String, Map<String, Object>>) root.get("slotitemMap");

        List<String> masterNames = slotitemMap.values().stream()
                .map(m -> (String) m.get("name"))
                .collect(Collectors.toList());

        List<String> missing = new ArrayList<>();
        for (Map<String, Object> item : result) {
            String name = (String) item.get("SlotItem");
            if (!masterNames.contains(name)) {
                missing.add(name);
            }
        }

        if (missing.isEmpty()) {
            System.out.println("Verification Successful: All SlotItems found in Master Data.");
        } else {
            System.err.println("Verification Failed: The following SlotItems were NOT found in Master Data:");
            for (String s : missing) {
                System.err.println(" - " + s);
            }
        }
    }

    private static void parseTable(Element table, List<Map<String, Object>> result) {
        Elements rows = table.select("tbody tr");
        String currentSlotItem = null;

        for (Element row : rows) {
            Elements cells = row.children();
            if (cells.isEmpty())
                continue;
            // Skip header rows inside tbody
            if (cells.get(0).tagName().equals("th"))
                continue;

            List<String> rowText = cells.stream().map(Element::text).collect(Collectors.toList());

            // Identify Day Column Start Index
            // We look for a sequence of 7 columns that resemble day markers.
            // Critical check: The column IMMEDIATELY FOLLOWING the 7 days should be the
            // Ship Name.
            // Ship Name should NOT look like a day marker (e.g. not "◯", "×").

            int dayStartIndex = -1;
            int bestValidMarkers = -1;

            // Allow searching a bit deeper but usually it's early in the row
            for (int k = 0; k <= rowText.size() - 7; k++) {
                List<String> sub = rowText.subList(k, k + 7);

                // 1. Must VALIDATE strictly as day columns (mostly markers or empty, but must
                // have some markers)
                if (isDayColumn(sub)) {
                    // 2. Check the column after
                    String nextCell = (k + 7 < rowText.size()) ? normalize(rowText.get(k + 7)) : "";

                    // If next cell is a marker, we probably allocated the window too early (e.g.
                    // started at an empty cell before real days)
                    if (!nextCell.isEmpty() && isMarker(nextCell)) {
                        continue;
                    }

                    // Found a candidate. Prefer the one with MORE explicit validated markers if
                    // multiple match?
                    // Actually, if we rely on "next cell is NOT marker", that's usually sufficient
                    // to disambiguate.
                    // But just in case, we track best match.
                    int validCount = countMarkers(sub);
                    if (validCount > bestValidMarkers) {
                        bestValidMarkers = validCount;
                        dayStartIndex = k;
                    }
                }
            }

            if (dayStartIndex != -1) {
                // Determine SlotItem name
                // If the row has enough columns before days, Index 0 is likely SlotItem.
                // Standard: [SlotItem, Stage, Resources..., Days...]
                // BUT, if rowspan is active, SlotItem might be missing.

                if (dayStartIndex > 0) {
                    String candidate = normalize(rowText.get(0));
                    if (!candidate.isEmpty() && !isImprovementStage(candidate) && !isResourceCell(candidate)) {
                        currentSlotItem = candidate;
                    }
                }

                if (currentSlotItem == null)
                    continue;

                String shipName = "";
                if (dayStartIndex + 7 < rowText.size()) {
                    shipName = rowText.get(dayStartIndex + 7);
                }

                if (isInvalidShipName(shipName))
                    continue;

                List<String> validDays = new ArrayList<>();
                String[] dayNames = { "日", "月", "火", "水", "木", "金", "土" };
                for (int d = 0; d < 7; d++) {
                    String marker = rowText.get(dayStartIndex + d);
                    if (isValidAvailability(marker)) {
                        validDays.add(dayNames[d]);
                    }
                }

                if (validDays.isEmpty())
                    continue;

                addToResult(result, currentSlotItem, shipName, validDays);
            }
        }
    }

    private static boolean isDayColumn(List<String> cells) {
        int validMarkers = 0;
        for (String c : cells) {
            String s = normalize(c);
            if (s.isEmpty())
                continue; // Empty is allowed (rowspan effect or just empty)
            if (isMarker(s)) {
                validMarkers++;
            } else {
                return false; // Found junk -> not a day column block
            }
        }
        return validMarkers > 0;
    }

    private static int countMarkers(List<String> cells) {
        int count = 0;
        for (String c : cells) {
            if (isMarker(normalize(c)))
                count++;
        }
        return count;
    }

    private static String normalize(String s) {
        String n = s.replace('\u00A0', ' ')
                .replace("＋", "+")
                .replace("／", "/")
                .replace("＆", "&")
                .trim();

        // Manual fixes
        n = n.replace("5inch単装砲 Mk.30 改", "5inch単装砲 Mk.30改");
        n = n.replace("Bofors15.2cm連装砲 Model1930", "Bofors 15.2cm連装砲 Model 1930");
        n = n.replace("SKレーダー", "SK レーダー");
        n = n.replace("SK+SGレーダー", "SK+SG レーダー");

        return n;
    }

    private static boolean isMarker(String s) {
        // Includes X for validation purposes (to identify the block)
        return s.matches("[◎○◯△×✓xX]|(OK)|(NG)");
    }

    private static boolean isValidAvailability(String s) {
        // Only positive availability markers
        return normalize(s).matches("[◎○◯△✓]|(OK)");
    }

    private static boolean isImprovementStage(String s) {
        // Detects row 0 values that are NOT SlotItems
        return s.startsWith("★") || s.startsWith("初期") || s.startsWith("改修");
    }

    private static boolean isResourceCell(String s) {
        // Detects "燃:120..." etc
        return s.startsWith("燃") || s.contains("/");
    }

    private static boolean isInvalidShipName(String s) {
        String n = normalize(s);
        return n.isEmpty() || n.equals("×") || n.equals("-") || n.equals("⇒");
    }

    private static void addToResult(List<Map<String, Object>> result, String slotItem, String shipName,
            List<String> days) {
        shipName = normalize(shipName);

        // Find existing entry for SlotItem
        Map<String, Object> entry = null;
        for (Map<String, Object> m : result) {
            if (slotItem.equals(m.get("SlotItem"))) {
                entry = m;
                break;
            }
        }

        if (entry == null) {
            entry = new LinkedHashMap<>();
            entry.put("SlotItem", slotItem);
            entry.put("Improvement", new ArrayList<Map<String, Object>>());
            result.add(entry);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) entry.get("Improvement");

        Map<String, Object> var = new LinkedHashMap<>();
        var.put("SupportShip", shipName);
        var.put("DayOfWeek", days);
        list.add(var);
    }
}
