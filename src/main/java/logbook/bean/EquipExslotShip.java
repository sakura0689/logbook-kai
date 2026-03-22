package logbook.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 装備別補強スロット装備追加条件を表します
 *
 */
@Data
public class EquipExslotShip implements Serializable {

    private static final long serialVersionUID = 6352617349132570935L;

    /** api_ship_ids */
    private Set<Integer> shipIds;

    /** api_stypes */
    private Set<Integer> stypes;

    /** api_ctypes */
    private Set<Integer> ctypes;

    /** api_req_level */
    private Integer reqLevel;

    /**
     * JsonObjectから{@link EquipExslotShip}を構築します
     *
     * @param json JsonObject
     * @return {@link EquipExslotShip}
     */
    public static EquipExslotShip toEquipExslotShip(JsonObject json) {
        EquipExslotShip bean = new EquipExslotShip();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .set("api_ship_ids", bean::setShipIds, EquipExslotShip::toSet)
                .set("api_stypes", bean::setStypes, EquipExslotShip::toSet)
                .set("api_ctypes", bean::setCtypes, EquipExslotShip::toSet)
                .setInteger("api_req_level", bean::setReqLevel);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }

    private static Set<Integer> toSet(JsonValue val) {
        if (val instanceof JsonObject) {
            JsonObject obj = (JsonObject) val;
            return obj.keySet().stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
