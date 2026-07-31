package logbook.internal.gui;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logbook.internal.kancolle.BattleLogs;
import logbook.internal.kancolle.BattleLogs.SimpleBattleLog;
import logbook.internal.kancolle.Mapping;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.DateUtil;

/**
 * ラスダン計算機コントローラー
 *
 */
public class LastDanceCalcController extends WindowController {

    @FXML
    private TextField area;

    @FXML
    private TextField cell;

    @FXML
    private TextField killCount;

    @FXML
    private TextField startTime;

    @FXML
    private TextArea resultText;

    @FXML
    void calculate(ActionEvent event) {
        String areaInput = this.area.getText() != null ? this.area.getText().trim() : "";
        String cellInput = this.cell.getText() != null ? this.cell.getText().trim() : "";
        String killCountStr = this.killCount.getText() != null ? this.killCount.getText().trim() : "";
        String startTimeStr = this.startTime.getText() != null ? this.startTime.getText().trim() : "";

        int count = 0;
        try {
            count = Integer.parseInt(killCountStr);
        } catch (NumberFormatException e) {
            this.resultText.setText("撃破回数には数値を入力してください。");
            return;
        }

        // 入力条件からフィルタを設定
        Predicate<SimpleBattleLog> predicate = getBattleLogFilter(areaInput, cellInput, startTimeStr);
        
        // 海域ドロップ報告書の読み込み
        List<SimpleBattleLog> matchedLogs;
        try {
            matchedLogs = BattleLogs.readSimpleLog(predicate);
        } catch (Exception e) {
            LoggerHolder.get().warn("海戦・ドロップ報告書の読み込み中に例外が発生しました", e);
            this.resultText.setText("海戦・ドロップ報告書の読み込み中に例外が発生しました: " + e.getMessage());
            return;
        }

        if (matchedLogs == null || matchedLogs.isEmpty()) {
            this.resultText.setText("該当するデータが見つかりませんでした。");
            return;
        }

        List<HpPair> hpPairs = new ArrayList<>();
        String bossName = "";
        
        for (SimpleBattleLog simpleLog : matchedLogs) {
            if ("".equals(bossName)) {
                bossName = simpleLog.getEName1();
            }
            String[] parts = simpleLog.getEHp1().replace("\"", "").split("/");
            try {
                HpPair hpPair = new HpPair(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                hpPairs.add(hpPair);
            } catch (Exception e) {
                LoggerHolder.get().warn("敵艦HP1の評価中に例外が発生しました", e);
                LoggerHolder.get().warn(simpleLog.toString());
            }
        }

        if (hpPairs.isEmpty()) {
            this.resultText.setText("敵艦1HPデータが見つかりませんでした。");
            return;
        }

        // 1行目から MAX HP を取得し、撃破HPを計算
        int maxHp = hpPairs.get(0).maxHp;
        int targetHp = maxHp * count;

        StringBuilder sb = new StringBuilder();
        sb.append(bossName).append("\n");
        sb.append("ゲージ撃破HP ").append(targetHp).append("\n");

        int totalScrapeHp = 0;

        for (HpPair pair : hpPairs) {
            int nowHp = pair.nowHp;
            int curMaxHp = pair.maxHp;
            int scrapeHp = Math.min(curMaxHp, curMaxHp - nowHp);

            totalScrapeHp += scrapeHp;
            sb.append("残HP ").append(nowHp).append(" 削りHP ").append(scrapeHp).append("\n");
        }

        int finalRemHp = targetHp - totalScrapeHp;
        sb.append("残HP ").append(finalRemHp);

        this.resultText.setText(sb.toString());
    }

    /**
     * BattleLogのFilter条件を設定
     * 
     * @return
     */
    private Predicate<SimpleBattleLog> getBattleLogFilter(String areaInput, String cellInput, String startTimeStr) {
        ZonedDateTime filterTime = parseZonedDateTime(startTimeStr);

        // 日付・海域・マスによるフィルタ条件
        Predicate<SimpleBattleLog> predicate = log -> {
            // 日時チェック
            if (filterTime != null) {
                if (log.getDate() != null && log.getDate().compareTo(filterTime) < 0) {
                    return false;
                }
            }
            
            // 海域チェック
            if (!areaInput.isEmpty()) {
                String areaShort = log.getAreaShortName();
                String areaName = log.getArea();
                boolean matchArea = (areaShort != null && areaShort.contains(areaInput))
                        || (areaName != null && areaName.contains(areaInput));
                if (!matchArea) {
                    return false;
                }
            }

            // マスチェック
            if (!cellInput.isEmpty()) {
                String logCell = log.getCell();
                if (logCell == null) {
                    return false;
                }
                String cell = Mapping.getCell(areaInput + "-" + logCell);
                boolean matchCell = (cell != null && cell.equalsIgnoreCase(cellInput));
                if (!matchCell) {
                    return false;
                }
            }

            return true;
        };

        return predicate;
    }
    
    private ZonedDateTime parseZonedDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String str = text.trim();
        try {
            TemporalAccessor ta = DateUtil.DATE_FORMAT.parse(str);
            return ZonedDateTime.of(LocalDateTime.from(ta), ZoneId.of("Asia/Tokyo"));
        } catch (DateTimeParseException e) {
            try {
                str = str.replace('/', '-');
                TemporalAccessor ta = DateUtil.DATE_FORMAT.parse(str);
                return ZonedDateTime.of(LocalDateTime.from(ta), ZoneId.of("Asia/Tokyo"));                        
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private class HpPair {
        int nowHp;
        int maxHp;

        HpPair(int nowHp, int maxHp) {
            this.nowHp = nowHp;
            this.maxHp = maxHp;
        }
    }
}