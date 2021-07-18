package logbook.internal.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import logbook.bean.Ship;
import logbook.bean.ShipMst;
import logbook.internal.Ships;

/**
 * 未改装の艦娘テーブルの行
 *
 */
public class ShortageShipItem {

    /** ID */
    private IntegerProperty id = new SimpleIntegerProperty();

    /** 艦娘 */
    private ObjectProperty<Ship> ship = new SimpleObjectProperty<Ship>();

    /** Lv */
    private IntegerProperty lv = new SimpleIntegerProperty();

    /** 改装Lv */
    private IntegerProperty afterLv = new SimpleIntegerProperty();

    /**
     * IDを取得します。
     * @return ID
     */
    public IntegerProperty idProperty() {
        return this.id;
    }

    /**
     * IDを取得します。
     * @return ID
     */
    public Integer getId() {
        return this.id.get();
    }

    /**
     * IDを設定します。
     * @param id ID
     */
    public void setId(Integer id) {
        this.id.set(id);
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
     * 改装Lvを取得します。
     * @return 改装Lv
     */
    public IntegerProperty afterLvProperty() {
        return this.afterLv;
    }

    /**
     * 改装Lvを取得します。
     * @return 改装Lv
     */
    public Integer getAfterLv() {
        return this.afterLv.get();
    }

    /**
     * 改装Lvを設定します。
     * @param afterLv 改装Lv
     */
    public void setAfterLv(Integer afterLv) {
        this.afterLv.set(afterLv);
    }

    /**
     * 未改装の艦娘テーブルの行を作成します
     *
     * @param ship 艦娘
     * @return 未改装の艦娘テーブルの行
     */
    public static ShortageShipItem toShipItem(Ship ship) {
        ShortageShipItem item = new ShortageShipItem();
        item.setId(ship.getId());
        item.setShip(ship);
        item.setLv(ship.getLv());
        item.setAfterLv(Ships.shipMst(ship).map(ShipMst::getAfterlv).orElse(0));
        return item;
    }
}