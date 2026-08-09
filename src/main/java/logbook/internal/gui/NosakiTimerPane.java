package logbook.internal.gui;

import java.io.IOException;
import java.time.Duration;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import logbook.bean.AppCondition;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.TimeUtil;

/**
 * 母港給糧タイマー
 *
 */
public class NosakiTimerPane extends AnchorPane {

    @FXML
    private Label time;

    /**
     * 母港給糧タイマーペインのコンストラクタ
     */
    public NosakiTimerPane() {
        try {
            FXMLLoader loader = InternalFXMLLoader.load("logbook/gui/nosaki_timer.fxml");
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            LoggerHolder.get().error("FXMLのロードに失敗しました", e);
        }
    }

    @FXML
    void initialize() {
        try {
            this.update();
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * 画面を更新します
     */
    public void update() {
        long timer = AppCondition.get().getNosakiTimer();
        if (timer > 0) {
            this.time.setText(TimeUtil.toString(Duration.ofMillis(System.currentTimeMillis() - timer), ""));
        } else {
            this.time.setText("");
        }
    }
}
