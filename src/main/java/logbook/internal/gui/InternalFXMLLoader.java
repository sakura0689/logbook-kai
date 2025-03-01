package logbook.internal.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logbook.bean.AppConfig;
import logbook.core.LogBookCoreServices;

public final class InternalFXMLLoader {

    /** OSによる（今の所Macのみ特別扱い）デフォルトのフォントファミリー */
    private static final String DEFAULT_FONT;

    static {
        DEFAULT_FONT = System.getProperty("os.name").toLowerCase().startsWith("mac") ? "Hiragino Maru Gothic ProN" : "Meiryo UI";
    }

    public static FXMLLoader load(String name) throws IOException {
        URL url = LogBookCoreServices.getResource(name);
        return load(url);
    }

    public static FXMLLoader load(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.setClassLoader(LogBookCoreServices.getClassLoader());
        return loader;
    }

    public static Parent setGlobal(Parent root) {
        String fontSize = AppConfig.get().getFontSize();
        if (fontSize != null && !"default".equals(fontSize)) {
            URL url = LogBookCoreServices.getResource("logbook/gui/application_" + fontSize + ".css");
            if (url != null) {
                root.getStylesheets().add(url.toString());
            }
        }
        String font = Optional.ofNullable(AppConfig.get().getFontFamily()).filter(str -> str.trim().length() > 0).orElse(DEFAULT_FONT);
        root.setStyle("-fx-font-family: \""+ font + "\";");
        return root;
    }

    /**
     * ウインドウを開く
     *
     * @param name リソース
     * @param parent 親ウインドウ
     * @param title ウインドウタイトル
     * @throws IOException 入出力例外が発生した場合
     */
    static void showWindow(String name, Stage parent, String title) throws IOException {
        showWindow(name, parent, title, null, null);
    }

    /**
     * ウインドウを開く
     *
     * @param name リソース
     * @param parent 親ウインドウ
     * @param title ウインドウタイトル
     * @param controllerConsumer コントローラーを操作するConsumer
     * @param windowConsumer ウインドウを操作するConsumer
     * @throws IOException 入出力例外が発生した場合
     */
    static void showWindow(String name, Stage parent, String title, Consumer<WindowController> controllerConsumer,
            Consumer<Stage> windowConsumer) throws IOException {
        showWindow(name, parent, title, null, controllerConsumer, windowConsumer);
    }

    /**
     * ウインドウを開く
     *
     * @param name リソース
     * @param parent 親ウインドウ
     * @param title ウインドウタイトル
     * @param sceneFunction シーン・グラフを操作するFunction
     * @param controllerConsumer コントローラーを操作するConsumer
     * @param windowConsumer ウインドウを操作するConsumer
     * @throws IOException 入出力例外が発生した場合
     */
    static void showWindow(String name, Stage parent, String title, Function<Parent, Scene> sceneFunction,
            Consumer<WindowController> controllerConsumer,
            Consumer<Stage> windowConsumer) throws IOException {
        showWindow(name, parent, title, null, sceneFunction, controllerConsumer, windowConsumer);
    }

    /**
     * ウインドウを開く
     *
     * @param name リソース
     * @param parent 親ウインドウ
     * @param title ウインドウタイトル
     * @param subkey 同じクラスでウィンドウ位置の保存を分けたいときに使うキー
     * @param sceneFunction シーン・グラフを操作するFunction
     * @param controllerConsumer コントローラーを操作するConsumer
     * @param windowConsumer ウインドウを操作するConsumer
     * @throws IOException 入出力例外が発生した場合
     */
    static void showWindow(String name, Stage parent, String title, String subkey, Function<Parent, Scene> sceneFunction,
            Consumer<WindowController> controllerConsumer,
            Consumer<Stage> windowConsumer) throws IOException {

        FXMLLoader loader = load(name);
        Stage stage = new Stage();
        Parent root = setGlobal(loader.load());
        if (sceneFunction != null) {
            stage.setScene(sceneFunction.apply(root));
        } else {
            stage.setScene(new Scene(root));
        }

        WindowController controller = loader.getController();
        controller.initWindow(stage);

        if (windowConsumer != null) {
            windowConsumer.accept(stage);
        }
        if (controllerConsumer != null) {
            controllerConsumer.accept(controller);
        }

        stage.initOwner(parent);
        stage.setTitle(title);
        Tools.Windows.setIcon(stage);
        Tools.Windows.defaultCloseAction(controller, subkey);
        Tools.Windows.defaultOpenAction(controller, subkey);
        stage.show();
    }
}
