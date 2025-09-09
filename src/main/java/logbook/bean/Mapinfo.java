package logbook.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import logbook.internal.Config;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * /kcsapi/api_get_member/mapinfo
 *
 */
@Data
public class Mapinfo implements Serializable {

    private static final long serialVersionUID = -4796721957493046860L;

    /** api_map_info */
    private List<MapInfo> mapInfo = new ArrayList<>();

    /** api_air_base */
    private List<AirBase> airBase = new ArrayList<>();

    /** 最終更新時刻 */
    private long lastModified;

    /**
     * api_map_info
     */
    @Data
    public static class MapInfo implements Serializable {

        private static final long serialVersionUID = 7388484486155838098L;

        /** api_id */
        private Integer id;

        /** api_cleared */
        private Integer cleared;

        /** api_exboss_flag */
        private Integer exbossFlag;

        /** api_defeat_count */
        private Integer defeatCount;

        /** api_air_base_decks */
        private Integer airBaseDecks;

        /**
         * JsonObjectから{@link MapInfo}を構築します
         *
         * @param json JsonObject
         * @return {@link MapInfo}
         */
        public static MapInfo toMapInfo(JsonValue json) {
            MapInfo bean = new MapInfo();

            UnUsedKeyBindListener unUsedKeyBindListener = null;
            if (LoggerHolder.get().isDebugEnabled()) {
                unUsedKeyBindListener = new UnUsedKeyBindListener((JsonObject)json);
            }
            
            JsonHelper.bind((JsonObject) json, unUsedKeyBindListener)
                    .setInteger("api_id", bean::setId)
                    .setInteger("api_cleared", bean::setCleared)
                    .setInteger("api_exboss_flag", bean::setExbossFlag)
                    .setInteger("api_defeat_count", bean::setDefeatCount)
                    .setInteger("api_air_base_decks", bean::setAirBaseDecks);

            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                // ゲージ関係の機能は現状作成予定がないため意図して除外する
                unUsedKey.removeIf("api_gauge_num"::equals); //ゲージ本数 1,2,3..
                unUsedKey.removeIf("api_gauge_type"::equals); //1=撃破ゲージ 2=HPゲージ 3=揚陸(TP)ゲージ
                for (String key : unUsedKey) {
                    LoggerHolder.get().debug("未使用のKeyを検出 : " + key + ":" + ((JsonObject)json).get(key));
                }
            }

            return bean;
        }
    }

    /**
     * api_air_base
     */
    @Data
    public static class AirBase implements Serializable {

        private static final long serialVersionUID = 2290504744869571586L;

        /** api_area_id */
        private Integer areaId;

        /** api_rid */
        private Integer rid;

        /** api_name */
        private String name;

        /** api_distance */
        private Distance distance;

        /** api_action_kind */
        private Integer actionKind;

        /** api_plane_info */
        private List<PlaneInfo> planeInfo;

        /**
         * JsonObjectから{@link AirBase}を構築します
         *
         * @param json JsonObject
         * @return {@link AirBase}
         */
        public static AirBase toAirBase(JsonValue json) {
            AirBase bean = new AirBase();
            
            UnUsedKeyBindListener unUsedKeyBindListener = null;
            if (LoggerHolder.get().isDebugEnabled()) {
                unUsedKeyBindListener = new UnUsedKeyBindListener((JsonObject)json);
            }
            JsonHelper.bind((JsonObject)json, unUsedKeyBindListener)
                    .setInteger("api_area_id", bean::setAreaId)
                    .setInteger("api_rid", bean::setRid)
                    .setString("api_name", bean::setName)
                    .set("api_distance", bean::setDistance, Distance::toDistance)
                    .setInteger("api_action_kind", bean::setActionKind)
                    .set("api_plane_info", bean::setPlaneInfo, JsonHelper.toList(PlaneInfo::toPlaneInfo));

            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                for (String key : unUsedKey) {
                    LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
                }
            }
            return bean;
        }
    }

    /**
     * api_distance
     */
    @Data
    public static class Distance implements Serializable {

        private static final long serialVersionUID = -6938404167175752929L;

        /** api_base */
        private Integer base;

        /** api_bonus */
        private Integer bonus;

        /**
         * JsonObjectから{@link Distance}を構築します
         *
         * @param json JsonObject
         * @return {@link Distance}
         */
        public static Distance toDistance(JsonValue json) {
            Distance bean = new Distance();

            UnUsedKeyBindListener unUsedKeyBindListener = null;
            if (LoggerHolder.get().isDebugEnabled()) {
                unUsedKeyBindListener = new UnUsedKeyBindListener((JsonObject)json);
            }

            JsonHelper.bind((JsonObject) json, unUsedKeyBindListener)
                    .setInteger("api_base", bean::setBase)
                    .setInteger("api_bonus", bean::setBonus);
            
            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                for (String key : unUsedKey) {
                    LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
                }
            }

            return bean;
        }
    }

    /**
     * api_plane_info
     */
    @Data
    public static class PlaneInfo implements Serializable {

        private static final long serialVersionUID = 2943711012497557057L;

        /** api_squadron_id */
        private Integer squadronId;

        /** api_state */
        private Integer state;

        /** api_slotid */
        private Integer slotid;

        /** api_count */
        private Integer count;

        /** api_max_count */
        private Integer maxCount;

        /** api_cond */
        private Integer cond;

        /**
         * JsonObjectから{@link PlaneInfo}を構築します
         *
         * @param json JsonObject
         * @return {@link PlaneInfo}
         */
        public static PlaneInfo toPlaneInfo(JsonValue json) {
            PlaneInfo bean = new PlaneInfo();

            UnUsedKeyBindListener unUsedKeyBindListener = null;
            if (LoggerHolder.get().isDebugEnabled()) {
                unUsedKeyBindListener = new UnUsedKeyBindListener((JsonObject)json);
            }

            JsonHelper.bind((JsonObject) json, unUsedKeyBindListener)
                    .setInteger("api_squadron_id", bean::setSquadronId)
                    .setInteger("api_state", bean::setState)
                    .setInteger("api_slotid", bean::setSlotid)
                    .setInteger("api_count", bean::setCount)
                    .setInteger("api_max_count", bean::setMaxCount)
                    .setInteger("api_cond", bean::setCond);

            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
                for (String key : unUsedKey) {
                    LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
                }
            }

            return bean;
        }
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから<code>Mapinfo</code>を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(Mapinfo.class, Mapinfo::new)</code>
     * </blockquote>
     *
     * @return <code>Mapinfo</code>
     */
    public static Mapinfo get() {
        return Config.getDefault().get(Mapinfo.class, Mapinfo::new);
    }
}
