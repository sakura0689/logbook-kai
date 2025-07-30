package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 装備
 *
 */
@Data
public class SlotItem implements Serializable {

    private static final long serialVersionUID = -5902864924857205128L;

    /** api_id */
    private Integer id;

    /** api_level */
    private Integer level;

    /** api_alv */
    private Integer alv;

    /** api_locked */
    private Boolean locked;

    /** api_slotitem_id */
    private Integer slotitemId;

    /**
     * JsonObjectから{@link SlotItem}を構築します
     *
     * @param json JsonObject
     * @return {@link SlotItem}
     */
    public static SlotItem toSlotItem(JsonObject json) {
        SlotItem bean = new SlotItem();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_level", bean::setLevel)
                .setInteger("api_alv", bean::setAlv)
                .setBoolean("api_locked", bean::setLocked)
                .setInteger("api_slotitem_id", bean::setSlotitemId);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
