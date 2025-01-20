package logbook.internal.gui;

import javafx.stage.Stage;

/**
 * Window情報を確保するクラスです
 * 
 * JavaFx17対応で、トースター表示の際OwnerWindow情報が必須となったので、MainControllerのStage情報を保持します
 * 
 * TODO:アプリ表示領域内に表示されるため、アプリが動いている画面内に変更する対応が必要
 */
public class WindowHolder {
    private static WindowHolder instance;
    private Stage mainWindow;
    
    private WindowHolder() {
    }
    
    public static WindowHolder getInstance() {
        if (instance == null) {
            instance = new WindowHolder();
        }
        return instance;
    }
    
    public void setMainWindow(Stage window) {
        this.mainWindow = window;
    }

    public Stage getMainWindow() {
        return mainWindow;
    }
}
