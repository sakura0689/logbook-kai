package logbook.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import logbook.internal.Config;
import lombok.Data;

/**
 * 艦娘別装備制限のコレクション
 *
 */
@Data
public class SlotitemEquipShipCollection implements Serializable {

    private static final long serialVersionUID = 4352618349132570935L;

    /** 艦娘別装備制限 (艦娘ID -> (装備種別ID -> 装備アイテムIDのリスト)) */
    private Map<Integer, Map<Integer, List<Integer>>> equipShipMap = new LinkedHashMap<>();

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link SlotitemEquipShipCollection}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(SlotitemEquipShipCollection.class, SlotitemEquipShipCollection::new)</code>
     * </blockquote>
     *
     * @return {@link SlotitemEquipShipCollection}
     */
    public static SlotitemEquipShipCollection get() {
        return Config.getDefault().get(SlotitemEquipShipCollection.class, SlotitemEquipShipCollection::new);
    }
}
