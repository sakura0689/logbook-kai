package logbook.internal;

import logbook.bean.Ship;
import logbook.bean.Stype;

/**
 * 統計パネル
 * 艦種グループ
 *
 */
public enum TypeGroup {
    駆逐艦("駆逐艦"),
    海防艦("海防艦"),
    正規空母("正規空母", "装甲空母"),
    軽空母("軽空母"),
    戦艦("戦艦", "航空戦艦"),
    巡洋艦("軽巡洋艦", "重雷装巡洋艦", "練習巡洋艦", "重巡洋艦", "航空巡洋艦"),
    潜水艦("潜水艦", "潜水空母"),
    特殊艦("水上機母艦", "揚陸艦", "工作艦", "潜水母艦", "補給艦");

    private String[] group;

    private TypeGroup(String... shipTypes) {
        this.group = shipTypes;
    }

    /**
     * 艦娘情報から艦種グループを返却します
     * 
     * @param ship
     */
    public static TypeGroup toTypeGroup(Ship ship) {
        Stype stype = Ships.stype(ship).orElse(null);
        if (stype != null) {
            String name = stype.getName();
            for (TypeGroup group : values()) {
                for (String v : group.group) {
                    if (v.equals(name))
                        return group;
                }
            }
        }
        return null;
    }

}
