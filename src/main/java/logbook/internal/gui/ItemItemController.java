package logbook.internal.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.TextFields;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import logbook.bean.AppItemTableConfig;
import logbook.bean.Ship;
import logbook.bean.SlotItem;
import logbook.bean.SlotItemCollection;
import logbook.bean.SlotitemEquiptype;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;
import logbook.common.Messages;
import logbook.core.LogBookCoreServices;
import logbook.internal.kancolle.Items;
import logbook.internal.kancolle.Ships;
import logbook.internal.logger.LoggerHolder;
import lombok.Data;

/**
 * 所有装備一覧のUIコントローラー
 *
 */
public class ItemItemController extends WindowController {

    @FXML
    private SplitPane splitPane;

    // フィルター
    /** フィルター */
    @FXML
    private TitledPane filter;

    @FXML
    private FlowPane filters;

    /** テキスト */
    @FXML
    private ToggleSwitch textFilter;

    /** テキスト */
    @FXML
    private ComboBox<String> textValue;

    @FXML
    private GridPane typeFilterPane;

    /** 種類フィルター */
    @FXML
    private ToggleSwitch typeFilter;

    @FXML
    /** 種別フィルターのタイトルペイン */
    private TitledPane typeFilterTitledPane;

    @FXML
    /** 種別フィルターの全選択・全解除 */
    private CheckBox allTypes;

    /** 種別フィルター */
    private final Map<Integer, CheckBox> typeFilters = new TreeMap<>();

    /** パラメータフィルター */
    private List<ParameterFilterPane<Item>> parameterFilters;

    // 一覧(装備一覧)

    /** 一覧 */
    @FXML
    private TableView<Item> typeTable;

    /** 名称 */
    @FXML
    private TableColumn<Item, Integer> name;

    /** 種別 */
    @FXML
    private TableColumn<Item, String> type;

    /** 個数 */
    @FXML
    private TableColumn<Item, Integer> count;

    /** 火力 */
    @FXML
    private TableColumn<Item, Integer> houg;

    /** 命中 */
    @FXML
    private TableColumn<Item, Integer> houm;

    /** 射程 */
    @FXML
    private TableColumn<Item, Integer> leng;

    /** 運 */
    @FXML
    private TableColumn<Item, Integer> luck;

    /** 回避 */
    @FXML
    private TableColumn<Item, Integer> houk;

    /** 爆装 */
    @FXML
    private TableColumn<Item, Integer> baku;

    /** 雷装 */
    @FXML
    private TableColumn<Item, Integer> raig;

    /** 索敵 */
    @FXML
    private TableColumn<Item, Integer> saku;

    /** 対潜 */
    @FXML
    private TableColumn<Item, Integer> tais;

    /** 対空 */
    @FXML
    private TableColumn<Item, Integer> tyku;

    /** 装甲 */
    @FXML
    private TableColumn<Item, Integer> souk;

    // 一覧(所持)

    /** 詳細・名前 */
    @FXML
    private Label detailName;

    /** 詳細・一覧 */
    @FXML
    private TableView<DetailItem> detailTable;

    /** 熟練 */
    @FXML
    private TableColumn<DetailItem, Integer> alv;

    /** 改修 */
    @FXML
    private TableColumn<DetailItem, Integer> level;

    /** 所持 */
    @FXML
    private TableColumn<DetailItem, Ship> ship;

    /** 一覧 */
    private FilteredList<Item> types;

    /** 詳細一覧 */
    private ObservableList<DetailItem> details = FXCollections.observableArrayList();

    /** フィルターの更新停止 */
    private boolean disableFilterUpdate;

    @FXML
    void initialize() {
        try {
            TableTool.setVisible(this.typeTable, this.getClass().toString() + "#" + "typeTable");
            TableTool.setVisible(this.detailTable, this.getClass().toString() + "#" + "detailTable");
            // SplitPaneの分割サイズ
            Timeline x = new Timeline();
            x.getKeyFrames().add(new KeyFrame(Duration.millis(1), (e) -> {
                Tools.Controls.setSplitWidth(this.splitPane, this.getClass() + "#" + "splitPane");
            }));
            x.play();
            this.filter.expandedProperty().addListener((ob, o, n) -> saveConfig());
            this.textFilter.selectedProperty().addListener((ob, ov, nv) -> {
                this.textValue.setDisable(!nv);
                this.textValue.setDisable(!nv);
            });
            this.textFilter.selectedProperty().addListener(this::filterAction);
            this.textValue.getSelectionModel().selectedItemProperty().addListener(this::filterAction);

            this.typeFilterTitledPane.expandedProperty().addListener((ob, o, n) -> saveConfig());
            // カテゴリを調べて存在するものだけ表示する
            Map<Integer, SlotitemMst> slotitemMap = SlotitemMstCollection.get().getSlotitemMap();
            Set<Integer> existingTypes = SlotItemCollection.get().getSlotitemMap().values().stream()
                .map(SlotItem::getSlotitemId)
                .map(slotitemMap::get)
                .map(mst -> mst.getType().get(2))
                .collect(Collectors.toSet());
            Map<String, List<SlotitemEquiptype>> categories = Items.getCategories();
            final AtomicInteger row = new AtomicInteger(1);
            final List<CheckBox> categoryCheckBoxes = new ArrayList<CheckBox>();
            categories.forEach((name, equipTypes) -> {
                List<CheckBox> types = equipTypes.stream()
                        // 該当の装備を1つも持ってないものは除外
                        .filter(type -> type != null && existingTypes.contains(type.getId()))
                        .map(type -> {
                            CheckBox check = new CheckBox(type.getName());
                            check.setDisable(true);
                            check.selectedProperty().addListener(this::filterAction);
                            this.typeFilters.put(type.getId(), check);
                            return check;
                        })
                        .collect(Collectors.toList());
                if (types.isEmpty()) {
                    // 空であれば何もしない
                    return;
                }

                FlowPane categoryPane = new FlowPane();
                CheckBox category = new CheckBox(name);
                category.setDisable(true);
                category.setAllowIndeterminate(true);
                categoryPane.getChildren().add(category);
                this.typeFilterPane.add(categoryPane, 0, row.get());
                categoryCheckBoxes.add(category);

                FlowPane typesPane = new FlowPane(5, 0);
                typesPane.setPrefWidth(Integer.MAX_VALUE);
                typesPane.getChildren().addAll(types);
                this.typeFilterPane.add(typesPane, 1, row.getAndIncrement());
                Tools.Controls.bindChildCheckBoxes(category, types);
                category.disabledProperty().addListener((ob, o, n) -> types.forEach(c -> c.setDisable(n)));
            });
            Tools.Controls.bindChildCheckBoxes(this.allTypes, categoryCheckBoxes);
            this.typeFilter.selectedProperty().addListener(this::filterAction);
            this.typeFilter.selectedProperty().addListener((ob, o, n) -> {
                categoryCheckBoxes.forEach(c -> c.setDisable(!n));
                this.allTypes.setDisable(!n);
            });


            this.parameterFilters = IntStream.range(0, 3).mapToObj(i -> new ParameterFilterPane.ItemParameterFilterPane()).collect(Collectors.toList());
            this.filters.getChildren().addAll(this.parameterFilters);
            this.parameterFilters.forEach(f -> f.filterProperty().addListener(this::filterAction));

            // カラムとオブジェクトのバインド
            this.name.setCellValueFactory(new PropertyValueFactory<>("id"));
            this.name.setCellFactory(p -> new ItemImageCell<>());
            this.type.setCellValueFactory(new PropertyValueFactory<>("type"));
            this.count.setCellValueFactory(new PropertyValueFactory<>("count"));
            this.houg.setCellValueFactory(new PropertyValueFactory<>("houg"));
            this.houm.setCellValueFactory(new PropertyValueFactory<>("houm"));
            this.leng.setCellValueFactory(new PropertyValueFactory<>("leng"));
            this.luck.setCellValueFactory(new PropertyValueFactory<>("luck"));
            this.houk.setCellValueFactory(new PropertyValueFactory<>("houk"));
            this.baku.setCellValueFactory(new PropertyValueFactory<>("baku"));
            this.raig.setCellValueFactory(new PropertyValueFactory<>("raig"));
            this.saku.setCellValueFactory(new PropertyValueFactory<>("saku"));
            this.tais.setCellValueFactory(new PropertyValueFactory<>("tais"));
            this.tyku.setCellValueFactory(new PropertyValueFactory<>("tyku"));
            this.souk.setCellValueFactory(new PropertyValueFactory<>("souk"));

            this.alv.setCellValueFactory(new PropertyValueFactory<>("alv"));
            this.alv.setCellFactory(p -> new ItemAlvCell<>());
            this.level.setCellValueFactory(new PropertyValueFactory<>("level"));
            this.level.setCellFactory(p -> new ItemLevelCell<>());
            this.ship.setCellValueFactory(new PropertyValueFactory<>("ship"));
            this.ship.setCellFactory(p -> new ShipImageCell());

            this.typeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            this.typeTable.setOnKeyPressed(TableTool::defaultOnKeyPressedHandler);
            this.detailTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            this.detailTable.setOnKeyPressed(TableTool::defaultOnKeyPressedHandler);
            // 行を作る
            ObservableList<Item> items = SlotitemMstCollection.get()
                    .getSlotitemMap()
                    .values()
                    .stream()
                    .map(Item::toItem)
                    .filter(e -> e.getCount() > 0)
                    .sorted(Comparator.comparing(Item::getType3).thenComparing(Comparator.comparing(Item::getName)))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            // テキストフィルター
            this.textValue.setItems(items.stream()
                    .map(Item::typeProperty)
                    .map(StringProperty::get)
                    .distinct()
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
            TextFields.bindAutoCompletion(this.textValue.getEditor(),
                    new SuggestSupport(String::contains, items.stream()
                            .flatMap(i -> Stream.of(i.typeProperty().get(), i.getName()))
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList())));

            // 装備一覧(装備一覧)
            this.types = new FilteredList<>(items);
            SortedList<Item> sortedListTypes = new SortedList<>(this.types);
            this.typeTable.setItems(sortedListTypes);
            sortedListTypes.comparatorProperty().bind(this.typeTable.comparatorProperty());
            // 装備一覧(所持) 最初は空のリスト
            SortedList<DetailItem> sortedListDetail = new SortedList<>(this.details);
            this.detailTable.setItems(sortedListDetail);
            sortedListDetail.comparatorProperty().bind(this.detailTable.comparatorProperty());

            // 装備が選択された時のリスナーを設定
            this.typeTable.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(this::detail);
            
            loadConfig();
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * フィルターを設定する
     */
    private void filterAction(ObservableValue<?> observable, Object oldValue, Object newValue) {
        createFilter();
        saveConfig();
    }
    
    private void createFilter() {
        Predicate<Item> filter = ItemFilter.DefaultFilter.builder()
                .typeFilter(this.textFilter.isSelected())
                .typeValue(this.textValue.getValue() == null ? "" : this.textValue.getValue())
                .build();
        filter = filterAnd(filter, this.parameterFilters.stream()
            .map(ParameterFilterPane::filterProperty)
            .map(ReadOnlyObjectProperty::get)
            .filter(Objects::nonNull)
            .reduce((acc, val) -> filterAnd(acc, val))
            .orElse(null));
        if (this.typeFilter.isSelected()) {
            Set<Integer> selectedTypes = getSelectedTypes();
            filter = filterAnd(filter, (item) -> selectedTypes.contains(item.getType2()));
        }
        this.types.setPredicate(filter);
    }
    
    private Set<Integer> getSelectedTypes() {
        return this.typeFilters.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private <T> Predicate<T> filterAnd(Predicate<T> base, Predicate<T> add) {
        if (base != null) {
            return add != null ? base.and(add) : base;
        }
        return add;
    }

    /**
     * 右ペインに詳細表示するリスナー
     *
     * @param observable 値が変更されたObservableValue
     * @param oldValue 古い値
     * @param value 新しい値
     */
    private void detail(ObservableValue<? extends Item> observable, Item oldValue, Item value) {
        this.details.clear();
        if (value != null) {
            // 選択
            this.detailName.setText(value.getName());
            // 行を作る
            List<DetailItem> items = SlotItemCollection.get()
                    .getSlotitemMap()
                    .values()
                    .stream()
                    .filter(e -> e.getSlotitemId().equals(value.idProperty().get()))
                    .sorted(Comparator.comparing(SlotItem::getAlv,
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                            .reversed())
                    .sorted(Comparator.comparing(SlotItem::getLevel,
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                            .reversed())
                    .map(DetailItem::toDetailItem)
                    .sorted(Comparator.comparing(DetailItem::getShipId,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            this.details.addAll(items);
        } else {
            // 未選択
            this.detailName.setText("");
        }
    }

    /**
     * 装備アイコンを表示するセル
     */
    private static class ItemImageCell<T> extends TableCell<T, Integer> {
        @Override
        protected void updateItem(Integer itemId, boolean empty) {
            super.updateItem(itemId, empty);
            if (!empty) {
                SlotitemMst mst = SlotitemMstCollection.get()
                        .getSlotitemMap()
                        .get(itemId);

                if (mst != null) {
                    this.setGraphic(Tools.Controls.zoomImage(new ImageView(Items.itemImage(mst))));
                    this.setText(mst.getName());
                }
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }

    /**
     * 熟練度を表示するセル
     */
    private static class ItemAlvCell<T> extends TableCell<T, Integer> {
        @Override
        protected void updateItem(Integer alv, boolean empty) {
            super.updateItem(alv, empty);
            if (!empty && alv != null && alv > 0) {
                URL url = LogBookCoreServices.getResource("logbook/gui/alv" + alv + ".png");
                Pane pane = new StackPane(new ImageView(url.toString()));
                pane.setPrefWidth(16);
                pane.setPrefHeight(16);
                this.setGraphic(pane);
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }

    /**
     * 改修を表示するセル
     */
    private static class ItemLevelCell<T> extends TableCell<T, Integer> {

        @Override
        protected void updateItem(Integer lv, boolean empty) {
            super.updateItem(lv, empty);
            if (!empty) {
                this.setText(Optional.ofNullable(lv)
                        .filter(v -> v > 0)
                        .map(v -> Messages.getString("item.level", v)) //$NON-NLS-1$
                        .orElse(""));
                if (!this.getStyleClass().contains("level")) {
                    this.getStyleClass().add("level");
                }
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }

    /**
     * 艦娘を表示するセル
     */
    private static class ShipImageCell extends TableCell<DetailItem, Ship> {
        @Override
        protected void updateItem(Ship ship, boolean empty) {
            super.updateItem(ship, empty);
            if (!empty) {
                this.setGraphic(Tools.Controls.zoomImage(new ImageView(Ships.shipWithItemImage(ship))));
                if (ship != null) {
                    this.setText(Ships.toName(ship));
                } else {
                    this.setText("未装備");
                }
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }

    /**
     * (装備一覧)クリップボードにコピー
     */
    @FXML
    void copyType() {
        TableTool.selectionCopy(this.typeTable);
    }

    /**
     * (装備一覧)すべてを選択
     */
    @FXML
    void selectAllType() {
        TableTool.selectAll(this.typeTable);
    }

    /**
     * (装備一覧)CSVファイルとして保存
     */
    @FXML
    void storeType() {
        try {
            TableTool.store(this.typeTable, "所有装備一覧", this.getWindow());
        } catch (IOException e) {
            LoggerHolder.get().error("CSVファイルとして保存に失敗しました", e);
        }
    }

    /**
     * 制空権シミュレータv2(装備)
     */
    @FXML
    void kancolleFleetanalysis() {
        try {
            List<KancolleFleetanalysisItem> list = SlotItemCollection.get().getSlotitemMap().values().stream()
                    .filter(item -> item.getLocked())
                    .map(KancolleFleetanalysisItem::toItem)
                    .sorted(Comparator.comparing(KancolleFleetanalysisItem::getId)
                            .thenComparing(Comparator.comparing(KancolleFleetanalysisItem::getLv)))
                    .collect(Collectors.toList());
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(list);

            ClipboardContent content = new ClipboardContent();
            content.putString(input);
            Clipboard.getSystemClipboard().setContent(content);
        } catch (Exception e) {
            LoggerHolder.get().error("制空権シミュレータv2形式でクリップボードコピーに失敗しました[ロック装備]", e);
        }
    }

    /**
     * (装備一覧)テーブル列の表示・非表示の設定
     */
    @FXML
    void columnVisibleType() {
        try {
            TableTool.showVisibleSetting(this.typeTable, this.getClass().toString() + "#" + "typeTable",
                    this.getWindow());
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * (所持)クリップボードにコピー
     */
    @FXML
    void copyDetail() {
        TableTool.selectionCopy(this.detailTable);
    }

    /**
     * (所持)すべてを選択
     */
    @FXML
    void selectAllDetail() {
        TableTool.selectAll(this.detailTable);
    }

    /**
     * (所持)テーブル列の表示・非表示の設定
     */
    @FXML
    void columnVisibleDetail() {
        try {
            TableTool.showVisibleSetting(this.detailTable, this.getClass().toString() + "#" + "detailTable",
                    this.getWindow());
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    private void loadConfig() {
        this.disableFilterUpdate = true;
        try {
            Optional.ofNullable(AppItemTableConfig.get()).map(AppItemTableConfig::getItemTabConfig).ifPresent(config -> {
                this.filter.setExpanded(config.isFilterExpanded());
                this.textFilter.setSelected(config.isTextFilterEnabled());
                Optional.ofNullable(config.getTextFilter()).ifPresent(this.textValue::setValue);
                Optional.ofNullable(config.getParameterFilters()).ifPresent((list) -> {
                    for (int i = 0; i < Math.min(list.size(), this.parameterFilters.size()); i++) {
                        this.parameterFilters.get(i).loadConfig(list.get(i));
                    }
                });
                this.typeFilter.setSelected(config.isTypeFilterEnabled());
                this.typeFilterTitledPane.setExpanded(config.isTypeFilterExpanded());
                Optional.ofNullable(config.getSelectedTypes()).ifPresent(types -> {
                    types.stream().map(this.typeFilters::get).forEach(checkbox -> checkbox.setSelected(true));
                });
            });
        } finally {
            this.disableFilterUpdate = false;
        }
    }
    
    private void saveConfig() {
        if (this.disableFilterUpdate) {
            return;
        }
        AppItemTableConfig config = AppItemTableConfig.get();
        AppItemTableConfig.ItemTabConfig itemTabConfig = new AppItemTableConfig.ItemTabConfig();
        itemTabConfig.setFilterExpanded(this.filter.isExpanded());
        itemTabConfig.setTextFilterEnabled(this.textFilter.isSelected());
        Optional.ofNullable(this.textValue.getValue()).map(String::trim).filter(str -> !str.isEmpty()).ifPresent(itemTabConfig::setTextFilter);
        itemTabConfig.setTypeFilterEnabled(this.typeFilter.isSelected());
        itemTabConfig.setTypeFilterExpanded(this.typeFilterTitledPane.isExpanded());
        itemTabConfig.setSelectedTypes(getSelectedTypes());
        itemTabConfig.setParameterFilters(this.parameterFilters.stream().map(ParameterFilterPane::saveConfig).collect(Collectors.toList()));
        config.setItemTabConfig(itemTabConfig);
    }

    @Data
    private static class KancolleFleetanalysisItem {

        @JsonProperty("api_slotitem_id")
        private int id;

        @JsonProperty("api_level")
        private int lv;

        public static KancolleFleetanalysisItem toItem(SlotItem item) {
            KancolleFleetanalysisItem kfi = new KancolleFleetanalysisItem();
            kfi.id = item.getSlotitemId();
            kfi.lv = item.getLevel();
            return kfi;
        }
    }
}
