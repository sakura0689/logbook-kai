package logbook.constants;

import java.util.stream.Stream;

/**
 * イベント海域の札情報
 *
 */
public enum SeaArea {

    識別札1("改R4計画艦隊", 1, 4),
    識別札2("ナルヴィク先遣隊", 2, 16),
    識別札3("ナルヴィク防衛主隊", 3, 18),
    識別札4("ナルヴィク駐留艦隊", 4, 20),
    識別札5("イギリス本国艦隊", 5, 22),
    識別札6("イギリス機動部隊", 6, 24),
    識別札7("欧州遠征収容艦隊", 7, 26),
    識別札8("拡張第三十一戦隊", 8, 28),
    識別札9("機動部隊", 9, 30),
    識別札10("礼号作戦部隊", 10, 5),
    識別札11("横須賀防備戦隊", 11, 7),
    識別札12("連合艦隊", 12, 9),
    識別札13("第百四戦隊", 13, 11),
    識別札14("決戦艦隊", 14, 13)
    ;

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
