package logbook.internal.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logbook.bean.AppCondition;
import logbook.bean.Basic;
import logbook.bean.Material;
import logbook.bean.SlotItemCollection;
import logbook.bean.Useitem;
import logbook.bean.UseitemCollection;
import logbook.bean.UseitemMst;

/**
 * アイテム
 */
public class UseitemItem {

    /** ID */
    private IntegerProperty id = new SimpleIntegerProperty();

    /** 名前 */
    private StringProperty name = new SimpleStringProperty();

    /** 個数 */
    private IntegerProperty count = new SimpleIntegerProperty();

    /** 説明 */
    private StringProperty description = new SimpleStringProperty();

    public Integer getId() {
        return this.id.get();
    }

    public void setId(Integer id) {
        this.id.setValue(id);
    }

    public IntegerProperty idProperty() {
        return this.id;
    }

    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public Integer getCount() {
        return this.count.get();
    }

    public void setCount(Integer count) {
        this.count.setValue(count);
    }

    public IntegerProperty countProperty() {
        return this.count;
    }

    public String getDescription() {
        return this.description.get();
    }

    public void setDescription(String description) {
        this.description.setValue(description);
    }

    public StringProperty descriptionProperty() {
        return this.description;
    }

    /** Useitem API での ID と material ID のマッピング */
    private static final Map<Integer, Integer> MATERIALS_MAP = new HashMap<>();
    
    /** Useitem API での ID と Slotitem ID のマッピング */
    private static final Map<Integer, Integer> ITEMS_MAP = new HashMap<>();

    static {
        MATERIALS_MAP.put(1, 6);    // バケツ
        MATERIALS_MAP.put(2, 5);    // バーナー
        MATERIALS_MAP.put(3, 7);    // 釘
        MATERIALS_MAP.put(4, 8);    // ネジ
        MATERIALS_MAP.put(31, 1);   // 燃料
        MATERIALS_MAP.put(32, 2);   // 弾薬
        MATERIALS_MAP.put(33, 3);   // 鋼材
        MATERIALS_MAP.put(34, 4);   // ボーキ
        ITEMS_MAP.put(50, 42);  // ダメコン
        ITEMS_MAP.put(51, 43);  // 女神
        ITEMS_MAP.put(66, 145);  // おにぎり
        ITEMS_MAP.put(67, 146);  // 洋上補給
        ITEMS_MAP.put(69, 150);  // 秋刀魚の缶詰
        ITEMS_MAP.put(76, 241);  // 特別なおにぎり
    }
    
    public static UseitemItem toUseitemItem(UseitemMst item) {
        UseitemItem ret = new UseitemItem();
        ret.setId(item.getId());
        ret.setName(item.getName());
        if (item.getDescription().size() > 0) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(item.getDescription().get(0).replaceAll("<br>", ""));
            if (item.getDescription().size() > 1 && item.getDescription().get(1).trim().length() > 0) {
                sb.append(" (").append(item.getDescription().get(1)).append(")");
            }
            ret.setDescription(sb.toString());
        }
        
        if (MATERIALS_MAP.containsKey(item.getId())) {
            // 資材系
            ret.setCount(Optional.ofNullable(MATERIALS_MAP.get(item.getId()))
                .map(AppCondition.get().getMaterial()::get)
                .map(Material::getValue)
                .orElse(0));
        } else if (item.getId() == 44) {
            // 家具コインのみ Basic からとる
            ret.setCount(Basic.get().getFcoin());
        } else if (ITEMS_MAP.containsKey(item.getId())) {
            // 装備系
            final int slotitemId = ITEMS_MAP.get(item.getId());
            ret.setCount((int)SlotItemCollection.get().getSlotitemMap().values().stream()
                    .filter(slotitem -> slotitem.getSlotitemId() == slotitemId)
                    .count());
        } else {
            // その他の純粋なアイテム
            Optional.ofNullable(UseitemCollection.get().getUseitemMap().get(item.getId()))
                .map(Useitem::getCount)
                .ifPresent(ret::setCount);
        }
        return ret;
    }
    
    @Override
    public String toString() {
        return new StringJoiner("\t")
                .add(Integer.toString(this.id.get()))
                .add(this.name.get())
                .add(Optional.ofNullable(this.count.get()).map(c -> Integer.toString(c)).orElse("-"))
                .add(this.description.get())
                .toString();
    }
}
