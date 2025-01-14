package logbook.internal.gui;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.StringJoiner;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logbook.bean.DeckPort;
import logbook.bean.Ship;
import logbook.bean.ShipMst;
import logbook.internal.Ships;
import logbook.internal.util.DateUtil;
import logbook.internal.util.TimeUtil;

/**
 * お風呂に入りたい艦娘
 *
 */
public class RequireNdock {

    /** 艦隊 */
    private StringProperty deck = new SimpleStringProperty();

    /** 艦娘 */
    private ObjectProperty<Ship> ship = new SimpleObjectProperty<Ship>();

    /** Lv */
    private IntegerProperty lv = new SimpleIntegerProperty();

    /** 時間 */
    private ObjectProperty<Duration> time = new SimpleObjectProperty<Duration>();

    /** 今から */
    private StringProperty end = new SimpleStringProperty();

    /** 燃料 */
    private IntegerProperty fuel = new SimpleIntegerProperty();

    /** 鋼材 */
    private IntegerProperty metal = new SimpleIntegerProperty();

    /**
     * 艦隊を取得します。
     * @return 艦隊
     */
    public StringProperty deckProperty() {
        return this.deck;
    }

    /**
     * 艦隊を取得します。
     * @return 艦隊
     */
    public String getDeck() {
        return this.deck.get();
    }

    /**
     * 艦隊を設定します。
     * @param deck 艦隊
     */
    public void setDeck(String deck) {
        this.deck.set(deck);
    }

    /**
     * 艦娘を取得します。
     * @return 艦娘
     */
    public ObjectProperty<Ship> shipProperty() {
        return this.ship;
    }

    /**
     * 艦娘を取得します。
     * @return 艦娘
     */
    public Ship getShip() {
        return this.ship.get();
    }

    /**
     * 艦娘を設定します。
     * @param ship 艦娘
     */
    public void setShip(Ship ship) {
        this.ship.set(ship);
    }

    /**
     * Lvを取得します。
     * @return Lv
     */
    public IntegerProperty lvProperty() {
        return this.lv;
    }

    /**
     * Lvを取得します。
     * @return Lv
     */
    public Integer getLv() {
        return this.lv.get();
    }

    /**
     * Lvを設定します。
     * @param lv Lv
     */
    public void setLv(Integer lv) {
        this.lv.set(lv);
    }

    /**
     * 時間を取得します。
     * @return 時間
     */
    public ObjectProperty<Duration> timeProperty() {
        return this.time;
    }

    /**
     * 時間を取得します。
     * @return 時間
     */
    public Duration getTime() {
        return this.time.get();
    }

    /**
     * 時間を設定します。
     * @param time 時間
     */
    public void setTime(Duration time) {
        this.time.set(time);
    }

    /**
     * 今からを取得します。
     * @return 今から
     */
    public StringProperty endProperty() {
        return this.end;
    }

    /**
     * 今からを取得します。
     * @return 今から
     */
    public String getEnd() {
        return this.end.get();
    }

    /**
     * 今からを設定します。
     * @param end 今から
     */
    public void setEnd(String end) {
        this.end.set(end);
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public IntegerProperty fuelProperty() {
        return this.fuel;
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public Integer getFuel() {
        return this.fuel.get();
    }

    /**
     * 燃料を設定します。
     * @param fuel 燃料
     */
    public void setFuel(Integer fuel) {
        this.fuel.set(fuel);
    }

    /**
     * 鋼材を取得します。
     * @return 鋼材
     */
    public IntegerProperty metalProperty() {
        return this.metal;
    }

    /**
     * 鋼材を取得します。
     * @return 鋼材
     */
    public Integer getMetal() {
        return this.metal.get();
    }

    /**
     * 鋼材を設定します。
     * @param metal 鋼材
     */
    public void setMetal(Integer metal) {
        this.metal.set(metal);
    }

    @Override
    public String toString() {
        return new StringJoiner("\t")
                .add(this.deck.get())
                .add(Optional.ofNullable(this.ship.get())
                        .map(s -> Ships.shipMst(s).map(ShipMst::getName).orElse(""))
                        .orElse(""))
                .add(Integer.toString(this.lv.get()))
                .add(TimeUtil.toString(this.time.get(), "修復完了"))
                .add(this.end.get())
                .add(Integer.toString(this.fuel.get()))
                .add(Integer.toString(this.metal.get()))
                .toString();
    }

    /**
     * 艦娘からお風呂に入りたい艦娘を生成します
     *
     * @param ship 艦娘
     * @return お風呂に入りたい艦娘
     */
    public static RequireNdock toRequireNdock(Ship ship) {
        RequireNdock ndock = new RequireNdock();
        ndock.setShip(ship);
        ndock.update();
        return ndock;
    }

    /**
     * お風呂に入りたい艦娘を更新します
     */
    public void update() {
        Ship ship = this.getShip();
        Duration d = Duration.ofMillis(ship.getNdockTime());
        this.setDeck(Ships.deckPort(ship).map(DeckPort::getId).map(Object::toString).orElse(""));
        this.setLv(ship.getLv());
        this.setTime(d);
        this.setEnd(endText(d));
        this.setFuel(ship.getNdockItem().get(0));
        this.setMetal(ship.getNdockItem().get(1));
    }

    /**
     * 今からのテキスト表現
     *
     * @param d 期間
     * @return 今からのテキスト表現
     */
    private static String endText(Duration d) {
        ZonedDateTime dateTime = DateUtil.now().plus(d);
        return DateTimeFormatter.ofPattern("H時m分s秒").format(dateTime);
    }
}
