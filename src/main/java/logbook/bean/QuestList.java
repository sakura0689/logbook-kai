package logbook.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 任務
 */
@Data
public class QuestList implements Serializable {

    private static final long serialVersionUID = 7042549795095230008L;

    /** api_count */
    private Integer count;

    /** api_completed_kind */
    private Integer completedKind;

    /** api_page_count */
    private Integer pageCount;

    /** api_disp_page */
    private Integer dispPage;

    /** api_list */
    private List<Quest> list;

    /** api_exec_count */
    private Integer execCount;

    /**
     * api_list[n]
     */
    @Data
    public static class Quest implements Serializable {

        private static final long serialVersionUID = 257861496728265702L;

        /** api_no */
        private Integer no;

        /** api_category */
        private Integer category;

        /** api_type */
        private Integer type;

        /** api_state */
        private Integer state;

        /** api_title */
        private String title;

        /** api_detail */
        private String detail;

        /** api_get_material */
        private List<Integer> getMaterial;

        /** api_bonus_flag */
        private Integer bonusFlag;

        /** api_progress_flag */
        private Integer progressFlag;

        /** api_invalid_flag */
        private Integer invalidFlag;

        /**
         * JsonObjectから{@link Quest}を構築します
         *
         * @param value JsonObject
         * @return {@link Quest}
         */
        public static Quest toQuest(JsonValue value) {
            if (value instanceof JsonObject) {
                Quest bean = new Quest();
                
                UnUsedKeyBindListener unUsedKeyBindListener = null;
                if (LoggerHolder.get().isDebugEnabled()) {
                    unUsedKeyBindListener = new UnUsedKeyBindListener((JsonObject)value);
                }

                JsonHelper.bind((JsonObject) value, unUsedKeyBindListener)
                        .setInteger("api_no", bean::setNo)
                        .setInteger("api_category", bean::setCategory)
                        .setInteger("api_type", bean::setType)
                        .setInteger("api_state", bean::setState)
                        .setString("api_title", bean::setTitle)
                        .setString("api_detail", bean::setDetail)
                        .setIntegerList("api_get_material", bean::setGetMaterial)
                        .setInteger("api_bonus_flag", bean::setBonusFlag)
                        .setInteger("api_progress_flag", bean::setProgressFlag)
                        .setInteger("api_invalid_flag", bean::setInvalidFlag);
                
                if (LoggerHolder.get().isDebugEnabled()) {
                    Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                    unUsedKey.removeIf("api_label_type"::equals); //任務アイコン種別 1=単発,2=デイリー,3=ウィークリー,6=マンスリー api_typeで十分
                    unUsedKey.removeIf("api_voice_id"::equals); //任務達成ボイスid
                    for (String key : unUsedKey) {
                        LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
                    }
                }

                return bean;
            } else {
                return null;
            }
        }
    }

    /**
     * JsonObjectから{@link QuestList}を構築します
     *
     * @param json JsonObject
     * @return {@link QuestList}
     */
    public static QuestList toQuestList(JsonObject json) {
        QuestList bean = new QuestList();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_count", bean::setCount)
                .setInteger("api_completed_kind", bean::setCompletedKind)
                .setInteger("api_page_count", bean::setPageCount)
                .setInteger("api_disp_page", bean::setDispPage)
                .set("api_list", bean::setList, JsonHelper.toList(Quest::toQuest))
                .setInteger("api_exec_count", bean::setExecCount);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
