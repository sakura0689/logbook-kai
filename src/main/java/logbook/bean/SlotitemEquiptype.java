package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_mst_slotitem_equiptype
 *
 */
@Data
public class SlotitemEquiptype implements Serializable {

    private static final long serialVersionUID = 6987412391631651270L;

    /** api_id */
    private Integer id;

    /** api_name */
    private String name;

    /** api_show_flg */
    private Integer showFlg;

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * JsonObjectから{@link SlotitemEquiptype}を構築します
     *
     * @param json JsonObject
     * @return {@link SlotitemEquiptype}
     */
    public static SlotitemEquiptype toSlotitemEquiptype(JsonObject json) {
        SlotitemEquiptype bean = new SlotitemEquiptype();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setString("api_name", bean::setName)
                .setInteger("api_show_flg", bean::setShowFlg);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
