package logbook.constants;

import java.util.stream.Stream;

/**
 * イベント海域の札情報
 *
 */
public enum SeaArea {

    識別札1("前路掃討部隊", 1, 4),
    識別札2("遠征偵察部隊", 2, 8),
    識別札3("遠征艦隊先遣隊", 3, 10),
    識別札4("地中海連合艦隊", 4, 12),
    識別札5("トーチ作戦英軍部隊", 5, 14),
    識別札6("トーチ作戦派遣部隊", 6, 16),
    識別札7("中央任務部隊", 7, 18),
    識別札8("東方任務部隊", 8, 20),
    識別札9("西方先遣部隊", 9, 22),
    識別札10("西方任務決戦部隊", 10, 5);

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
