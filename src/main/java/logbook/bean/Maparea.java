package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_mst_maparea
 *
 */
@Data
public class Maparea implements Serializable {

    private static final long serialVersionUID = 3065470938786532030L;

    /** api_id */
    private Integer id;

    /** api_name */
    private String name;

    /** api_type */
    private Integer type;

    /**
     * JsonObjectから{@link Maparea}を構築します
     *
     * @param json JsonObject
     * @return {@link Maparea}
     */
    public static Maparea toMaparea(JsonObject json) {
        Maparea bean = new Maparea();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setString("api_name", bean::setName)
                .setInteger("api_type", bean::setType);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
