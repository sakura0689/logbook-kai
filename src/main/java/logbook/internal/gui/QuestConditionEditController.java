package logbook.internal.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import logbook.bean.AppQuest;
import logbook.bean.AppQuestCollection;
import logbook.bean.QuestList.Quest;
import logbook.core.LogBookCoreServices;
import logbook.internal.logger.LoggerHolder;

/**
 * 任務達成条件編集コントローラー
 */
public class QuestConditionEditController extends WindowController {

    @FXML
    private ComboBox<AppQuest> questSelector;

    @FXML
    private Label questType;
    @FXML
    private ComboBox<String> resetType;
    @FXML
    private TextField yearlyResetMonth;

    @FXML
    private TextField filterArea;
    @FXML
    private VBox fleetConditionsBox;

    @FXML
    private VBox conditionsBox;

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

            // リストの表示数を20に設定
            this.questSelector.setVisibleRowCount(20);

            // カテゴリフィルタ (2, 8, 9)
            List<Integer> targetCategories = Arrays.asList(2, 8, 9);

            List<AppQuest> quests = AppQuestCollection.get().getQuest().values().stream()
                    .filter(q -> q.getQuest() != null)
                    .filter(q -> targetCategories.contains(q.getQuest().getCategory()))
                    .filter(q -> LogBookCoreServices.getQuestResource(q.getNo()) == null)
                    .collect(Collectors.toList());

            this.questSelector.setItems(FXCollections.observableArrayList(quests));

            // ResetType初期化
            this.resetType.setItems(FXCollections.observableArrayList(
                    "デイリー", "ウィークリー", "マンスリー", "単発", "クォータリー", "イヤリー"));

            // 任務選択時の動作
            this.questSelector.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> {
                if (n != null && n.getQuest() != null) {
                    this.setQuestData(n);
                }
            });

            // イヤリーの時のみ入力可
            this.yearlyResetMonth.disableProperty()
                    .bind(this.resetType.getSelectionModel().selectedItemProperty().isNotEqualTo("イヤリー"));

            // 海域入力時の動作
            this.filterArea.textProperty().addListener((ob, o, n) -> {
                this.updateConditionsArea(n);
            });

        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    private void setQuestData(AppQuest appQuest) {
        Quest quest = appQuest.getQuest();
        if (quest != null) {
            // Type
            this.questType.setText(this.getCategoryName(quest.getCategory()));

            // ResetType (推定)
            String type = "単発";
            switch (quest.getType()) {
                case 1:
                    type = "デイリー";
                    break;
                case 2:
                    type = "ウィークリー";
                    break;
                case 3:
                    type = "マンスリー";
                    break;
                case 4:
                    type = "単発";
                    break;
                case 5:
                    type = "クォータリー";
                    break; // 他、だが一旦クォータリーとみなす
            }
            this.resetType.getSelectionModel().select(type);
            this.yearlyResetMonth.setText("");

            // Filter / Conditions Clear
            this.filterArea.setText("");
            this.fleetConditionsBox.getChildren().clear();
            this.conditionsBox.getChildren().clear();
        }
    }

    private void updateConditionsArea(String areaText) {
        this.conditionsBox.getChildren().clear();
        if (areaText == null || areaText.isEmpty()) {
            return;
        }

        String[] areas = areaText.split(",");
        for (String area : areas) {
            String val = area.trim();
            if (!val.isEmpty()) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Label label = new Label(val);
                label.setPrefWidth(50);

                CheckBox checkS = new CheckBox("S");
                CheckBox checkA = new CheckBox("A");
                CheckBox checkB = new CheckBox("B");

                TextField count = new TextField();
                count.setPrefWidth(50);
                Label countLabel = new Label("回");

                row.getChildren().addAll(label, checkS, checkA, checkB, count, countLabel);
                this.conditionsBox.getChildren().add(row);
            }
        }
    }

    private String getCategoryName(int category) {
        switch (category) {
            case 1:
                return "編成";
            case 2:
                return "出撃";
            case 3:
                return "演習";
            case 4:
                return "遠征";
            case 5:
                return "補給/入渠";
            case 6:
                return "工廠";
            case 7:
                return "改装";
            case 8:
                return "出撃(2)";
            case 9:
                return "出撃(3)";
            default:
                return "その他";
        }
    }

    /**
     * 艦隊条件追加
     *
     * @param e ActionEvent
     */
    @FXML
    void addFleetCondition(javafx.event.ActionEvent e) {
        HBox row = new HBox(3); // Spacing 3px
        row.setAlignment(Pos.CENTER_LEFT);

        int rowsInfo = this.fleetConditionsBox.getChildren().size();

        // 2行目以降は論理演算子を追加
        if (rowsInfo > 0) {
            ComboBox<String> logic = new ComboBox<>(FXCollections.observableArrayList("かつ", "または"));
            logic.getSelectionModel().select("かつ");
            logic.setPrefWidth(80);
            row.getChildren().add(logic);
        }

        // Type/Name Selector
        ComboBox<String> typeSelector = new ComboBox<>(FXCollections.observableArrayList("艦種", "艦名"));
        typeSelector.getSelectionModel().select("艦種"); // Default
        typeSelector.setPrefWidth(80);
        row.getChildren().add(typeSelector);

        TextField text = new TextField();
        text.setPromptText("例：駆逐艦,赤城");
        HBox.setHgrow(text, Priority.ALWAYS);

        // 入力補正リスナー
        text.textProperty().addListener((ob, o, n) -> {
            if (n != null && (n.contains("「") || n.contains("」"))) {
                // 「A」「B」 -> A,B
                String replaced = n.replaceAll("」\\s*「", ",")
                        .replaceAll("「", "")
                        .replaceAll("」", "");
                if (!replaced.equals(n)) {
                    text.setText(replaced);
                }
            }
        });

        row.getChildren().add(text);

        // 旗艦チェックボックス (1行目のみ)
        if (rowsInfo == 0) {
            CheckBox flagship = new CheckBox("旗艦");
            row.getChildren().add(flagship);
        }

        TextField count = new TextField();
        count.setPrefWidth(50);
        Label unit = new Label("隻");
        row.getChildren().addAll(count, unit);

        // 比較演算子
        ComboBox<String> compare = new ComboBox<>(FXCollections.observableArrayList("以上", "等しい", "以下"));
        compare.getSelectionModel().select("以上");
        compare.setPrefWidth(80);
        row.getChildren().add(compare);

        Button remove = new Button("削除");
        remove.setOnAction(ev -> {
            this.fleetConditionsBox.getChildren().remove(row);
            this.updateFleetDeleteButtons();
        });
        row.getChildren().add(remove);

        this.fleetConditionsBox.getChildren().add(row);

        this.updateFleetDeleteButtons();
    }

    /**
     * 削除ボタンの有効/無効制御
     * 末尾の行のみ有効にする
     */
    private void updateFleetDeleteButtons() {
        javafx.collections.ObservableList<javafx.scene.Node> children = this.fleetConditionsBox.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            javafx.scene.Node node = children.get(i);
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                // 削除ボタンは末尾にある前提
                if (!row.getChildren().isEmpty()) {
                    javafx.scene.Node lastChild = row.getChildren().get(row.getChildren().size() - 1);
                    if (lastChild instanceof Button) {
                        Button deleteBtn = (Button) lastChild;
                        // 最終行のみ有効
                        deleteBtn.setDisable(i != size - 1);
                    }
                }
            }
        }
    }

    @FXML
    void save(javafx.event.ActionEvent e) {
        AppQuest appQuest = this.questSelector.getSelectionModel().getSelectedItem();
        if (appQuest == null || appQuest.getQuest() == null) {
            return;
        }

        try {
            // Root Object
            logbook.bean.AppQuestCondition condition = new logbook.bean.AppQuestCondition();

            // Type & ResetType
            int category = appQuest.getQuest().getCategory();
            condition.setType(
                    category == 4 ? logbook.bean.AppQuestCondition.Type.遠征 : logbook.bean.AppQuestCondition.Type.出撃);
            condition.setResetType(this.resetType.getSelectionModel().getSelectedItem());

            if ("イヤリー".equals(condition.getResetType())) {
                try {
                    condition.setYearlyResetMonth(Integer.parseInt(this.yearlyResetMonth.getText()));
                } catch (NumberFormatException ex) {
                    // Ignore or set default?
                }
            }

            // Filter
            logbook.bean.AppQuestCondition.FilterCondition filter = new logbook.bean.AppQuestCondition.FilterCondition();
            String areaText = this.filterArea.getText();
            if (areaText != null && !areaText.isEmpty()) {
                filter.setArea(Arrays.stream(areaText.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet()));
            }

            // Fleet Conditions
            logbook.bean.AppQuestCondition.FleetCondition fleet = new logbook.bean.AppQuestCondition.FleetCondition();
            List<logbook.bean.AppQuestCondition.FleetCondition> subConditions = new java.util.ArrayList<>();

            String globalLogic = "AND"; // Default

            for (int i = 0; i < this.fleetConditionsBox.getChildren().size(); i++) {
                HBox row = (HBox) this.fleetConditionsBox.getChildren().get(i);

                logbook.bean.AppQuestCondition.FleetCondition sub = new logbook.bean.AppQuestCondition.FleetCondition();

                // Row components check
                // Row 1: [Type/Name], [Text], [Flagship]?, [Count], [Unit], [Compare], [Del]
                // Row 2+: [Logic], [Type/Name], [Text], [Count], [Unit], [Compare], [Del]

                int idx = 0;

                // Logic (Row 2+)
                if (i > 0) {
                    ComboBox<String> logicCombo = (ComboBox<String>) row.getChildren().get(idx++);
                    String l = logicCombo.getSelectionModel().getSelectedItem();
                    if (i == 1) {
                        globalLogic = "または".equals(l) ? "OR" : "AND";
                    }
                }

                // Type/Name Selector
                ComboBox<String> typeCombo = (ComboBox<String>) row.getChildren().get(idx++);
                String typeVal = typeCombo.getSelectionModel().getSelectedItem(); // "艦種" or "艦名"

                // Text
                TextField tf = (TextField) row.getChildren().get(idx++);
                String text = tf.getText();
                if (text != null && !text.isEmpty()) {
                    java.util.LinkedHashSet<String> valSet = Arrays.stream(text.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

                    if ("艦種".equals(typeVal)) {
                        sub.setStype(valSet);
                    } else {
                        sub.setName(valSet);
                    }
                }

                // Flagship (Row 1 only)
                if (i == 0) {
                    CheckBox fs = (CheckBox) row.getChildren().get(idx++);
                    if (fs.isSelected()) {
                        sub.setOrder(1);
                    }
                }

                // Count
                TextField cf = (TextField) row.getChildren().get(idx++);
                try {
                    sub.setCount(Integer.parseInt(cf.getText()));
                } catch (Exception ex) {
                    // ignore
                }

                // Unit Label
                idx++;

                // Compare Operator
                ComboBox<String> opCombo = (ComboBox<String>) row.getChildren().get(idx++);
                String op = opCombo.getSelectionModel().getSelectedItem();
                switch (op) {
                    case "以上":
                        sub.setOperator("GE");
                        break;
                    case "等しい":
                        sub.setOperator("EQ");
                        break;
                    case "以下":
                        sub.setOperator("LE");
                        break;
                }

                subConditions.add(sub);
            }

            if (!subConditions.isEmpty()) {
                fleet.setOperator(globalLogic);
                fleet.setConditions(subConditions);
                filter.setFleet(fleet);
            }
            condition.setFilter(filter);

            // Achievement Conditions
            for (javafx.scene.Node node : this.conditionsBox.getChildren()) {
                HBox row = (HBox) node;
                // Label(Area), S, A, B, Count, Label
                Label areaLabel = (Label) row.getChildren().get(0);
                CheckBox s = (CheckBox) row.getChildren().get(1);
                CheckBox a = (CheckBox) row.getChildren().get(2);
                CheckBox b = (CheckBox) row.getChildren().get(3);
                TextField countField = (TextField) row.getChildren().get(4);

                logbook.bean.AppQuestCondition.Condition cond = new logbook.bean.AppQuestCondition.Condition();
                cond.setBoss(true); // Default
                cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList(areaLabel.getText())));

                java.util.LinkedHashSet<String> ranks = new java.util.LinkedHashSet<>();
                if (s.isSelected())
                    ranks.add("S");
                if (a.isSelected())
                    ranks.add("A");
                if (b.isSelected())
                    ranks.add("B");
                if (!ranks.isEmpty()) {
                    cond.setRank(ranks);
                }

                try {
                    cond.setCount(Integer.parseInt(countField.getText()));
                } catch (Exception ex) {
                    cond.setCount(1);
                }

                condition.getConditions().add(cond);
            }

            // Save to JSON
            // customquest/[id].json
            java.nio.file.Path dir = java.nio.file.Paths.get("./customquest");
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }

            java.nio.file.Path file = dir.resolve(appQuest.getQuest().getNo() + ".json");

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

            // Mixins to ignore unneeded boolean fields
            mapper.addMixIn(logbook.bean.AppQuestCondition.FleetCondition.class, FleetConditionMixin.class);
            mapper.addMixIn(logbook.bean.AppQuestCondition.Condition.class, ConditionMixin.class);
            mapper.addMixIn(logbook.bean.AppQuestCondition.class, AppQuestConditionMixin.class);

            try (java.io.Writer writer = java.nio.file.Files.newBufferedWriter(file,
                    java.nio.charset.StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, condition);
            }

            // Show Alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.initOwner(this.questSelector.getScene().getWindow());
            alert.setTitle("保存成功");
            alert.setHeaderText(null);
            alert.setContentText("./customquest/" + appQuest.getQuest().getNo() + ".jsonを出力しました。");
            alert.showAndWait();

        } catch (Exception ex) {
            LoggerHolder.get().error("保存に失敗しました", ex);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.initOwner(this.questSelector.getScene().getWindow());
            alert.setTitle("保存失敗");
            alert.setHeaderText("保存に失敗しました");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    // Mixin classes for Jackson serialization control
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "difference" })
    abstract class FleetConditionMixin {
        java.util.LinkedHashSet<String> stype;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "start" })
    abstract class ConditionMixin {
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "collectStypeInternal" })
    abstract class AppQuestConditionMixin {
    }
}
