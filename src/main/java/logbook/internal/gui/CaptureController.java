package logbook.internal.gui;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.AppViewConfig;
import logbook.bean.AppViewConfig.CaptureConfig;
import logbook.bean.Mapinfo;
import logbook.internal.ThreadManager;
import logbook.internal.gui.ScreenCapture.ImageData;
import logbook.internal.logger.LoggerHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * キャプチャ
 *
 */
public class CaptureController extends WindowController {

    /** 設定 */
    @FXML
    private MenuButton config;

    /** 連写 */
    @FXML
    private CheckMenuItem cyclic;

    /** 動画 */
    @FXML
    private CheckMenuItem movie;

    /** ラジオボタングループ */
    @FXML
    private ToggleGroup cut;

    /** 画像形式jpeg */
    @FXML
    private RadioMenuItem jpeg;

    /** 画像形式png */
    @FXML
    private RadioMenuItem png;

    /** 画像形式ボタングループ */
    @FXML
    private ToggleGroup type;

    /** キャプチャ */
    @FXML
    private Button capture;

    /** 保存 */
    @FXML
    private Button save;

    @FXML
    private CheckBox direct;

    @FXML
    private CheckBox autoBattleCapture;

    @FXML
    private Label message;

    @FXML
    private ScrollPane imageParent;

    @FXML
    private ImageView image;

    /** 画像リスト */
    private ObservableList<ImageData> images = FXCollections.observableArrayList();

    /** 画像プレビュー */
    private ObjectProperty<ImageData> preview = new SimpleObjectProperty<>();

    /** スクリーンショット */
    private ScreenCapture sc;

    /** 周期キャプチャ */
    private Timeline timeline = new Timeline();

    /** 動画キャプチャ ステータス */
    private boolean processRunning;

    /** 動画キャプチャ */
    private Process process;

    /** 直接保存先 */
    private Path directPath;

    /** 出撃連動動画キャプチャ用チェック */
    private Timeline autoBattleCaptureTimeline;

    @FXML
    void initialize() {
        ImageIO.setUseCache(false);
        this.image.fitWidthProperty().bind(this.imageParent.widthProperty());
        this.image.fitHeightProperty().bind(this.imageParent.heightProperty());
        this.preview.addListener(this::viewImage);
        this.direct.selectedProperty().addListener((ov, o, n) -> {
            if (n) {
                DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle("キャプチャの保存先");
                // 覚えた保存先をセット
                File initDir = Optional.ofNullable(AppConfig.get().getCaptureDir())
                        .map(File::new)
                        .filter(File::isDirectory)
                        .orElse(null);
                if (initDir != null) {
                    dc.setInitialDirectory(initDir);
                }
                File file = dc.showDialog(this.getWindow());
                if (file != null) {
                    this.directPath = file.toPath();
                    // 保存先を覚える
                    AppConfig.get().setCaptureDir(file.getAbsolutePath());
                } else {
                    this.direct.setSelected(false);

                    if (this.movie.isSelected()) {
                        this.movie.setSelected(false);
                        this.setCatureButtonState(ButtonState.CAPTURE);
                    }
                }
            } else {
                if (this.movie.isSelected()) {
                    this.movie.setSelected(false);
                    this.setCatureButtonState(ButtonState.CAPTURE);
                }
            }
        });
        this.jpeg.setSelected("jpg".equals(AppConfig.get().getCaptureFormat()));
        this.png.setSelected("png".equals(AppConfig.get().getCaptureFormat()));
        this.autoBattleCapture.selectedProperty().addListener(this::toggleAutoBattleCapture);
    }

    private void toggleAutoBattleCapture(ObservableValue<? extends Boolean> ob, Boolean prev, Boolean value) {
        if (value) {
            if (this.autoBattleCaptureTimeline == null) {
                // 本来は observer pattern を使いたいところだが Java 11 になってからにする
                this.mapInfoLastModified = Mapinfo.get().getLastModified();
                this.autoBattleCaptureTimeline = new Timeline();
                this.autoBattleCaptureTimeline.setCycleCount(Timeline.INDEFINITE);
                this.autoBattleCaptureTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3), this::checkBattleStartEnd));
                this.autoBattleCaptureTimeline.play();
            }
        } else {
            if (this.autoBattleCaptureTimeline != null) {
                this.autoBattleCaptureTimeline.stop();
                this.autoBattleCaptureTimeline = null;
            }
        }
    }

    private long mapInfoLastModified;
    private boolean shouldBeInBattle;

    private void checkBattleStartEnd(ActionEvent event) {
        boolean inBattle = this.shouldBeInBattle;
        if (this.shouldBeInBattle) {
            if (AppCondition.get().isMapStart()) {
                // 出撃した
                this.mapInfoLastModified = -1;
            } else if (System.currentTimeMillis() - this.mapInfoLastModified > 90000) {
                // 戦闘終了、または海域を開いたけど1分半以内に出撃しなかったので停止
                inBattle = false;
                this.mapInfoLastModified = Mapinfo.get().getLastModified();
            }
        } else {
            if (this.mapInfoLastModified != Mapinfo.get().getLastModified()) {
                // 海域を開いたので出撃になるかもしれない
                this.mapInfoLastModified = Mapinfo.get().getLastModified();
                inBattle = true;
            } else if (AppCondition.get().isMapStart()) {
                // 出撃後にオンにしたなど
                inBattle = true;
            }
        }
        if (this.shouldBeInBattle ^ inBattle) {
            // 状況に変化があった
            this.shouldBeInBattle = inBattle;
            // もし手動で動画を開始・停止されていたら何もしない
            if (this.processRunning ^ this.shouldBeInBattle) {
                capture(event);
            }
        }
    }
    
    @FXML
    void cutNone(ActionEvent event) {
        this.sc.setCutRect(ScreenCapture.CutType.NONE.getAngle());
    }

    @FXML
    void cutUnit(ActionEvent event) {
        this.sc.setCutRect(ScreenCapture.CutType.UNIT.getAngle());
    }

    @FXML
    void cutUnitWithoutShip(ActionEvent event) {
        this.sc.setCutRect(ScreenCapture.CutType.UNIT_WITHOUT_SHIP.getAngle());
    }

    @FXML
    void detect(ActionEvent event) {
        this.detectAction();
    }

    @FXML
    void detectManual(ActionEvent event) {
        this.detectManualAction();
    }

    @FXML
    void input(ActionEvent event) {
        if (this.sc != null) {
            Rectangle rectangle = this.sc.getRectangle();
            RectangleBean bean = new RectangleBean(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            new PropertyDialog<>(this.getWindow(), bean, "範囲を編集").showAndWait();
            rectangle.x = bean.getX();
            rectangle.y = bean.getY();
            rectangle.width = bean.getWidth();
            rectangle.height = bean.getHeight();
            this.setBounds(this.sc.getRobot(), rectangle);
        } else {
            Tools.Controls.alert(AlertType.INFORMATION, "範囲を編集", "範囲を編集するには自動または手動で範囲を設定してください", this.getWindow());
        }
    }

    @FXML
    void cyclic(ActionEvent event) {
        // キャプチャ中であれば止める
        this.stopTimeLine();
        // 動画モード解除
        this.movie.setSelected(false);
        this.autoBattleCapture.setSelected(false);
        this.autoBattleCapture.setDisable(true);

        if (this.cyclic.isSelected()) {
            // キャプチャボタンテキストの変更
            this.setCatureButtonState(ButtonState.START);
        } else {
            // キャプチャボタンテキストの変更
            this.setCatureButtonState(ButtonState.CAPTURE);
        }
    }

    @FXML
    void movie(ActionEvent event) {
        // キャプチャ中であれば止める
        this.stopTimeLine();
        // 連写モード解除
        this.cyclic.setSelected(false);

        if ((AppConfig.get().getFfmpegPath() == null || AppConfig.get().getFfmpegPath().isEmpty())
                || (AppConfig.get().getFfmpegArgs() == null || AppConfig.get().getFfmpegArgs().isEmpty())
                || (AppConfig.get().getFfmpegExt() == null || AppConfig.get().getFfmpegExt().isEmpty())) {
            Tools.Controls.alert(AlertType.INFORMATION, "設定が必要です", "[設定]メニューの[キャプチャ]タブから"
                    + "FFmpegパスおよび引数を設定してください。", this.getWindow());
            this.movie.setSelected(false);
        }
        if (this.movie.isSelected()) {
            // キャプチャボタンテキストの変更
            this.setCatureButtonState(ButtonState.START);
            // 直接保存に設定
            this.direct.setSelected(true);
            this.autoBattleCapture.setDisable(false);
        } else {
            // キャプチャボタンテキストの変更
            this.setCatureButtonState(ButtonState.CAPTURE);
            this.autoBattleCapture.setDisable(true);
            this.autoBattleCapture.setSelected(false);
        }
    }

    @FXML
    void capture(ActionEvent event) {
        boolean running = this.timeline.getStatus() == Status.RUNNING;
        if (running) {
            this.stopTimeLine();
        }
        if (this.processRunning) {
            this.stopProcess();
        }
        if (this.cyclic.isSelected()) {
            // 動画撮影中ではない
            this.processRunning = false;

            // 周期キャプチャの場合
            if (running) {
                // キャプチャボタンテキストの変更
                this.setCatureButtonState(ButtonState.START);
            } else {
                // キャプチャ中で無ければ開始する
                this.timeline.setCycleCount(Animation.INDEFINITE);
                this.timeline.getKeyFrames().clear();
                this.timeline.getKeyFrames()
                        .add(new KeyFrame(javafx.util.Duration.millis(100),
                                this::captureAction));
                this.timeline.play();
                // キャプチャボタンテキストの変更
                this.setCatureButtonState(ButtonState.STOP);
            }
        } else if (this.movie.isSelected()) {
            if (this.processRunning) {
                // キャプチャボタンテキストの変更
                this.setCatureButtonState(ButtonState.START);
                this.processRunning = false;
            } else {
                // キャプチャ中で無ければ開始する
                this.startProcess();
                this.processRunning = true;
                // キャプチャボタンテキストの変更
                this.setCatureButtonState(ButtonState.STOP);
            }
        } else {
            // 動画撮影中ではない
            this.processRunning = false;

            this.captureAction(event);
        }
    }

    @FXML
    void save(ActionEvent event) {
        try {
            InternalFXMLLoader.showWindow("logbook/gui/capturesave.fxml", this.getWindow(), "キャプチャの保存", controller -> {
                ((CaptureSaveController) controller).setItems(this.images);
            }, null);
        } catch (Exception ex) {
            LoggerHolder.get().error("キャプチャの保存に失敗しました", ex);
        }
    }

    @FXML
    void setJpeg(ActionEvent event) {
        this.sc.setType("jpg");
        AppConfig.get().setCaptureFormat("jpg");
    }

    @FXML
    void setPng(ActionEvent event) {
        this.sc.setType("png");
        AppConfig.get().setCaptureFormat("png");
    }

    @Override
    public void setWindow(Stage window) {
        super.setWindow(window);
        this.detectAction();
        if (this.sc == null) {
            Optional.ofNullable(AppViewConfig.get().getCaptureConfig())
                .map(CaptureConfig::getBounds)
                .ifPresent(bounds -> {
                    // 前の状態を復元する
                    GraphicsConfiguration gc = this.currentGraphicsConfiguration();
                    try {
                        Robot robot = new Robot(gc.getDevice());
                        Rectangle rect = new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                        setBounds(robot, rect);
                    } catch (AWTException e) {
                        // could not restored but just ignore
                    }
                });
        }
    }

    /**
     * 座標取得アクション
     */
    private void detectAction() {
        try {
            GraphicsConfiguration gc = this.currentGraphicsConfiguration();
            Robot robot = new Robot(gc.getDevice());
            BufferedImage image = robot.createScreenCapture(gc.getBounds());
            Rectangle relative = ScreenCapture.detectGameScreen(image);
            Rectangle screenBounds = gc.getBounds();
            this.setBounds(robot, relative, screenBounds);
        } catch (Exception e) {
            LoggerHolder.get().error("座標取得に失敗しました", e);
        }
    }

    private Point2D start;

    private Point2D end;

    /**
     * 座標取得アクション
     */
    private void detectManualAction() {
        try {
            GraphicsConfiguration gcnf = this.currentGraphicsConfiguration();
            Robot robot = new Robot(gcnf.getDevice());

            BufferedImage bufferedImage = robot.createScreenCapture(gcnf.getBounds());

            WritableImage image = SwingFXUtils.toFXImage(bufferedImage, null);

            Stage stage = new Stage();
            Group root = new Group();
            Canvas canvas = new Canvas();
            canvas.widthProperty().bind(stage.widthProperty());
            canvas.heightProperty().bind(stage.heightProperty());
            canvas.setCursor(Cursor.CROSSHAIR);

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.setLineDashes(5, 5);

            // ドラッグの開始
            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                this.start = new Point2D(e.getX(), e.getY());
            });
            // ドラッグ中
            canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                Point2D now = new Point2D(e.getX(), e.getY());

                double x = Math.min(this.start.getX(), now.getX()) + 0.5;
                double y = Math.min(this.start.getY(), now.getY()) + 0.5;
                double w = Math.abs(this.start.getX() - now.getX());
                double h = Math.abs(this.start.getY() - now.getY());

                gc.strokeRect(x, y, w, h);
            });
            // ドラッグの終了
            canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
                this.end = new Point2D(e.getX(), e.getY());
                if (!this.start.equals(this.end)) {

                    Optional<ButtonType> buttonType = Tools.Controls.alert(Alert.AlertType.CONFIRMATION,
                            "矩形選択",
                            "この範囲でよろしいですか？",
                            stage);
                    if (buttonType.orElse(null) == ButtonType.OK) {
                        int x = (int) Math.min(this.start.getX(), this.end.getX());
                        int y = (int) Math.min(this.start.getY(), this.end.getY());
                        int w = (int) Math.abs(this.start.getX() - this.end.getX());
                        int h = (int) Math.abs(this.start.getY() - this.end.getY());

                        Rectangle tmp = getTrimSize(bufferedImage.getSubimage(x, y, w, h));
                        Rectangle relative = new Rectangle(
                                (int) (x + tmp.getX()),
                                (int) (y + tmp.getY()),
                                (int) tmp.getWidth(),
                                (int) tmp.getHeight());

                        Rectangle screenBounds = gcnf.getBounds();

                        this.setBounds(robot, relative, screenBounds);

                        stage.setFullScreen(false);
                    }
                }
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            });
            root.getChildren().addAll(new ImageView(image), canvas);

            stage.setScene(new Scene(root));
            stage.setX(gcnf.getBounds().getX());
            stage.setY(gcnf.getBounds().getY());
            stage.setTitle("座標取得");
            stage.setFullScreenExitHint("キャプチャする領域をマウスでドラッグして下さい。 [Esc]キーでキャンセル");
            stage.setFullScreen(true);
            stage.fullScreenProperty().addListener((ov, o, n) -> {
                if (!n)
                    stage.close();
            });
            stage.setAlwaysOnTop(true);
            stage.show();
        } catch (Exception e) {
            LoggerHolder.get().error("座標取得に失敗しました", e);
        }
    }

    /**
     * キャプチャアクション
     */
    private void captureAction(ActionEvent event) {
        try {
            if (this.sc != null) {
                if (this.jpeg.isSelected())
                    this.sc.setType("jpg");
                if (this.png.isSelected())
                    this.sc.setType("png");

                boolean isDirect = this.direct.isSelected() && this.directPath != null;
                if (isDirect) {
                    this.sc.captureDirect(this.directPath);
                } else {
                    this.sc.capture();
                }
            }
        } catch (Exception e) {
            LoggerHolder.get().error("キャプチャに失敗しました", e);
        }
    }

    /**
     * ウインドウを閉じる時のアクション
     *
     * @param event WindowEvent
     */
    @Override
    protected void onWindowHidden(WindowEvent e) {
        this.images.clear();
        this.timeline.stop();
        Optional.ofNullable(this.autoBattleCaptureTimeline).ifPresent(Timeline::stop);
        this.stopProcess();
    }

    /**
     * キャプチャプレビュー
     *
     * @param ov 値が変更されたObservableValue
     * @param o 古い値
     * @param n 新しい値
     */
    private void viewImage(ObservableValue<? extends ImageData> ov, ImageData o, ImageData n) {
        ImageData image = this.preview.getValue();
        if (image != null) {
            this.image.setImage(new Image(new ByteArrayInputStream(image.getImage())));
        }
    }

    /**
     * タイムラインを停止
     */
    private void stopTimeLine() {
        if (this.timeline.getStatus() == Status.RUNNING) {
            this.timeline.stop();
        }
    }

    /**
     * プロセス開始
     */
    private void startProcess() {
        Rectangle rectangle = this.sc.getRectangle();
        Path to = this.directPath.resolve(CaptureSaveController.DATE_FORMAT.format(ZonedDateTime.now())
                + "." + AppConfig.get().getFfmpegExt());

        Map<String, String> param = new HashMap<>();
        param.put("{x}", String.valueOf((int) rectangle.getX()));
        param.put("{y}", String.valueOf((int) rectangle.getY()));
        param.put("{width}", String.valueOf((int) rectangle.getWidth()));
        param.put("{height}", String.valueOf((int) rectangle.getHeight()));
        param.put("{path}", to.toAbsolutePath().toString());

        List<String> args = new ArrayList<>();
        args.add(AppConfig.get().getFfmpegPath());
        Arrays.stream(AppConfig.get().getFfmpegArgs().split("\n"))
                .flatMap(str -> Arrays.stream(str.split(" ")))
                .map(str -> {
                    String r = str;
                    for (Entry<String, String> entry : param.entrySet()) {
                        r = r.replace(entry.getKey(), entry.getValue());
                    }
                    return r;
                })
                .forEach(args::add);
        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            this.process = pb.start();
        } catch (Exception e) {
            this.stopProcess();
            Tools.Controls.alert(AlertType.ERROR, "動画撮影に失敗しました", "設定が誤っている可能性があります。\n引数:" + args.toString(), e,
                    this.getWindow());
        }
    }

    /**
     * プロセスを停止
     */
    private void stopProcess() {
        if (this.process != null) {
            Process process = this.process;
            ThreadManager.getExecutorService().execute(() -> {
                if (process.isAlive()) {
                    PrintWriter pw = new PrintWriter(process.getOutputStream(), true);
                    pw.println("q");
                    try {
                        process.waitFor(2, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        // NOP
                    } finally {
                        process.destroy();
                    }
                }
            });
        }
    }

    private void setCatureButtonState(ButtonState state) {
        for (ButtonState val : ButtonState.values()) {
            this.capture.getStyleClass().remove(val.getClassName());
        }
        this.capture.setText(state.getName());
        this.capture.getStyleClass().add(state.getClassName());
    }

    private static Rectangle getTrimSize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int startwidth = width / 2;
        int startheightTop = (height / 3) * 2;
        int startheightButton = height / 3;

        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;

        int color = image.getRGB(0, 0);

        // 左トリム(上)
        for (int i = 0; i < width; i++) {
            if (image.getRGB(i, startheightTop) != color) {
                x = i;
                break;
            }
        }
        // 左トリム(下)
        for (int i = 0; i < width; i++) {
            if (image.getRGB(i, startheightButton) != color) {
                x = Math.min(x, i);
                break;
            }
        }
        // 上トリム
        for (int i = 0; i < height; i++) {
            if (image.getRGB(startwidth, i) != color) {
                y = i;
                break;
            }
        }
        // 右トリム(上)
        for (int i = width - 1; i >= 0; i--) {
            if (image.getRGB(i, startheightTop) != color) {
                w = (i - x) + 1;
                break;
            }
        }
        // 右トリム(下)
        for (int i = width - 1; i >= 0; i--) {
            if (image.getRGB(i, startheightButton) != color) {
                w = Math.max(w, (i - x) + 1);
                break;
            }
        }
        // 下トリム
        for (int i = height - 1; i >= 0; i--) {
            if (image.getRGB(startwidth, i) != color) {
                h = (i - y) + 1;
                break;
            }
        }

        if ((w == 0) || (h == 0)) {
            return new Rectangle(0, 0, image.getWidth(), image.getHeight());
        } else {
            return new Rectangle(x, y, w, h);
        }
    }

    private void setBounds(Robot robot, Rectangle relative, Rectangle screenBounds) {
        if (relative != null) {
            Rectangle fixed = new Rectangle(relative.x + screenBounds.x, relative.y + screenBounds.y,
                    relative.width, relative.height);
            this.setBounds(robot, fixed);
        } else {
            this.message.setText("座標未設定");
            this.capture.setDisable(true);
            this.config.setDisable(true);
            this.sc = null;
        }
    }

    private void setBounds(Robot robot, Rectangle fixed) {
        String text = "(" + (int) fixed.getMinX() + "," + (int) fixed.getMinY() + ")";
        this.message.setText(text);
        this.capture.setDisable(false);
        this.config.setDisable(false);
        this.sc = new ScreenCapture(robot, fixed);
        this.sc.setItems(this.images);
        this.sc.setCurrent(this.preview);
        
        CaptureConfig config = AppViewConfig.get().getCaptureConfig();
        if (config == null) {
            config = new CaptureConfig();
        }
        CaptureConfig.Bounds bounds = new CaptureConfig.Bounds();
        bounds.setX((int)fixed.getMinX());
        bounds.setY((int)fixed.getMinY());
        bounds.setWidth((int)fixed.getWidth());
        bounds.setHeight((int)fixed.getHeight());
        config.setBounds(bounds);
        AppViewConfig.get().setCaptureConfig(config);
    }

    private GraphicsConfiguration currentGraphicsConfiguration() {
        Window window = this.getWindow();
        int x = (int) window.getX();
        int y = (int) window.getY();
        return ScreenCapture.detectScreenDevice(x, y);
    }

    private static enum ButtonState {

        CAPTURE("キャプチャ", "start"),
        START("開始", "start"),
        STOP("停止", "stop");

        @Getter
        private String name;

        @Getter
        private String className;

        private ButtonState(String name, String className) {
            this.name = name;
            this.className = className;
        }
    }

    @Data
    @AllArgsConstructor
    public static class RectangleBean {

        private int x;

        private int y;

        private int width;

        private int height;
    }
}
