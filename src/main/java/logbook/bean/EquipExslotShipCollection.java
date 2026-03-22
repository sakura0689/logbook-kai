package logbook.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import logbook.internal.Config;
import lombok.Data;

/**
 * 装備別補強スロット装備追加条件のコレクション
 *
 */
@Data
public class EquipExslotShipCollection implements Serializable {

    private static final long serialVersionUID = -8352618349132570935L;

    /** 装備別補強スロット装備追加条件 (装備ID または 装備種別ID -> 条件) */
    private Map<Integer, EquipExslotShip> equipExslotShipMap = new LinkedHashMap<>();

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link EquipExslotShipCollection}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(EquipExslotShipCollection.class, EquipExslotShipCollection::new)</code>
     * </blockquote>
     *
     * @return {@link EquipExslotShipCollection}
     */
    public static EquipExslotShipCollection get() {
        return Config.getDefault().get(EquipExslotShipCollection.class, EquipExslotShipCollection::new);
    }
}
