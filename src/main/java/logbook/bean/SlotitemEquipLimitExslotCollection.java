package logbook.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import logbook.internal.Config;
import lombok.Data;

/**
 * 艦娘別補強スロット装備制限のコレクション
 *
 */
@Data
public class SlotitemEquipLimitExslotCollection implements Serializable {

    private static final long serialVersionUID = -7752617349132570935L;

    /** 艦娘別補強スロット装備制限 (艦娘ID -> 装備種別IDのセット) */
    private Map<Integer, Set<Integer>> shipLimitMap = new LinkedHashMap<>();

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link SlotitemEquipLimitExslotCollection}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(SlotitemEquipLimitExslotCollection.class, SlotitemEquipLimitExslotCollection::new)</code>
     * </blockquote>
     *
     * @return {@link SlotitemEquipLimitExslotCollection}
     */
    public static SlotitemEquipLimitExslotCollection get() {
        return Config.getDefault().get(SlotitemEquipLimitExslotCollection.class, SlotitemEquipLimitExslotCollection::new);
    }
}
