package logbook.internal.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logbook.bean.Ship;
import logbook.bean.ShipMst;
import logbook.internal.kancolle.Ships;

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

    /** 改装取得装備 */
    private StringProperty equipments = new SimpleStringProperty("");

    /** 改装資材 */
    private StringProperty upgradeMaterials = new SimpleStringProperty("");

    /**
     * IDを取得します。
     * 
     * @return ID
     */
    public IntegerProperty idProperty() {
        return this.id;
    }

    /**
     * IDを取得します。
     * 
     * @return ID
     */
    public Integer getId() {
        return this.id.get();
    }

    /**
     * IDを設定します。
     * 
     * @param id ID
     */
    public void setId(Integer id) {
        this.id.set(id);
    }

    /**
     * 艦娘を取得します。
     * 
     * @return 艦娘
     */
    public ObjectProperty<Ship> shipProperty() {
        return this.ship;
    }

    /**
     * 艦娘を取得します。
     * 
     * @return 艦娘
     */
    public Ship getShip() {
        return this.ship.get();
    }

    /**
     * 艦娘を設定します。
     * 
     * @param ship 艦娘
     */
    public void setShip(Ship ship) {
        this.ship.set(ship);
    }

    /**
     * Lvを取得します。
     * 
     * @return Lv
     */
    public IntegerProperty lvProperty() {
        return this.lv;
    }

    /**
     * Lvを取得します。
     * 
     * @return Lv
     */
    public Integer getLv() {
        return this.lv.get();
    }

    /**
     * Lvを設定します。
     * 
     * @param lv Lv
     */
    public void setLv(Integer lv) {
        this.lv.set(lv);
    }

    /**
     * 改装Lvを取得します。
     * 
     * @return 改装Lv
     */
    public IntegerProperty afterLvProperty() {
        return this.afterLv;
    }

    /**
     * 改装Lvを取得します。
     * 
     * @return 改装Lv
     */
    public Integer getAfterLv() {
        return this.afterLv.get();
    }

    /**
     * 改装Lvを設定します。
     * 
     * @param afterLv 改装Lv
     */
    public void setAfterLv(Integer afterLv) {
        this.afterLv.set(afterLv);
    }

    /**
     * 改装取得装備を取得します。
     * 
     * @return 改装取得装備
     */
    public StringProperty equipmentsProperty() {
        return this.equipments;
    }

    /**
     * 改装取得装備を取得します。
     * 
     * @return 改装取得装備
     */
    public String getEquipments() {
        return this.equipments.get();
    }

    /**
     * 改装取得装備を設定します。
     * 
     * @param equipments 改装取得装備
     */
    public void setEquipments(String equipments) {
        this.equipments.set(equipments);
    }

    /**
     * 改装資材を取得します。
     * 
     * @return 改装資材
     */
    public StringProperty upgradeMaterialsProperty() {
        return this.upgradeMaterials;
    }

    /**
     * 改装資材を取得します。
     * 
     * @return 改装資材
     */
    public String getUpgradeMaterials() {
        return this.upgradeMaterials.get();
    }

    /**
     * 改装資材を設定します。
     * 
     * @param upgradeMaterials 改装資材
     */
    public void setUpgradeMaterials(String upgradeMaterials) {
        this.upgradeMaterials.set(upgradeMaterials);
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

        ShipMst shipMst = Ships.shipMst(ship).orElse(null);
        item.setAfterLv(shipMst != null && shipMst.getAfterlv() != null ? shipMst.getAfterlv() : 0);

        String equipmentsStr = "";
        if (shipMst != null && shipMst.getAftershipid() != null && shipMst.getAftershipid() != 0) {
            java.util.List<Integer> itemIds = Ships.shipItems(shipMst.getAftershipid()).orElse(null);
            if (itemIds != null && !itemIds.isEmpty()) {
                java.util.Map<Integer, logbook.bean.SlotitemMst> itemMap = logbook.bean.SlotitemMstCollection.get()
                        .getSlotitemMap();
                equipmentsStr = itemIds.stream()
                        .map(itemMap::get)
                        .filter(java.util.Objects::nonNull)
                        .map(logbook.bean.SlotitemMst::getName)
                        .collect(java.util.stream.Collectors.joining(", "));
            }
        }
        item.setEquipments(equipmentsStr);

        String upgradeMaterialsStr = "";
        logbook.bean.ShipUpgrade upgrade = logbook.bean.ShipUpgradeCollection.get().getShipUpgradeMap()
                .get(ship.getShipId());
        if (upgrade != null) {
            java.util.StringJoiner sj = new java.util.StringJoiner(", ");
            if (upgrade.getDrawingCount() > 0)
                sj.add("改装設計図 " + upgrade.getDrawingCount());
            if (upgrade.getCatapultCount() > 0)
                sj.add("試製甲板カタパルト " + upgrade.getCatapultCount());
            if (upgrade.getReportCount() > 0)
                sj.add("戦闘詳報 " + upgrade.getReportCount());
            if (upgrade.getAviationMatCount() > 0)
                sj.add("新型航空兵装資材 " + upgrade.getAviationMatCount());
            if (upgrade.getArmsMatCount() > 0)
                sj.add("新型兵装資材 " + upgrade.getArmsMatCount());
            if (upgrade.getTechCount() > 0)
                sj.add("海外艦最新技術 " + upgrade.getTechCount());
            if (upgrade.getBoilerCount() > 0)
                sj.add("新型高温高圧缶 " + upgrade.getBoilerCount());
            upgradeMaterialsStr = sj.toString();
        }
        item.setUpgradeMaterials(upgradeMaterialsStr);

        return item;
    }
}