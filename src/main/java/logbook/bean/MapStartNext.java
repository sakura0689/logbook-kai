package logbook.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 出撃/進撃
 *
 */
@Data
public class MapStartNext implements Serializable {

    private static final long serialVersionUID = -4272803839336790705L;

    /** api_rashin_flg */
    private Integer rashinFlg;

    /** api_rashin_id */
    private Integer rashinId;

    /** api_maparea_id */
    private Integer mapareaId;

    /** api_mapinfo_no */
    private Integer mapinfoNo;

    /** api_no */
    private Integer no;

    /** api_color_no */
    private Integer colorNo;

    /** api_event_id */
    private Integer eventId;

    /** api_event_kind */
    private Integer eventKind;

    /** api_next */
    private Integer next;

    /** api_bosscell_no */
    private Integer bosscellNo;

    /** api_bosscomp */
    private Integer bosscomp;

    /** api_eventmap */
    private MapTypes.Eventmap eventmap;

    /** api_comment_kind */
    private Integer commentKind;

    /** api_production_kind */
    private Integer productionKind;

    /** api_enemy */
    private MapTypes.Enemy enemy;

    /** api_happening */
    private MapTypes.Happening happening;

    /** api_itemget */
    private List<MapTypes.Itemget> itemget;

    /** api_select_route */
    private MapTypes.SelectRoute selectRoute;

    /** api_from_no */
    private Integer fromNo;

    /** api_destruction_battle */
    private DestructionBattle destructionBattle;

    /** api_m1 */
    private Integer m1;

    /** api_m2 */
    private Integer m2;

    /**
     * ギミック1が達成されたかを返します
     * @return
     */
    @JsonIgnore
    public boolean achievementGimmick1() {
        return (this.m1 != null && this.m1 > 0)
                || (this.destructionBattle != null
                        && this.destructionBattle.m1 != null
                        && this.destructionBattle.m1 > 0);
    }

    /**
     * ギミック2が達成されたかを返します
     * @return
     */
    @JsonIgnore
    public boolean achievementGimmick2() {
        return (this.m2 != null && this.m2 > 0)
                || (this.destructionBattle != null
                        && this.destructionBattle.m2 != null
                        && this.destructionBattle.m2 > 0);
    }

    /**
     * JsonObjectから{@link MapStartNext}を構築します
     *
     * @param json JsonObject
     * @return {@link MapStartNext}
     */
    public static MapStartNext toMapStartNext(JsonObject json) {
        MapStartNext bean = new MapStartNext();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_rashin_flg", bean::setRashinFlg)
                .setInteger("api_rashin_id", bean::setRashinId)
                .setInteger("api_maparea_id", bean::setMapareaId)
                .setInteger("api_mapinfo_no", bean::setMapinfoNo)
                .setInteger("api_no", bean::setNo)
                .setInteger("api_color_no", bean::setColorNo)
                .setInteger("api_event_id", bean::setEventId)
                .setInteger("api_event_kind", bean::setEventKind)
                .setInteger("api_next", bean::setNext)
                .setInteger("api_bosscell_no", bean::setBosscellNo)
                .setInteger("api_bosscomp", bean::setBosscomp)
                .set("api_eventmap", bean::setEventmap, MapTypes.Eventmap::toEventmap)
                .setInteger("api_comment_kind", bean::setCommentKind)
                .setInteger("api_production_kind", bean::setProductionKind)
                .set("api_enemy", bean::setEnemy, MapTypes.Enemy::toEnemy)
                .set("api_happening", bean::setHappening, MapTypes.Happening::toHappening)
                .set("api_itemget", bean::setItemget, JsonHelper.toList(MapTypes.Itemget::toItemget))
                .set("api_select_route", bean::setSelectRoute, MapTypes.SelectRoute::toSelectRoute)
                .setInteger("api_from_no", bean::setFromNo)
                .set("api_destruction_battle", bean::setDestructionBattle, DestructionBattle::toDestructionBattle)
                .setInteger("api_m1", bean::setM1)
                .setInteger("api_m2", bean::setM2);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            unUsedKey.removeIf("api_e_deck_info"::equals); //次マスの敵情報
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key + ":" + json.get(key));
            }
        }

        return bean;
    }

    /**
     * api_destruction_battle
     *
     */
    @Data
    public static class DestructionBattle implements Serializable {

        private static final long serialVersionUID = -6111573497713359784L;

        /** api_lost_kind */
        private Integer lostKind;

        /** api_m1 */
        private Integer m1;

        /** api_m2 */
        private Integer m2;

        public static DestructionBattle toDestructionBattle(JsonObject json) {
            DestructionBattle bean = new DestructionBattle();

            UnUsedKeyBindListener unUsedKeyBindListener = null;
            if (LoggerHolder.get().isDebugEnabled()) {
                unUsedKeyBindListener = new UnUsedKeyBindListener(json);
            }

            JsonHelper.bind(json, unUsedKeyBindListener)
                    .setInteger("api_lost_kind", bean::setLostKind)
                    .setInteger("api_m1", bean::setM1)
                    .setInteger("api_m2", bean::setM2);

            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                for (String key : unUsedKey) {
                    LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
                }
            }

            return bean;
        }
    }
}
