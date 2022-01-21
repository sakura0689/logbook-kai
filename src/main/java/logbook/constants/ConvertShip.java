package logbook.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * コンバート艦情報
 */
public enum ConvertShip {

    翔鶴(461, 466),
    瑞鶴(462, 467),
    朝潮(463, 468),
    霞(464, 470),
    最上(501, 506),
    鈴谷(503, 508),
    熊野(504, 509),
    Saratoga(545, 550),
    瑞鳳(555, 560),
    山風(588, 667),
    赤城(594, 599),
    加賀(610, 646, 698),
    夕張(622, 623, 624),
    Fletcher(628, 629),
    宗谷(645, 650, 699),
    球磨(652, 657),
    矢矧(663, 668),
    龍鳳(883, 888),
    天霧(903,908)
    ;

    private int[] shipIds;

    private ConvertShip(int... shipIds) {
        this.shipIds = shipIds;
    }
    
    private int[] getShipIds() {
        return this.shipIds;
    }
    
    private static final Map<Integer, String> shipidmap = new HashMap<Integer, String>() {{
        for (ConvertShip convertShip : ConvertShip.values()) {
            for (int id : convertShip.getShipIds()) {
                put(id, convertShip.name());
            }
        }
    }};
    
    /**
     * コンバート艦であるかの判定結果を返却します
     * 
     * @param shipid
     * @return
     */
    public static boolean isConvertShip(int shipid) {
        return shipidmap.containsKey(shipid);
    }
}
