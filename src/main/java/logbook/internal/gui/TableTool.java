package logbook.internal.gui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * TableViewに関係するメソッドを集めたクラス
 *
 */
class TableTool {

    /**
     * 行をヘッダ付きで文字列にします
     *
     * @param table テーブル
     * @param rows 行
     * @return ヘッダ付きの文字列
     */
    static <T> String toString(TableView<?> table, List<?> rows) {
        return Tools.Tables.toString(table, rows);
    }

    /**
     * 選択行をヘッダ付きで文字列にします
     *
     * @param table テーブル
     * @return ヘッダ付きの文字列
     */
    static String selectionToString(TableView<?> table) {
        return Tools.Tables.selectionToString(table);
    }

    /**
     * 選択行をヘッダ付きでクリップボードにコピーします
     *
     * @param table テーブル
     */
    static void selectionCopy(TableView<?> table) {
        Tools.Tables.selectionCopy(table);
    }

    /**
     * テーブルの行をすべて選択します
     *
     * @param table テーブル
     */
    static void selectAll(TableView<?> table) {
        Tools.Tables.selectAll(table);
    }

    /**
     * キーボードイベントのハンドラー(Ctrl+Cを実装)
     *
     * @param event キーボードイベント
     */
    static void defaultOnKeyPressedHandler(KeyEvent event) {
        Tools.Tables.defaultOnKeyPressedHandler(event);
    }

    /**
     * テーブルの内容をCSVファイルとして出力します
     *
     * @param table テーブル
     * @param title タイトル及びファイル名
     * @param own 親ウインドウ
     */
    static void store(TableView<?> table, String title, Window own) throws IOException {
        Tools.Tables.store(table, title, own);
    }

    /**
     * テーブル列の表示・非表示の設定を行う
     * @param table テーブル
     * @param key テーブルのキー名
     * @param window 親ウインドウ
     * @throws IOException 入出力例外が発生した場合
     */
    static void showVisibleSetting(TableView<?> table, String key, Stage window) throws IOException {
        Tools.Tables.showVisibleSetting(table, key, window);
    }

    /**
     * テーブル列の表示・非表示の設定を行う
     * @param table テーブル
     * @param key テーブルのキー名
     */
    static void setVisible(TableView<?> table, String key) {
        Tools.Tables.setVisible(table, key);
        Tools.Tables.setWidth(table, key);
        Tools.Tables.setSortOrder(table, key);
    }

    static <T> Callback<TableColumn<T, Integer>, TableCell<T, Integer>> getRowCountCellFactory() {
        return e -> {
            TableCell<T, Integer> cell = new TableCell<T, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        TableRow<?> currentRow = this.getTableRow();
                        this.setText(Integer.toString(currentRow.getIndex() + 1));
                    } else {
                        this.setText(null);
                    }
                }
            };
            return cell;
        };
    }
    
    static Callback<TableColumn<UseitemItem, Integer>, TableCell<UseitemItem, Integer>> createIntegerFormattedCellFactory() {
        final NumberFormat format = NumberFormat.getIntegerInstance();
        return new Callback<TableColumn<UseitemItem,Integer>, TableCell<UseitemItem,Integer>>() {
            @Override
            public TableCell<UseitemItem, Integer> call(TableColumn<UseitemItem, Integer> param) {
                return new TableCell<UseitemItem, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(format.format(item));
                        }
                    }
                };
            }
        };
    }
}
