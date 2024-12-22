package logbook.constants;

import logbook.bean.SlotitemMst;

/**
 * 装備種定数
 *
 */
public enum SlotItemType {

    小口径主砲(1, true),
    中口径主砲(2, true),
    大口径主砲(3, true),
    副砲(4, true),
    魚雷(5, true),
    艦上戦闘機(6, true),
    艦上爆撃機(7, true),
    艦上攻撃機(8, true),
    艦上偵察機(9, true),
    水上偵察機(10, true),
    水上爆撃機(11, true),
    小型電探(12, true),
    大型電探(13, true),
    ソナー(14, true),
    爆雷(15, true),
    追加装甲(16, true),
    機関部強化(17, true),
    対空強化弾(18, true),
    対艦強化弾(19, true),
    VT信管(20, true),
    対空機銃(21, true),
    特殊潜航艇(22, true),
    応急修理要員(23, false),
    上陸用舟艇(24, true),
    オートジャイロ(25, true),
    対潜哨戒機(26, true),
    追加装甲中型(27, true),
    追加装甲大型(28, true),
    探照灯(29, true),
    簡易輸送部材(30, true),
    艦艇修理施設(31, true),
    潜水艦魚雷(32, true),
    照明弾(33, true),
    司令部施設(34, true),
    航空要員(35, true),
    高射装置(36, true),
    対地装備(37, true),
    大口径主砲II(38, true),
    水上艦要員(39, true),
    大型ソナー(40, true),
    大型飛行艇(41, true),
    大型探照灯(42, true),
    戦闘糧食(43, false),
    補給物資(44, false),
    水上戦闘機(45, true),
    特型内火艇(46, true),
    陸上攻撃機(47, true),
    局地戦闘機(48, true),
    陸上偵察機(49, true),
    輸送機材(50, true),
    潜水艦装備(51, true),
    陸戦部隊(52, true),
    大型陸上機(53, true),
    水上艦装備(54, true),
    噴式戦闘機(56, true),
    噴式戦闘爆撃機(57, true),
    噴式攻撃機(58, true),
    噴式偵察機(59, true),
    大型電探II(93, true),
    艦上偵察機II(94, true),
    副砲II(95, true),
    
    不明(-1, true);

    private final int item;
    private final boolean isCount ;

    private SlotItemType(int item, boolean isCount) {
        this.item = item;
        this.isCount = isCount;
    }

    /**
     * この定数がitemと等しい場合trueを返します
     *
     * @param item 装備
     * @return 装備がこの定数と同じ場合はtrue
     */
    @Deprecated
    public boolean equals(SlotitemMst item) {
        return item != null && this.item == item.getType().get(SlotitemMstTypeConst.SLOT_ITEM_TYPE);
    }
    
    /**
     * 装備種定数を返します
     * 
     * @return 装備種定数
     */
    public int getType() {
        return this.item;
    }

    /**
     * 所有装備としてカウントされるかフラグを返却します
     * 
     * @return
     */
    public boolean isCount() {
        return this.isCount;
    }
    
    /**
     * 装備種定数を返します
     *
     * @param item 装備
     * @return 装備種定数
     */
    public static SlotItemType toSlotItemType(SlotitemMst item) {
        int type = item.getType().get(SlotitemMstTypeConst.SLOT_ITEM_TYPE);
        for (SlotItemType e : values()) {
            if (e.item == type) {
                return e;
            }
        }
        return 不明;
    }
}
