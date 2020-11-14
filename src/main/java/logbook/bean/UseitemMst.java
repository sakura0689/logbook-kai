package logbook.bean;

import java.io.Serializable;
import java.util.List;

import javax.json.JsonObject;

import logbook.internal.JsonHelper;
import lombok.Data;

/**
 * api_mst_useitem
 */
@Data
public class UseitemMst implements Serializable {

    private static final long serialVersionUID = -3290324243327123224L;

    /** api_id */
    private Integer id;

    /** api_name */
    private String name;

    /** api_description */
    private List<String> description;

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * JsonObjectから{@link UseitemMst}を構築します
     *
     * @param json JsonObject
     * @return {@link UseitemMst}
     */
    public static UseitemMst toUseitem(JsonObject json) {
        UseitemMst bean = new UseitemMst();
        JsonHelper.bind(json)
                .setInteger("api_id", bean::setId)
                .setString("api_name", bean::setName)
                .setStringList("api_description", bean::setDescription);
        return bean;
    }
}
