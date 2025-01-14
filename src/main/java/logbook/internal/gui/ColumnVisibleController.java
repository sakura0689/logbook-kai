package logbook.internal.gui;

import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.WindowEvent;
import logbook.bean.AppConfig;
import logbook.internal.util.ToStringConverter;

/**
 * テーブル列の表示・非表示の設定ダイアログを表示する
 *
 */
public class ColumnVisibleController extends WindowController {

    /** テーブルのキー名 */
    private String key;

    /** リスト */
    @FXML
    private ListView<TableColumn<?, ?>> listView;

    @FXML
    void initialize() {
        this.listView.setCellFactory(
                CheckBoxListCell.forListView(t -> t.visibleProperty(),
                        ToStringConverter.of(Tools.Tables::getColumnName)));
    }

    /**
     * 全てを選択
     */
    @FXML
    void selectAll() {
        this.listView.getItems().forEach(e -> e.setVisible(true));
    }

    /**
     * 全てを非選択
     */
    @FXML
    void deselectAll() {
        this.listView.getItems().forEach(e -> e.setVisible(false));
    }

    /**
     * 幅をリセット
     */
    @FXML
    void resetWidth() {
        AppConfig.get()
                .getColumnWidthMap()
                .remove(this.key);
        AppConfig.get()
                .getColumnOrderMap()
                .remove(this.key);
        Tools.Controls.alert(AlertType.INFORMATION, "列幅・並び順をリセット", "列幅・並び順がリセットされました。\n再度ウインドウを開いたときに反映されます。",
                this.getWindow());
    }

    /**
     * リストにアイテムを設定する
     *
     * @param table テーブル
     * @param key テーブルのキー名
     */
    public void setData(TableView<?> table, String key) {
        this.key = key;
        this.listView.getItems().addAll(Tools.Tables.getColumns(table).collect(Collectors.toList()));
        // 閉じるときに設定を保存する
        this.getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
            // 非表示にした列のSet
            Set<String> setting = this.listView.getItems()
                    .stream()
                    .filter(c -> !c.isVisible())
                    .map(Tools.Tables::getColumnName)
                    .collect(Collectors.toSet());
            if (setting.isEmpty()) {
                AppConfig.get().getColumnVisibleMap().remove(key);
            } else {
                AppConfig.get().getColumnVisibleMap().put(key, setting);
            }
        });
    }
}
