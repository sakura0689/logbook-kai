package logbook.internal.gui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;
import logbook.bean.AppQuest;
import logbook.bean.AppQuestCollection;
import logbook.bean.ArsenalQuestSetting;
import logbook.bean.QuestList.Quest;
import logbook.internal.logger.LoggerHolder;

/**
 * 工廠任務受領設定編集コントローラー
 *
 */
public class ArsenalQuestConditionEditController extends WindowController {

    @FXML
    private ComboBox<AppQuest> questSelector;

    @FXML
    private CheckBox discard;

    @FXML
    private TextArea comment;

    @FXML
    void initialize() {
        try {
            this.questSelector.setConverter(new StringConverter<AppQuest>() {
                @Override
                public String toString(AppQuest object) {
                    if (object != null) {
                        Quest quest = object.getQuest();
                        if (quest != null) {
                            return String.format("%d: %s", quest.getNo(), quest.getTitle());
                        }
                    }
                    return null;
                }

                @Override
                public AppQuest fromString(String string) {
                    return null;
                }
            });

            // 工廠任務(Category=6, 11) を抽出
            List<AppQuest> quests = AppQuestCollection.get().getQuest().values().stream()
                    .filter(q -> q.getQuest() != null)
                    .filter(q -> {
                        int cat = q.getQuest().getCategory();
                        return cat == 6 || cat == 11;
                    })
                    .collect(Collectors.toList());

            this.questSelector.setItems(FXCollections.observableArrayList(quests));

            this.questSelector.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> {
                if (n != null) {
                    this.loadSetting(n);
                }
            });

        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    private void loadSetting(AppQuest quest) {
        Path path = this.getSettingPath(quest);
        if (Files.exists(path)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                ArsenalQuestSetting setting = mapper.readValue(path.toFile(), ArsenalQuestSetting.class);
                this.discard.setSelected(setting.isDiscard());
                this.comment.setText(setting.getComment());
            } catch (IOException e) {
                LoggerHolder.get().error("設定の読み込みに失敗しました", e);
            }
        } else {
            this.discard.setSelected(false);
            this.comment.setText("");
        }
    }

    @FXML
    void save(ActionEvent e) {
        AppQuest quest = this.questSelector.getSelectionModel().getSelectedItem();
        if (quest == null) {
            return;
        }

        ArsenalQuestSetting setting = new ArsenalQuestSetting();
        setting.setDiscard(this.discard.isSelected());
        setting.setComment(this.comment.getText());

        Path path = this.getSettingPath(quest);
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            try (java.io.Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, setting);
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initOwner(this.getWindow());
            alert.setTitle("保存成功");
            alert.setHeaderText(null);
            alert.setContentText("設定を保存しました。");
            alert.showAndWait();

        } catch (Exception ex) {
            LoggerHolder.get().error("保存に失敗しました", ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(this.getWindow());
            alert.setTitle("保存失敗");
            alert.setHeaderText("保存に失敗しました");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private Path getSettingPath(AppQuest quest) {
        return Paths.get("./arsenalquest/" + quest.getQuest().getNo() + ".json");
    }
}
