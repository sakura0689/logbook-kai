package logbook.bean;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_mst_stype
 *
 */
@Data
public class Stype implements Serializable {

    private static final long serialVersionUID = -8213373063199467493L;

    /** api_id */
    private Integer id;

    /** api_sortno */
    private Integer sortno;

    /** api_name */
    private String name;

    /** api_scnt */
    private Integer scnt;

    /** api_kcnt */
    private Integer kcnt;

    /** api_equip_type */
    private Map<Integer, Integer> equipType;

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * JsonObjectから{@link Stype}を構築します
     *
     * @param json JsonObject
     * @return {@link Stype}
     */
    public static Stype toStype(JsonObject json) {
        // api_equip_type変換関数
        Function<JsonObject, Map<Integer, Integer>> equipTypeFunc = t -> JsonHelper
                .toMap(t, Integer::valueOf, JsonHelper::toInteger);

        Stype bean = new Stype();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_sortno", bean::setSortno)
                .setString("api_name", bean::setName)
                .setInteger("api_scnt", bean::setScnt)
                .setInteger("api_kcnt", bean::setKcnt)
                .set("api_equip_type", bean::setEquipType, equipTypeFunc);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
