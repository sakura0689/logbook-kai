package logbook.constants;

import java.util.stream.Stream;

/**
 * イベント海域の札情報
 *
 */
public enum SeaArea {

    識別札1("第一特務艦隊", 1),
    識別札2("地中海艦隊", 2),
    識別札3("第二特務艦隊", 3),
    識別札4("ForceH", 4),
    識別札5("ForceZ", 5),
    識別札6("第10潜水戦隊", 6),
    識別札7("ForceX", 7),
    識別札8("識別札8", 8),
    識別札9("識別札9", 9),
    識別札10("識別札10", 10);

    /** 名前 */
    private String name;

    /** 海域(イベント海域のお札) */
    private int area;

    SeaArea(String name, int area) {
        this.name = name;
        this.area = area;
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
