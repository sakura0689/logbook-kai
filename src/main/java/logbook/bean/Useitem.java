package logbook.bean;

import java.io.Serializable;

import javax.json.JsonObject;

import logbook.internal.util.JsonHelper;
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
        JsonHelper.bind(json)
                .setInteger("api_id", bean::setId)
                .setInteger("api_count", bean::setCount);
        return bean;
    }
}
