package logbook.internal.kancolle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logbook.bean.SlotitemEquipShipCollection;

/**
 * ItemIDから装備可能な艦娘一覧を取得するためのクラス。
 * SlotitemEquipShipCollectionのデータを逆引きしてキャッシュとして保持する。
 */
public class SlotitemEquipShips {

    private static final SlotitemEquipShips INSTANCE = new SlotitemEquipShips();
    private final Map<Integer, List<Integer>> itemToShipMap = new HashMap<>();

    private SlotitemEquipShips() {
        initialize();
    }

    /**
     * Singletonインスタンスを取得します。
     * @return SlotitemEquipShips インスタンス
     */
    public static SlotitemEquipShips getInstance() {
        return INSTANCE;
    }

    private void initialize() {
        SlotitemEquipShipCollection collection = SlotitemEquipShipCollection.get();
        if (collection == null || collection.getEquipShipMap() == null) {
            return;
        }

        // ShipID -> (CategoryID -> List<ItemID>) を走査して ItemID -> List<ShipID> に変換
        collection.getEquipShipMap().forEach((shipId, categoryMap) -> {
            if (categoryMap != null) {
                categoryMap.values().forEach(itemIds -> {
                    if (itemIds != null) {
                        for (Integer itemId : itemIds) {
                            itemToShipMap.computeIfAbsent(itemId, k -> new ArrayList<>()).add(shipId);
                        }
                    }
                });
            }
        });

        // リストをソートして一貫性を持たせる（任意）
        itemToShipMap.values().forEach(Collections::sort);
    }

    /**
     * 指定されたItemIDを装備可能な艦娘IDの一覧を取得します。
     * @param itemId 装備アイテムID
     * @return 装備可能な艦娘IDのリスト。該当がない場合は空のリストを返します。
     */
    public List<Integer> getShips(int itemId) {
        List<Integer> ships = itemToShipMap.get(itemId);
        return ships != null ? Collections.unmodifiableList(ships) : Collections.emptyList();
    }

    /**
     * ItemID,List<Ship>のMap情報を返却します
     * 
     * @return 装備可能な艦娘IDのリスト。該当がない場合は空のリストを返します。
     */
    public Map<Integer, List<Integer>> getAllSlotitemEquipShips() {
        return itemToShipMap;
    }

}
