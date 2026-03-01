package logbook.ship;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

/**
 * 艦娘の初期ステータス（対潜・回避・索敵）をWikiから取得してJSONを作成するクラスです。
 */
public class ShipsJsonCreateBuilder {

    public static void main(String[] args) {
        try {
            // カレントディレクトリの親（プロジェクトルート想定）を取得
            Path root = Paths.get(".").toAbsolutePath().getParent();
            System.out.println("Root path: " + root);

            Path configDirPath = root.resolve("target/test-classes/logbook/config");

            // 装備マスタの読み込み（名前 -> ID マッピング作成）
            Map<String, Integer> itemNameToId = new java.util.HashMap<>();
            Path slotitemMstPath = configDirPath.resolve("logbook.bean.SlotitemMstCollection.json");
            if (Files.exists(slotitemMstPath)) {
                try (InputStream is = new FileInputStream(slotitemMstPath.toFile());
                        JsonReader reader = Json.createReader(is)) {
                    JsonObject json = reader.readObject();
                    JsonObject itemMap = json.getJsonObject("slotitemMap");
                    if (itemMap != null) {
                        for (String key : itemMap.keySet()) {
                            JsonObject item = itemMap.getJsonObject(key);
                            if (item != null) {
                                String itemName = item.getString("name");
                                int itemId = item.getInt("id");
                                String normalizedMasterName = normalizeItemName(itemName);
                                itemNameToId.put(normalizedMasterName, itemId);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Warning: SlotitemMstCollection.json not found at " + slotitemMstPath);
            }

            Path shipMstPath = configDirPath.resolve("logbook.bean.ShipMstCollection.json");
            if (!Files.exists(shipMstPath)) {
                System.err.println("File not found: " + shipMstPath);
                return;
            }

            Map<Integer, JsonObject> shipMap = new TreeMap<>();
            try (InputStream is = new FileInputStream(shipMstPath.toFile());
                    JsonReader reader = Json.createReader(is)) {
                JsonObject json = reader.readObject();
                JsonObject ships = json.getJsonObject("shipMap");

                for (int i = 1; i < 1500; i++) {
                    JsonObject ship = ships.getJsonObject(Integer.toString(i));
                    if (ship != null) {
                        int id = ship.getInt("id");
                        shipMap.put(id, ship);
                        System.out.println(id + " : " + ship.getString("name"));
                    }
                }
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            int total = shipMap.size();
            int current = 0;

            for (Map.Entry<Integer, JsonObject> entry : shipMap.entrySet()) {
                current++;
                JsonObject ship = entry.getValue();
                String name = ship.getString("name");
                int id = ship.getInt("id");

                boolean fromCache = false;
                try {
                    int remainingSec = (total - current) * 10;
                    System.out.println(String.format("Fetching (%d/%d): %s (ETA: %d min %d sec)",
                            current, total, name, remainingSec / 60, remainingSec % 60));
                    Map<String, Object> status = scrapeWiki(name, id);

                    if (status != null) {
                        fromCache = Boolean.TRUE.equals(status.get("fromCache"));
                        JsonObjectBuilder shipBuilder = Json.createObjectBuilder()
                                .add("id", id)
                                .add("name", name)
                                .add("min_taisen", parseSafe((String) status.get("min_taisen")))
                                .add("min_kaihi", parseSafe((String) status.get("min_kaihi")))
                                .add("min_sakuteki", parseSafe((String) status.get("min_sakuteki")));

                        // 装備情報の追加（名前からIDへ変換）
                        @SuppressWarnings("unchecked")
                        java.util.List<String> items = (java.util.List<String>) status.get("items");
                        JsonArrayBuilder itemArrayBuilder = Json.createArrayBuilder();
                        if (items != null) {
                            for (String itemName : items) {
                                if ("未装備".equals(itemName)) {
                                    continue;
                                }

                                // 装備名の正規化 (全角/半角の揺れを吸収)
                                String normalizedName = normalizeItemName(itemName);

                                Integer itemId = itemNameToId.get(normalizedName);
                                if (itemId != null) {
                                    itemArrayBuilder.add(itemId);
                                    System.out.println("  Found item: " + itemName + " (Normalized: " + normalizedName
                                            + ") -> ID: " + itemId);
                                } else {
                                    System.err.println("  Warning: Item ID not found for " + itemName + " (Normalized: "
                                            + normalizedName + ")");
                                }
                            }
                        }
                        shipBuilder.add("item", itemArrayBuilder);

                        arrayBuilder.add(shipBuilder);
                        System.out.println("Added: " + name + " " + status);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing " + name + ": " + e.getMessage());
                }

                if (!fromCache) {
                    // Wikiへの負荷軽減のため(通信した場合のみ)10秒待機
                    Thread.sleep(10000);
                }
            }

            // JSONファイルとして書き出し
            Path outPath = root.resolve("ships.json");
            JsonWriterFactory factory = Json.createWriterFactory(
                    Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

            try (OutputStream os = Files.newOutputStream(outPath);
                    JsonWriter writer = factory.createWriter(os)) {
                writer.write(arrayBuilder.build());
            }

            System.out.println("JSON output written to: " + outPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> scrapeWiki(String shipName, int shipId) throws Exception {
        File tempDir = new File("./logs/temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File htmlFile = new File(tempDir, shipName + ".html");

        Document doc;
        boolean fromCache = false;

        if (htmlFile.exists()) {
            doc = Jsoup.parse(htmlFile, "UTF-8", "https://wikiwiki.jp/kancolle/");
            fromCache = true;
        } else {
            String urlName = shipName;
            // Glorious改の同名回避 (ID 740:巡洋戦艦, 741:正規空母)
            if ("Glorious改".equals(shipName)) {
                if (shipId == 740) {
                    urlName = "Glorious改(巡洋戦艦)";
                } else if (shipId == 741) {
                    urlName = "Glorious改(正規空母)";
                }
            }
            String url = "https://wikiwiki.jp/kancolle/" + urlName;
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0")
                    .timeout(10000)
                    .get();
            Files.writeString(htmlFile.toPath(), doc.outerHtml(), java.nio.charset.StandardCharsets.UTF_8);
        }

        Element content = doc.selectFirst("#content");
        if (content == null)
            return null;

        Map<String, Object> shipStatus = new java.util.HashMap<>();
        java.util.List<String> items = new java.util.ArrayList<>();

        Elements tables = content.select("table");
        boolean foundStatus = false;
        boolean foundEquip = false;

        for (Element table : tables) {
            String tableText = table.text();
            // ステータスまたは装備が含まれる可能性のあるテーブルを特定
            if (tableText.contains("回避") || tableText.contains("艦船ステータス") || tableText.contains("装備")) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String rowText = row.text().trim();
                    Elements ths = row.select("th");
                    Elements tds = row.select("td");

                    // ステータス抽出 (回避, 対潜, 索敵)
                    // foundEquipがtrueになるまでは全ての行でステータス項目を探し続ける
                    if (!foundEquip) {
                        for (int i = 0; i < ths.size(); i++) {
                            String header = ths.get(i).text().trim();
                            if (i < tds.size()) {
                                String value = tds.get(i).text().trim();
                                if (header.contains("回避")) {
                                    shipStatus.put("min_kaihi", value.split("/")[0].trim());
                                    foundStatus = true;
                                } else if (header.contains("対潜")) {
                                    shipStatus.put("min_taisen", value.split("/")[0].trim());
                                    foundStatus = true;
                                } else if (header.contains("索敵")) {
                                    shipStatus.put("min_sakuteki", value.split("/")[0].trim());
                                    foundStatus = true;
                                }
                            }
                        }
                    }

                    // 装備抽出の開始判定
                    boolean hasEquipmentHeader = false;
                    boolean hasStatusHeader = false;
                    for (Element cell : row.select("th, td")) {
                        String cellText = cell.text().trim();
                        // 「装備」「搭載」「艦載」のいずれかを含むヘッダーを探す
                        if (cellText.equals("装備") || cellText.equals("搭載") || cellText.equals("艦載")) {
                            hasEquipmentHeader = true;
                        }
                        // ステータス項目が含まれている行は装備ヘッダーではないと判断する
                        if (cellText.contains("対潜") || cellText.contains("索敵") || cellText.contains("回避")
                                || cellText.contains("耐久")) {
                            hasStatusHeader = true;
                        }
                    }

                    if (hasEquipmentHeader && !hasStatusHeader && !foundEquip) {
                        // 装備セクションの開始。ただしステータスと同じテーブルであることを期待
                        foundEquip = true;
                        continue;
                    }

                    if (foundEquip) {
                        // 終了判定
                        if (rowText.contains("改造チャート") || rowText.contains("図鑑説明") || rowText.contains("点滅") ||
                                rowText.contains("装備ボーナス") || rowText.contains("性能比較")) {
                            foundEquip = false;
                            continue;
                        }

                        if (!tds.isEmpty()) {
                            Element itemCell = tds.get(tds.size() - 1);
                            String equipStr = itemCell.text().trim();

                            // フィルタリング
                            if (equipStr.isEmpty() || equipStr.equals("-") || equipStr.equals("未装備") ||
                                    equipStr.equals("編集") || equipStr.equals("追加") || equipStr.contains("対空CI") ||
                                    equipStr.matches("^[〇×◯？?△□x＋\\+].*") || equipStr.matches("\\*\\d+") ||
                                    equipStr.contains("変動") || equipStr.equals("なし") || equipStr.equals("装備不可")) {
                                continue;
                            }

                            if (items.size() >= 5) {
                                foundEquip = false;
                                continue;
                            }

                            Element a = itemCell.selectFirst("a");
                            if (a != null) {
                                String aText = a.text().trim();
                                if (!aText.isEmpty() && !aText.equals("未装備") && !aText.equals("編集") &&
                                        !aText.matches("^[〇×◯？?△□x＋\\+].*") && !aText.matches("\\*\\d+")) {
                                    items.add(aText);
                                }
                            } else {
                                items.add(equipStr);
                            }
                        }
                    }
                }
            }
            if (foundStatus && !items.isEmpty())
                break;
        }
        shipStatus.put("items", items);
        shipStatus.put("fromCache", fromCache);
        return shipStatus;
    }

    private static int parseSafe(String val) {
        if (val == null || val.isEmpty())
            return 0;
        try {
            // 数字以外の文字が含まれる場合を考慮して数字のみ抽出するか、単純パース
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Wikiの装備名表記(全角記号など)をマスターデータの表記(半角等)に合わせるための正規化を行います。
     * 
     * @param itemName Wikiから抽出した装備名
     * @return 正規化後の装備名
     */
    private static String normalizeItemName(String itemName) {
        if (itemName == null)
            return null;

        // 1. NFKCで全角英数記号を半角へ変換 (10cm連装高角砲＋高射装置 -> 10cm連装高角砲+高射装置)
        String normalized = Normalizer.normalize(itemName, Normalizer.Form.NFKC);

        // 2. その他のマスターデータ側固有の揺れや、NFKCで変換しきれない・別の文字になるケースの微調整
        // 例: 「14inch／45」 の ／ は全角スラッシュ (U+FF0F) だが、
        // もしNFKCで意図した半角スラッシュにならなかった場合などに備えて明示的に置換
        normalized = normalized.replace("＋", "+");
        normalized = normalized.replace("／", "/");
        normalized = normalized.replace("　", " "); // 全角スペースの置換

        // 3. 完全手動の例外対応（Wiki側とMaster側でスペースの有無などが異なる特定の装備）
        normalized = normalized.replace("5inch単装砲 Mk.30 改", "5inch単装砲 Mk.30改");
        normalized = normalized.replace("SKレーダー", "SK レーダー");
        normalized = normalized.replace("SK+SGレーダー", "SK+SG レーダー");
        normalized = normalized.replace("Bofors15.2cm連装砲 Model1930", "Bofors 15.2cm連装砲 Model 1930");

        return normalized;
    }
}
