package logbook.internal.gui;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.WindowEvent;
import logbook.bean.UseitemMstCollection;
import logbook.internal.LoggerHolder;

/**
 * アイテム一覧のコントローラー
 *
 */
public class UseitemController extends WindowController {

    /** 持ってないアイテムも出す */
    @FXML
    private CheckBox includeEmpty;

    @FXML
    private TableView<UseitemItem> table;

    /** 行番号 */
    @FXML
    private TableColumn<UseitemItem, Integer> row;

    /** ID */
    @FXML
    private TableColumn<UseitemItem, Integer> id;

    /** 名前 */
    @FXML
    private TableColumn<UseitemItem, String> name;

    /** 個数 */
    @FXML
    private TableColumn<UseitemItem, Integer> count;

    /** 説明 */
    @FXML
    private TableColumn<UseitemItem, String> description;

    private ObservableList<UseitemItem> items = FXCollections.observableArrayList();

    private int itemsHashCode;

    private Timeline timeline;

    @FXML
    void initialize() {
        TableTool.setVisible(this.table, this.getClass().toString() + "#" + "table");

        // カラムとオブジェクトのバインド
        this.row.setCellFactory(TableTool.getRowCountCellFactory());
        this.id.setCellValueFactory(new PropertyValueFactory<>("id"));
        this.name.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.count.setCellValueFactory(new PropertyValueFactory<>("count"));
        this.count.setCellFactory(TableTool.createIntegerFormattedCellFactory());
        this.description.setCellValueFactory(new PropertyValueFactory<>("description"));

        SortedList<UseitemItem> sortedList = new SortedList<>(this.items);
        this.table.setItems(sortedList);
        sortedList.comparatorProperty().bind(this.table.comparatorProperty());
        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.table.setOnKeyPressed(TableTool::defaultOnKeyPressedHandler);

        this.update(null);
    }

    /**
     * 画面の更新
     *
     * @param e ActionEvent
     */
    @FXML
    void update(ActionEvent e) {
        List<UseitemItem> items = UseitemMstCollection.get().getUseitemMap().values().stream()
                .map(UseitemItem::toUseitemItem)
                .filter(this::filter)
                .sorted(Comparator.comparing(UseitemItem::getId))
                .collect(Collectors.toList());
        if (this.itemsHashCode != items.hashCode()) {
            this.items.clear();
            this.items.addAll(items);
            this.itemsHashCode = items.hashCode();
        }
    }

    /**
     * クリップボードにコピー
     */
    @FXML
    void copy() {
        TableTool.selectionCopy(this.table);
    }

    /**
     * すべてを選択
     */
    @FXML
    void selectAll() {
        TableTool.selectAll(this.table);
    }

    /**
     * テーブル列の表示・非表示の設定
     */
    @FXML
    void columnVisible() {
        try {
            TableTool.showVisibleSetting(this.table, this.getClass().toString() + "#" + "table",
                    this.getWindow());
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * フィルター
     * @param item アイテム
     * @return フィルタ結果
     */
    private boolean filter(UseitemItem item) {
        boolean result = item.getName() != null && item.getName().length() > 0;

        if (result && !this.includeEmpty.isSelected()) {
            result &= Optional.ofNullable(item.getCount()).filter(c -> c.intValue() > 0).orElse(0) > 0;
        }
        return result;
    }

    @Override
    protected void onWindowHidden(WindowEvent e) {
        if (this.timeline != null) {
            this.timeline.stop();
        }
    }
}
