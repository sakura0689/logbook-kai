package logbook.internal.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * JsonObjectから別のオブジェクトへの単方向バインディングを提供します。<br>
 *
 */
public class Bind {

    private JsonObject json;

    private BindListener listener;

    /**
     * コンストラクター
     *
     * @param json JsonObject
     */
    Bind(JsonObject json) {
        this.json = json;
    }

    /**
     * コンストラクター
     *
     * @param json JsonObject
     * @param listener BindListener
     */
    Bind(JsonObject json, BindListener listener) {
        this.json = json;
        this.listener = listener;
    }

    /**
     * keyで取得したJsonValueをconverterで変換したものをconsumerへ設定します<br>
     *
     * @param <T> JsonObject#get(Object) の戻り値の型
     * @param <R> converterの戻り値の型
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @param converter JsonValueを変換するFunction
     * @return {@link Bind}
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonValue, R> Bind set(String key, Consumer<R> consumer, Function<T, R> converter) {
        JsonValue val = this.json.get(key);
        if (val != null && JsonValue.NULL != val) {
            R obj = converter.apply((T) val);
            consumer.accept(obj);
            if (this.listener != null) {
                this.listener.apply(key, val, obj);
            }
        }
        return this;
    }

    /**
     * keyで取得したJsonValueをList<Integer>に変換したものをconsumerへ設定します<br>
     *
     * @param <T> JsonObject#get(Object) の戻り値の型
     * @param key JsonObjectから取得するキー
     * @param consumer List<Integer>を消費するConsumer
     * @return {@link Bind}
     */
    public <T extends JsonArray> Bind setIntegerList(String key, Consumer<List<Integer>> consumer) {
        return this.set(key, consumer, JsonHelper::toIntegerList);
    }

    /**
     * keyで取得したJsonValueをList<Long>に変換したものをconsumerへ設定します<br>
     *
     * @param <T> JsonObject#get(Object) の戻り値の型
     * @param key JsonObjectから取得するキー
     * @param consumer List<Long>を消費するConsumer
     * @return {@link Bind}
     */
    public <T extends JsonArray> Bind setLongList(String key, Consumer<List<Long>> consumer) {
        return this.set(key, consumer, JsonHelper::toLongList);
    }

    /**
     * keyで取得したJsonValueをList<Double>に変換したものをconsumerへ設定します<br>
     *
     * @param <T> JsonObject#get(Object) の戻り値の型
     * @param key JsonObjectから取得するキー
     * @param consumer List<Double>を消費するConsumer
     * @return {@link Bind}
     */
    public <T extends JsonArray> Bind setDoubleList(String key, Consumer<List<Double>> consumer) {
        return this.set(key, consumer, JsonHelper::toDoubleList);
    }

    /**
     * keyで取得したJsonValueをList<String>に変換したものをconsumerへ設定します<br>
     *
     * @param <T> JsonObject#get(Object) の戻り値の型
     * @param key JsonObjectから取得するキー
     * @param consumer List<Integer>を消費するConsumer
     * @return {@link Bind}
     */
    public <T extends JsonArray> Bind setStringList(String key, Consumer<List<String>> consumer) {
        return this.set(key, consumer, JsonHelper::toStringList);
    }

    /**
     * keyで取得したJsonValueをStringに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setString(String key, Consumer<String> consumer) {
        return this.set(key, consumer, JsonHelper::toString);
    }

    /**
     * keyで取得したJsonValueをIntegerに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setInteger(String key, Consumer<Integer> consumer) {
        return this.set(key, consumer, JsonHelper::toInteger);
    }

    /**
     * keyで取得したJsonValueをLongに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setLong(String key, Consumer<Long> consumer) {
        return this.set(key, consumer, JsonHelper::toLong);
    }

    /**
     * keyで取得したJsonValueをDoubleに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setDouble(String key, Consumer<Double> consumer) {
        return this.set(key, consumer, JsonHelper::toDouble);
    }

    /**
     * keyで取得したJsonValueをBigDecimalに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setBigDecimal(String key, Consumer<BigDecimal> consumer) {
        return this.set(key, consumer, JsonHelper::toBigDecimal);
    }

    /**
     * keyで取得したJsonValueをBooleanに変換しconsumerへ設定します<br>
     *
     * @param key JsonObjectから取得するキー
     * @param consumer converterの戻り値を消費するConsumer
     * @return {@link Bind}
     */
    public Bind setBoolean(String key, Consumer<Boolean> consumer) {
        return this.set(key, consumer, JsonHelper::toBoolean);
    }
}