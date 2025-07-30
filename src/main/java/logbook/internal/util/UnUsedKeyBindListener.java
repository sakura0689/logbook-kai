package logbook.internal.util;

import java.util.HashSet;
import java.util.Set;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * JsonHelper.bind(json)にて、未使用のKey情報を保持するクラスです
 */
public class UnUsedKeyBindListener implements BindListener {

    private final Set<String> usedKeys = new HashSet<>();
    private final JsonObject json;
    
    public UnUsedKeyBindListener(JsonObject json) {
        this.json = json;
    }
    
    @Override
    public void apply(String key, JsonValue val, Object obj) {
        this.usedKeys.add(key);
    }

    /**
     * JsonHelper.bindにて、bindされていないkey情報を返却します
     * @return
     */
    public Set<String> getUnusedKeys() {
        Set<String> unUsedKeys = new HashSet<>(json.keySet());
        unUsedKeys.removeAll(this.usedKeys);
        return unUsedKeys;
    }
}
