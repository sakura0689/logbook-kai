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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;
import logbook.constants.StatisticsShipTypeGroup;
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
            List<Integer> targetCategories = Arrays.asList(2, 8, 9, 10);

            List<AppQuest> quests = AppQuestCollection.get().getQuest().values().stream()
                    .filter(q -> q.getQuest() != null)
                    .filter(q -> targetCategories.contains(q.getQuest().getCategory()))
                    .filter(q -> LogBookCoreServices.getQuestResource(q.getNo(), true) == null) // 恒常任務
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

            // Load existing custom conditions
            this.loadQuestData(quest);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadQuestData(Quest quest) {
        java.nio.file.Path file = java.nio.file.Paths.get("./customquest/" + quest.getNo() + ".json");
        if (!java.nio.file.Files.exists(file)) {
            return;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);

            // Register MixIns to safely ignore unknown properties or handle specific fields
            // if necessary
            // Note: The MixIns defined in this class are abstract and used for writing.
            // For reading, standard mapping should suffice if the JSON matches the bean
            // structure.

            logbook.bean.AppQuestCondition condition = mapper.readValue(file.toFile(),
                    logbook.bean.AppQuestCondition.class);

            if (condition != null) {
                // ResetType
                if (condition.getResetType() != null) {
                    this.resetType.getSelectionModel().select(condition.getResetType());
                }
                if (condition.getYearlyResetMonth() != null) {
                    this.yearlyResetMonth.setText(String.valueOf(condition.getYearlyResetMonth()));
                }

                // Filter Area
                // Note: Reconstruct Filter Text from Achievement Conditions to support 7-2-1
                // etc.
                // The logic below will handle setting filterArea.setText handles the simple
                // case if no achievement conditions exist.

                // Reconstruct Filter Text from Achievement Conditions to support 7-2-1 etc.
                if (condition.getConditions() != null) {
                    List<String> areas = new java.util.ArrayList<>();
                    for (logbook.bean.AppQuestCondition.Condition cond : condition.getConditions()) {
                        if (cond.getArea() != null && !cond.getArea().isEmpty()) {
                            String area = cond.getArea().iterator().next();
                            if ("7-2".equals(area)) {
                                if ("G".equals(cond.getCell())) {
                                    area = "7-2-1";
                                } else if ("M".equals(cond.getCell())) {
                                    area = "7-2-2";
                                }
                            } else if ("7-3".equals(area)) {
                                if ("E".equals(cond.getCell())) {
                                    area = "7-3-1";
                                } else if ("P".equals(cond.getCell())) {
                                    area = "7-3-2";
                                }
                            } else if ("7-5".equals(area)) {
                                if ("K".equals(cond.getCell())) {
                                    area = "7-5-1";
                                } else if ("Q".equals(cond.getCell())) {
                                    area = "7-5-2";
                                } else if ("T".equals(cond.getCell())) {
                                    area = "7-5-3";
                                }
                            }
                            areas.add(area);
                        }
                    }
                    String joined = String.join(",", areas);
                    this.filterArea.setText(joined);
                }

                // If filterArea is empty but filter.area exists (no conditions case?)
                if (this.filterArea.getText().isEmpty() && condition.getFilter() != null
                        && condition.getFilter().getArea() != null) {
                    this.filterArea.setText(String.join(",", condition.getFilter().getArea()));
                }

                // Fleet Conditions
                if (condition.getFilter() != null && condition.getFilter().getFleet() != null) {
                    logbook.bean.AppQuestCondition.FleetCondition fleet = condition.getFilter().getFleet();
                    String globalOp = fleet.getOperator(); // AND or OR

                    if (fleet.getConditions() != null) {
                        for (logbook.bean.AppQuestCondition.FleetCondition sub : fleet.getConditions()) {
                            this.addFleetCondition(null);
                            HBox row = (HBox) this.fleetConditionsBox.getChildren()
                                    .get(this.fleetConditionsBox.getChildren().size() - 1);

                            int idx = 0;
                            // Logic (Row 2+)
                            if (this.fleetConditionsBox.getChildren().size() > 1) {
                                ComboBox<String> logicCombo = (ComboBox<String>) row.getChildren().get(idx++);
                                logicCombo.getSelectionModel().select("OR".equals(globalOp) ? "または" : "かつ");
                            }

                            // Type/Name
                            ComboBox<String> typeCombo = (ComboBox<String>) row.getChildren().get(idx++);
                            boolean isStype = sub.getStype() != null && !sub.getStype().isEmpty();
                            typeCombo.getSelectionModel().select(isStype ? "艦種" : "艦名");

                            // Text
                            TextField tf = (TextField) row.getChildren().get(idx++);
                            if (isStype) {
                                tf.setText(String.join(",", sub.getStype()));
                            } else if (sub.getName() != null) {
                                tf.setText(String.join(",", sub.getName()));
                            }

                            // Flagship / Second Ship
                            if (this.fleetConditionsBox.getChildren().size() == 1) {
                                CheckBox fs = (CheckBox) row.getChildren().get(idx++);
                                fs.setSelected(sub.getOrder() != null && sub.getOrder() == 1);
                            } else if (this.fleetConditionsBox.getChildren().size() == 2) {
                                // 2行目で、かつ要素数が足りてる(チェックボックスがある)場合
                                // Flagshipチェックボックスを追加するかどうかの判定は addFleetCondition で行われているので
                                // ここでは単に取得してセットする
                                if (idx < row.getChildren().size() && row.getChildren().get(idx) instanceof CheckBox) {
                                    CheckBox ss = (CheckBox) row.getChildren().get(idx++);
                                    ss.setSelected(sub.getOrder() != null && sub.getOrder() == 2);
                                }
                            }

                            // Count
                            TextField cf = (TextField) row.getChildren().get(idx++);
                            if (sub.getCount() != null) {
                                cf.setText(String.valueOf(sub.getCount()));
                            }
                            idx++; // Unit

                            // Compare
                            ComboBox<String> opCombo = (ComboBox<String>) row.getChildren().get(idx++);
                            if (sub.getOperator() != null) {
                                switch (sub.getOperator()) {
                                    case "GE":
                                        opCombo.getSelectionModel().select("以上");
                                        break;
                                    case "EQ":
                                        opCombo.getSelectionModel().select("等しい");
                                        break;
                                    case "LE":
                                        opCombo.getSelectionModel().select("以下");
                                        break;
                                }
                            }
                        }
                    }
                }

                // Achievement Conditions (Rows are already created by filterArea listener)
                if (condition.getConditions() != null) {
                    for (int i = 0; i < condition.getConditions().size(); i++) {
                        if (i >= this.conditionsBox.getChildren().size())
                            break;

                        logbook.bean.AppQuestCondition.Condition cond = condition.getConditions().get(i);
                        HBox row = (HBox) this.conditionsBox.getChildren().get(i);

                        // row children: Label(Area), (Label, TextField)?, S, A, B, Count, Label
                        int idx = 1;
                        // Check if cell field exists
                        if (row.getChildren().size() > 6) {
                            idx++; // Label("マス")
                            TextField cellField = (TextField) row.getChildren().get(idx++);
                            if (cond.getCell() != null) {
                                cellField.setText(cond.getCell());
                            } else if (cond.getCells() != null && !cond.getCells().isEmpty()) {
                                cellField.setText(String.join(",", cond.getCells()));
                            }
                        }

                        CheckBox s = (CheckBox) row.getChildren().get(idx++);
                        CheckBox a = (CheckBox) row.getChildren().get(idx++);
                        CheckBox b = (CheckBox) row.getChildren().get(idx++);
                        TextField countField = (TextField) row.getChildren().get(idx++);

                        if (cond.getRank() != null) {
                            s.setSelected(cond.getRank().contains("S"));
                            a.setSelected(cond.getRank().contains("A"));
                            b.setSelected(cond.getRank().contains("B"));
                        }
                        countField.setText(String.valueOf(cond.getCount()));
                    }
                }
            }

        } catch (Exception e) {
            LoggerHolder.get().error("カスタム任務設定の読み込みに失敗しました", e);
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

                row.getChildren().add(label);

                // マス入力欄を追加 (7-2, 7-3, 7-5)
                if (val.startsWith("7-2") || val.startsWith("7-3") || val.startsWith("7-5")) {
                    row.getChildren().add(new Label("マス"));
                    TextField cellField = new TextField();
                    cellField.setPrefWidth(40);
                    // 初期値を設定
                    if (val.equals("7-2-1")) {
                        cellField.setText("G");
                    } else if (val.equals("7-2-2")) {
                        cellField.setText("M");
                    } else if (val.equals("7-3-1")) {
                        cellField.setText("E");
                    } else if (val.equals("7-3-2")) {
                        cellField.setText("P");
                    } else if (val.equals("7-5-1")) {
                        cellField.setText("K");
                    } else if (val.equals("7-5-2")) {
                        cellField.setText("Q");
                    } else if (val.equals("7-5-3")) {
                        cellField.setText("T");
                    }
                    row.getChildren().add(cellField);
                }

                row.getChildren().addAll(checkS, checkA, checkB, count, countLabel);
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
            case 10:
                return "出撃(4)";
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
        typeSelector.setPrefWidth(70);
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
        } else if (rowsInfo == 1) {
            // 2行目のみ、かつ1行目が旗艦指定の場合に「二番艦」を追加
            HBox firstRow = (HBox) this.fleetConditionsBox.getChildren().get(0);
            // 1行目の要素構造: [Type], [Text], [Flagship], [Count], [Unit], [Compare], [Del]
            // Flagship は index 2 にあるはず
            if (firstRow.getChildren().size() > 2 && firstRow.getChildren().get(2) instanceof CheckBox) {
                CheckBox firstFlagship = (CheckBox) firstRow.getChildren().get(2);
                if (firstFlagship.isSelected()) {
                    CheckBox secondShip = new CheckBox("二番艦");
                    row.getChildren().add(secondShip);
                }
            }
        }

        TextField count = new TextField();
        count.setPrefWidth(30);
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

    /**
     * 艦種一覧を表示
     *
     * @param e ActionEvent
     */
    @FXML
    void showShipTypeList(javafx.event.ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        for (StatisticsShipTypeGroup group : StatisticsShipTypeGroup.values()) {
            sb.append(String.join(", ", group.getGroup())).append("\n");
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(this.questSelector.getScene().getWindow());
        alert.setTitle("艦種一覧");
        alert.setHeaderText("艦種グループに含まれる艦種");
        alert.getDialogPane().setContent(new TextArea(sb.toString()));
        alert.showAndWait();
    }

    @SuppressWarnings("unchecked")
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
                        .map(s -> {
                            if ("7-2-1".equals(s) || "7-2-2".equals(s)) {
                                return "7-2";
                            }
                            if ("7-3-1".equals(s) || "7-3-2".equals(s)) {
                                return "7-3";
                            }
                            if ("7-5-1".equals(s) || "7-5-2".equals(s) || "7-5-3".equals(s)) {
                                return "7-5";
                            }
                            return s;
                        })
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

                // Flagship / Second Ship
                if (idx < row.getChildren().size() && row.getChildren().get(idx) instanceof CheckBox) {
                    CheckBox cb = (CheckBox) row.getChildren().get(idx++);
                    if (cb.isSelected()) {
                        if (i == 0) {
                            sub.setOrder(1); // Flagship
                        } else if (i == 1) {
                            sub.setOrder(2); // Second Ship
                        }
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
                if (sub.getOrder() == null) {
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
                // Label(Area), [Label, TextField]?, S, A, B, Count, Label
                Label areaLabel = (Label) row.getChildren().get(0);

                int idx = 1;
                String cellValue = null;
                if (row.getChildren().size() > 6) {
                    idx++; // Label("マス")
                    TextField cellField = (TextField) row.getChildren().get(idx++);
                    cellValue = cellField.getText();
                }

                CheckBox s = (CheckBox) row.getChildren().get(idx++);
                CheckBox a = (CheckBox) row.getChildren().get(idx++);
                CheckBox b = (CheckBox) row.getChildren().get(idx++);
                TextField countField = (TextField) row.getChildren().get(idx++);

                logbook.bean.AppQuestCondition.Condition cond = new logbook.bean.AppQuestCondition.Condition();
                cond.setBoss(true); // Default

                String labelText = areaLabel.getText();
                if ("7-2-1".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-2")));
                    cond.setCell("G");
                } else if ("7-2-2".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-2")));
                    cond.setCell("M");
                } else if ("7-3-1".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-3")));
                    cond.setCell("E");
                } else if ("7-3-2".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-3")));
                    cond.setCell("P");
                } else if ("7-5-1".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-5")));
                    cond.setCell("K");
                } else if ("7-5-2".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-5")));
                    cond.setCell("Q");
                } else if ("7-5-3".equals(labelText)) {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList("7-5")));
                    cond.setCell("T");
                } else {
                    cond.setArea(new java.util.LinkedHashSet<>(Arrays.asList(labelText)));
                }

                // マス入力を反映 (ユーザー入力を優先)
                if (cellValue != null && !cellValue.isEmpty()) {
                    if (cellValue.contains(",")) {
                        cond.setCells(new java.util.LinkedHashSet<>(Arrays.asList(cellValue.split(","))));
                        cond.setCell(null);
                    } else {
                        cond.setCell(cellValue);
                        cond.setCells(null);
                    }
                }

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
