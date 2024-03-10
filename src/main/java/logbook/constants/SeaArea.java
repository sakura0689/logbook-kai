package logbook.constants;

import java.util.stream.Stream;

/**
 * イベント海域の札情報
 *
 */
public enum SeaArea {

    識別札1("第一艦隊", 1, 4),
    識別札2("第二艦隊", 2, 10),
    識別札3("第五艦隊", 3, 12),
    識別札4("機動部隊", 4, 14),
    識別札5("第六艦隊", 5, 16),
    識別札6("第三艦隊先遣隊", 6, 18),
    識別札7("前衛艦隊", 7, 20),
    識別札8("第三艦隊", 8, 22),
    識別札9("ウェーキ島輸送部隊", 9, 24),
    識別札10("新編竜巻部隊", 10, 5),
    識別札11("Z決戦艦隊", 11, 7);

    /** 名前 */
    private String name;

    /** 海域(イベント海域のお札) */
    private int area;
    
    /** お札アイコンのNo
     *  resources\common\common_event
     *  */
    private int imageNo;

    SeaArea(String name, int area, int imageNo) {
        this.name = name;
        this.area = area;
        this.imageNo = imageNo;
    }

    /**
     * 名前を取得します。
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * 海域(イベント海域のお札)を取得します。
     * @return 海域(イベント海域のお札)
     */
    public int getArea() {
        return this.area;
    }

    /**
     * お札アイコンのNoを取得します。
     * @return お札アイコンのNo
     */
    public int getImageId() {
        return this.imageNo;
    }
    
    /**
     * お札アイコンのNoを取得します。
     * @param 海域(イベント海域のお札)
     * @return お札アイコンのNo
     */
    public static int getImageId(int area) {
        SeaArea seaArea = fromArea(area);
        if (seaArea == null) {
           return 0; 
        }
        return seaArea.getImageId();
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * イベント海域を取得します
     *
     * @param area お札
     * @return 海域
     */
    public static SeaArea fromArea(int area) {
        return Stream.of(SeaArea.values()).filter(s -> s.getArea() == area).findAny().orElse(null);
    }
    
}
