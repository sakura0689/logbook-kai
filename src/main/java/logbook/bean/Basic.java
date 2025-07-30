package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.Config;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_basic
 *
 */
@Data
public class Basic implements Serializable {

    private static final long serialVersionUID = -2392950337873034663L;

    /** api_comment */
    private String comment = "";

    /** api_count_deck */
    private Integer countDeck = 0;

    /** api_count_kdock */
    private Integer countKdock = 0;

    /** api_count_ndock */
    private Integer countNdock = 0;

    /** api_experience */
    private Integer experience = 0;

    /** api_fcoin */
    private Integer fcoin = 0;

    /** api_large_dock */
    private Boolean largeDock = Boolean.FALSE;

    /** api_level */
    private Integer level = 0;

    /** api_rank */
    private Integer rank = 0;

    /** api_max_chara */
    private Integer maxChara = 0;

    /** api_max_slotitem */
    private Integer maxSlotitem = 0;

    /** api_medals */
    private Integer medals = 0;

    /** api_nickname */
    private String nickname = "";

    /**
     * JsonObjectから{@link Basic}を構築します
     *
     * @param bean Basic
     * @param json JsonObject
     * @return {@link Basic}
     */
    public static Basic updateBasic(Basic bean, JsonObject json) {

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setString("api_comment", bean::setComment)
                .setInteger("api_count_deck", bean::setCountDeck)
                .setInteger("api_count_kdock", bean::setCountKdock)
                .setInteger("api_count_ndock", bean::setCountNdock)
                .setInteger("api_experience", bean::setExperience)
                .setInteger("api_fcoin", bean::setFcoin)
                .setBoolean("api_large_dock", bean::setLargeDock)
                .setInteger("api_level", bean::setLevel)
                .setInteger("api_rank", bean::setRank)
                .setInteger("api_max_chara", bean::setMaxChara)
                .setInteger("api_max_slotitem", bean::setMaxSlotitem)
                .setInteger("api_medals", bean::setMedals)
                .setString("api_nickname", bean::setNickname);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link Basic}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(Basic.class, Basic::new)</code>
     * </blockquote>
     *
     * @return {@link Basic}
     */
    public static Basic get() {
        return Config.getDefault().get(Basic.class, Basic::new);
    }
}
