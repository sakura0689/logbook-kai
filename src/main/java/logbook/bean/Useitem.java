package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

@Data
public class Useitem implements Serializable {

    private static final long serialVersionUID = 3756342448962760487L;

    /** api_id */
    private Integer id;

    /** api_count */
    private Integer count;

    /**
     * JsonObjectから{@link Useitem}を構築します
     *
     * @param json JsonObject
     * @return {@link Useitem}
     */
    public static Useitem toUseitem(JsonObject json) {
        Useitem bean = new Useitem();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_count", bean::setCount);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
