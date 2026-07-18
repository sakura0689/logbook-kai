package logbook.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import logbook.internal.Config;
import logbook.internal.kancolle.BattleLogs;
import logbook.internal.kancolle.BattleLogs.SimpleBattleLog;
import logbook.internal.kancolle.MissionLogs;
import logbook.internal.kancolle.MissionLogs.SimpleMissionLog;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.DateUtil;
import lombok.Data;
import lombok.val;

/**
 * 任務の受託期間を管理します。
 *
 */
@Data
public class AppQuestDuration {

    /** 期限をキーにするマップ */
    private ConcurrentHashMap<String, Map<Integer, List<Duration>>> map = new ConcurrentHashMap<>();

    /**
     * 受託します。
     * 
     * @param quest 任務
     */
    @JsonIgnore
    public void set(AppQuest quest) {
        if (quest.getExpire() == null) {
            return;
        }
        val durationMap = this.map.computeIfAbsent(quest.getExpire(), k -> new ConcurrentHashMap<>());
        val durations = durationMap.computeIfAbsent(quest.getNo(), k -> new ArrayList<>());
        synchronized (durations) {
            for (Duration duration : durations) {
                if (duration.getTo() == null) {
                    return;
                }
            }
            Duration duration = new Duration();
            duration.setFrom(DateUtil.nowString());
            durations.add(duration);
        }
        // 重複があれば削除
        for (val entry : this.map.entrySet()) {
            if (!entry.getKey().equals(quest.getExpire())) {
                entry.getValue().remove(quest.getNo());
            }
        }
        // 期限切れの削除
        String now = DateUtil.nowString();
        val iterator = this.map.entrySet().iterator();
        for (; iterator.hasNext();) {
            val entry = iterator.next();
            if (now.compareTo(entry.getKey()) > 0) {
                iterator.remove();
            }
        }
    }

    /**
     * 受託を停止します。
     * 
     * @param questId 任務ID
     */
    @JsonIgnore
    public void unset(Integer questId) {
        for (val entry : this.map.entrySet()) {
            val durations = entry.getValue().get(questId);
            if (durations != null) {
                synchronized (durations) {
                    for (Duration duration : durations) {
                        if (duration.getTo() == null) {
                            duration.setTo(DateUtil.nowString());
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 受託を完了します。
     * 
     * @param questId
     */
    @JsonIgnore
    public void remove(Integer questId) {
        for (val entry : this.map.entrySet()) {
            entry.getValue().remove(questId);
        }
    }

    @JsonIgnore
    public Optional<List<SimpleBattleLog>> getCondition(AppQuest quest) {
        int no = quest.getNo();
        if (no >= 10001 && no <= 10005) {
            String from = getCustomQuestFromDate(quest);
            return Optional.of(BattleLogs.readSimpleLog(log -> {
                String date = log.getDateString();
                return date.compareTo(from) >= 0;
            }));
        }

        val durationMap = this.map.get(quest.getExpire());
        if (durationMap == null)
            return Optional.empty();

        val durations = durationMap.get(quest.getNo());
        if (durations == null)
            return Optional.empty();

        return Optional.of(BattleLogs.readSimpleLog(log -> {
            String date = log.getDateString();
            for (Duration duration : durations) {
                String from = duration.getFrom();
                String to = duration.getTo();
                if (date.compareTo(from) >= 0 && (to == null || date.compareTo(to) <= 0)) {
                    return true;
                }
            }
            return false;
        }));
    }

    @JsonIgnore
    public Optional<List<SimpleMissionLog>> getMissionCondition(AppQuest quest) {
        int no = quest.getNo();
        if (no >= 10001 && no <= 10005) {
            String from = getCustomQuestFromDate(quest);
            return Optional.of(MissionLogs.readSimpleLog(log -> {
                String date = log.getDateString();
                return date.compareTo(from) >= 0;
            }));
        }

        val durationMap = this.map.get(quest.getExpire());
        if (durationMap == null)
            return Optional.empty();

        val durations = durationMap.get(quest.getNo());
        if (durations == null)
            return Optional.empty();

        return Optional.of(MissionLogs.readSimpleLog(log -> {
            String date = log.getDateString();
            for (Duration duration : durations) {
                String from = duration.getFrom();
                String to = duration.getTo();
                if (date.compareTo(from) >= 0 && (to == null || date.compareTo(to) <= 0)) {
                    return true;
                }
            }
            return false;
        }));
    }

    /**
     * 受託期間
     *
     */
    @Data
    public static class Duration {
        private String from;
        private String to;
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから<code>AppQuestCondition</code>を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(AppQuestCondition.class, AppQuestCondition::new)</code>
     * </blockquote>
     *
     * @return <code>AppQuestCondition</code>
     */
    public static AppQuestDuration get() {
        return Config.getDefault().get(AppQuestDuration.class, AppQuestDuration::new);
    }

    private String getCustomQuestFromDate(AppQuest quest) {
        // デフォルトは単発（全期間、2000-01-01）
        String from = "2000-01-01 05:00:00";
        try {
            java.nio.file.Path file = java.nio.file.Paths.get("./customquest/" + quest.getNo() + ".json");
            if (java.nio.file.Files.exists(file)) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
                logbook.bean.AppQuestCondition condition = mapper.readValue(file.toFile(), logbook.bean.AppQuestCondition.class);
                if (condition != null && condition.getStartDate() != null) {
                    from = condition.getStartDate();
                }
            }
        } catch (Exception e) {
            LoggerHolder.get().warn("カスタム任務設定ファイルの開始日時取得に失敗しました", e);
        }
        return from;
    }
}
