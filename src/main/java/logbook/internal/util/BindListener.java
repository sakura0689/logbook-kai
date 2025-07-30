package logbook.internal.util;

import jakarta.json.JsonValue;

/**
 * {@link Bind}によって設定される値を監視するためのリスナー
 */
public interface BindListener {

    /**
     * 設定される値を監視します
     *
     * @param key JsonObjectのキー
     * @param val JsonObjectの値
     * @param obj converterより返された値
     */
    void apply(String key, JsonValue val, Object obj);
}