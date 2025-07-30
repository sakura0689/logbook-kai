package logbook.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * api_mst_mapinfo
 *
 */
@Data
public class MapinfoMst implements Serializable {

    private static final long serialVersionUID = -5261390920143650901L;

    /** api_id */
    private Integer id;

    /** api_maparea_id */
    private Integer mapareaId;

    /** api_no */
    private Integer no;

    /** api_name */
    private String name;

    /** api_level */
    private Integer level;

    /** api_opetext */
    private String opetext;

    /** api_infotext */
    private String infotext;

    /** api_item */
    private List<Integer> item;

    /** api_max_maphp */
    private Integer maxMaphp;

    /** api_required_defeat_count */
    private Integer requiredDefeatCount;

    /** api_sally_flag */
    private List<Integer> sallyFlag;

    /**
     * JsonObjectから{@link MapinfoMst}を構築します
     *
     * @param json JsonObject
     * @return {@link MapinfoMst}
     */
    public static MapinfoMst toMapinfoMst(JsonObject json) {
        MapinfoMst bean = new MapinfoMst();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_maparea_id", bean::setMapareaId)
                .setInteger("api_no", bean::setNo)
                .setString("api_name", bean::setName)
                .setInteger("api_level", bean::setLevel)
                .setString("api_opetext", bean::setOpetext)
                .setString("api_infotext", bean::setInfotext)
                .setIntegerList("api_item", bean::setItem)
                .setInteger("api_max_maphp", bean::setMaxMaphp)
                .setInteger("api_required_defeat_count", bean::setRequiredDefeatCount)
                .setIntegerList("api_sally_flag", bean::setSallyFlag);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}
