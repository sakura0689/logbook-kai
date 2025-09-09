package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_material
 *
 */
@Data
public class Material implements Serializable {

    private static final long serialVersionUID = -6919096591550530580L;

    /** api_id */
    private Integer id;

    /** api_value */
    private Integer value;

    /**
     * JsonObjectから{@link Material}を構築します
     *
     * @param json JsonObject
     * @return {@link Material}
     */
    public static Material toMaterial(JsonObject json) {
        Material bean = new Material();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_value", bean::setValue);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            unUsedKey.removeIf("api_member_id"::equals); //提督id
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
