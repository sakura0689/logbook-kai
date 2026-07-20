package logbook.internal.gui;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logbook.bean.AppConfig;
import logbook.bean.Spritesmith;
import logbook.constants.SeaArea;
import logbook.internal.kancolle.SeaAreas;
import logbook.internal.logger.LoggerHolder;

/**
 * 作戦札編集コントローラー
 *
 */
public class SeaAreaEditController extends WindowController {

    @FXML
    private GridPane gridPane;

    @FXML
    private ImageView commonEventImage;

    private final List<TextField> nameFields = new ArrayList<>();
    private final List<TextField> imageNoFields = new ArrayList<>();
    private final List<ImageView> previewImages = new ArrayList<>();

    @FXML
    void initialize() {
        try {
            Path resourcesDir = Paths.get(AppConfig.get().getResourcesDir());

            // 1. common_event.pngを表示 (common_event.jsonを評価して数字をオーバーレイする)
            Path eventPngPath = resourcesDir.resolve(Paths.get("common", "common_event.png"));
            Path eventJsonPath = resourcesDir.resolve(Paths.get("common", "common_event.json"));
            if (Files.exists(eventPngPath)) {
                Image baseImage = new Image(eventPngPath.toUri().toString());
                if (Files.exists(eventJsonPath)) {
                    try (Reader reader = Files.newBufferedReader(eventJsonPath, StandardCharsets.UTF_8)) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        Spritesmith sprites = mapper.readValue(reader, Spritesmith.class);
                        if (sprites != null && sprites.getFrames() != null) {
                            double width = baseImage.getWidth();
                            double height = baseImage.getHeight();
                            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(width, height);
                            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

                            // 元の画像を描画
                            gc.drawImage(baseImage, 0, 0);

                            // フォントの設定
                            gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));

                            for (Map.Entry<String, Spritesmith.Frame> entry : sprites.getFrames().entrySet()) {
                                String key = entry.getKey();
                                Spritesmith.Frame frameObj = entry.getValue();
                                if (frameObj != null && frameObj.getFrame() != null) {
                                    Spritesmith.Rect rect = frameObj.getFrame();
                                    String numStr = key.replaceAll("[^0-9]", "");
                                    if (!numStr.isEmpty()) {
                                        // 右上に描画する
                                        double textX = rect.getX() + rect.getW() - 18;
                                        double textY = rect.getY() + 15;

                                        gc.setStroke(javafx.scene.paint.Color.BLACK);
                                        gc.setLineWidth(3);
                                        gc.strokeText(numStr, textX, textY);

                                        gc.setFill(javafx.scene.paint.Color.YELLOW);
                                        gc.fillText(numStr, textX, textY);
                                    }
                                }
                            }

                            javafx.scene.SnapshotParameters sp = new javafx.scene.SnapshotParameters();
                            sp.setFill(javafx.scene.paint.Color.TRANSPARENT);
                            Image processedImage = canvas.snapshot(sp, null);
                            this.commonEventImage.setImage(processedImage);
                        } else {
                            this.commonEventImage.setImage(baseImage);
                        }
                    } catch (Exception e) {
                        LoggerHolder.get().warn("common_event.jsonの解析またはオーバーレイ描画に失敗しました。元の画像を表示します。", e);
                        this.commonEventImage.setImage(baseImage);
                    }
                } else {
                    this.commonEventImage.setImage(baseImage);
                }
            }

            // 2. 既存のカスタムJSONがあればロードする
            Map<Integer, SeaAreaCustomEntry> customMap = new HashMap<>();
            Path customJsonPath = resourcesDir.resolve(Paths.get("common", "common_event", "common_event_custom.json"));
            if (Files.exists(customJsonPath)) {
                try (Reader reader = Files.newBufferedReader(customJsonPath, StandardCharsets.UTF_8)) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<SeaAreaCustomEntry> customEntries = mapper.readValue(reader, new TypeReference<List<SeaAreaCustomEntry>>() {});
                    if (customEntries != null) {
                        for (SeaAreaCustomEntry entry : customEntries) {
                            customMap.put(entry.getArea(), entry);
                        }
                    }
                } catch (Exception e) {
                    LoggerHolder.get().warn("common_event_custom.jsonの読み込みに失敗しました。デフォルト値を使用します。", e);
                }
            }

            // 3. GridPaneのヘッダーを作成
            this.gridPane.add(new Label("札"), 0, 0);
            this.gridPane.add(new Label("札名"), 1, 0);
            this.gridPane.add(new Label("画像No"), 2, 0);
            this.gridPane.add(new Label("画像"), 3, 0);

            // 4. エリア1〜14の編集行を構築
            for (int area = 1; area <= 14; area++) {
                final int currentArea = area;
                SeaArea defaultArea = SeaArea.fromArea(area);

                String initName = "";
                int initImageNo = 0;

                if (defaultArea != null) {
                    initName = defaultArea.getName();
                    initImageNo = defaultArea.getImageId();
                }

                // カスタムデータがあれば上書き
                SeaAreaCustomEntry customEntry = customMap.get(area);
                if (customEntry != null) {
                    initName = customEntry.getName();
                    initImageNo = customEntry.getImageNo();
                }

                // 札ラベル
                Label areaLabel = new Label("札 " + area);
                this.gridPane.add(areaLabel, 0, area);

                // 札名入力
                TextField nameField = new TextField(initName);
                nameField.setPrefWidth(200.0);
                this.gridPane.add(nameField, 1, area);
                this.nameFields.add(nameField);

                // 画像No入力
                TextField imageNoField = new TextField(Integer.toString(initImageNo));
                imageNoField.setPrefWidth(80.0);
                this.gridPane.add(imageNoField, 2, area);
                this.imageNoFields.add(imageNoField);

                // 画像プレビュー
                ImageView preview = new ImageView();
                preview.setPreserveRatio(true);
                this.gridPane.add(preview, 3, area);
                this.previewImages.add(preview);

                // プレビューの初期読み込み
                this.updatePreview(currentArea, imageNoField.getText(), preview);

                // リスナーの設定（画像Noが変更されたらプレビューを更新する）
                imageNoField.textProperty().addListener((ob, o, n) -> {
                    this.updatePreview(currentArea, n, preview);
                });
            }

        } catch (Exception e) {
            LoggerHolder.get().error("作戦札編集画面の初期化に失敗しました", e);
        }
    }

    private void updatePreview(int area, String imageNoStr, ImageView preview) {
        try {
            int imgNo = Integer.parseInt(imageNoStr.trim());
            Path resourcesDir = Paths.get(AppConfig.get().getResourcesDir());
            Path p = resourcesDir.resolve(Paths.get("common", "common_event", "common_event_" + imgNo + ".png"));
            Path eventPngPath = resourcesDir.resolve(Paths.get("common", "common_event.png"));

            if (Files.exists(p) && Files.exists(eventPngPath)) {
                // 更新日時の比較 (common_event.pngの更新日時より前のものは表示しない)
                java.nio.file.attribute.FileTime t1 = Files.getLastModifiedTime(eventPngPath);
                java.nio.file.attribute.FileTime t2 = Files.getLastModifiedTime(p);
                if (t2.compareTo(t1) < 0) {
                    preview.setImage(null);
                    return;
                }

                Image img = new Image(p.toUri().toString());
                preview.setImage(img);

                // 表示サイズを70%に縮小
                preview.setFitWidth(img.getWidth() * 0.7);
                preview.setFitHeight(img.getHeight() * 0.7);
                preview.setPreserveRatio(true);
                return;
            }
        } catch (NumberFormatException e) {
            // 数値でない場合はプレビューを表示しない
        } catch (Exception e) {
            LoggerHolder.get().warn("プレビュー画像の読み込みまたは更新日時の比較に失敗しました", e);
        }
        preview.setImage(null);
    }

    @FXML
    void save(ActionEvent event) {
        try {
            Path resourcesDir = Paths.get(AppConfig.get().getResourcesDir());
            Path dir = resourcesDir.resolve(Paths.get("common", "common_event"));

            // バリデーション
            List<SeaAreaCustomEntry> entries = new ArrayList<>();
            for (int i = 0; i < 14; i++) {
                int area = i + 1;
                String name = this.nameFields.get(i).getText();
                String imageNoStr = this.imageNoFields.get(i).getText().trim();

                int imageNo;
                try {
                    imageNo = Integer.parseInt(imageNoStr);
                    if (imageNo < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.initOwner(this.gridPane.getScene().getWindow());
                    alert.setTitle("入力エラー");
                    alert.setHeaderText("画像Noが正しくありません");
                    alert.setContentText("エリア " + area + " の画像Noには非負の整数を入力してください。");
                    alert.showAndWait();
                    return;
                }

                entries.add(new SeaAreaCustomEntry(area, name, imageNo));
            }

            // 保存先ディレクトリ作成
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path file = dir.resolve("common_event_custom.json");

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, entries);
            }

            // キャッシュを更新
            SeaAreas.reload();

            // 成功アラートを表示
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initOwner(this.gridPane.getScene().getWindow());
            alert.setTitle("保存成功");
            alert.setHeaderText(null);
            alert.setContentText("作戦札設定を保存しました。\n保存先: " + file.toAbsolutePath().toString());
            alert.showAndWait();

            // 画面を閉じる
            Stage stage = (Stage) this.gridPane.getScene().getWindow();
            stage.close();

        } catch (Exception ex) {
            LoggerHolder.get().error("作戦札設定の保存に失敗しました", ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(this.gridPane.getScene().getWindow());
            alert.setTitle("保存失敗");
            alert.setHeaderText("保存に失敗しました");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }



    public static class SeaAreaCustomEntry {
        private int area;
        private String name;
        private int imageNo;

        public SeaAreaCustomEntry() {
        }

        public SeaAreaCustomEntry(int area, String name, int imageNo) {
            this.area = area;
            this.name = name;
            this.imageNo = imageNo;
        }

        public int getArea() {
            return this.area;
        }

        public void setArea(int area) {
            this.area = area;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getImageNo() {
            return this.imageNo;
        }

        public void setImageNo(int imageNo) {
            this.imageNo = imageNo;
        }
    }
}
