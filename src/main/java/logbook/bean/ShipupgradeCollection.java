package logbook.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import logbook.internal.Config;
import lombok.Data;

/**
 * 艦娘改装情報のコレクション
 *
 */
@Data
public class ShipupgradeCollection implements Serializable {

    private static final long serialVersionUID = 8352617349132570935L;

    /** 艦娘改装情報 */
    private Map<Integer, Shipupgrade> shipupgradeMap = new LinkedHashMap<>();

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link ShipupgradeCollection}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(ShipupgradeCollection.class, ShipupgradeCollection::new)</code>
     * </blockquote>
     *
     * @return {@link ShipupgradeCollection}
     */
    public static ShipupgradeCollection get() {
        return Config.getDefault().get(ShipupgradeCollection.class, ShipupgradeCollection::new);
    }
}
