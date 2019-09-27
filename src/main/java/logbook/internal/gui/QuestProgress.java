package logbook.internal.gui;

import java.io.InputStream;
import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import logbook.bean.AppQuest;
import logbook.bean.AppQuestCondition;
import logbook.bean.AppQuestCondition.Condition;
import logbook.internal.LoggerHolder;
import logbook.internal.QuestCollect;
import logbook.internal.ThreadManager;
import logbook.plugin.PluginServices;

/**
 * 任務進捗確認
 *
 */
public class QuestProgress extends WindowController {

    @FXML
    private Label name;

    @FXML
    private Label info;

    @FXML
    private TreeView<String> condition;

    private AppQuest quest;

    void setQuest(AppQuest quest) {
        this.quest = quest;
        this.name.setText(quest.getQuest().getTitle());
        this.info.setText(quest.getQuest().getDetail().replaceAll("<br>", ""));
        this.condition.setRoot(new TreeItem<String>("読み込み中"));
        this.load();
    }

    private void load() {
        try {
            Task<Optional<AppQuestCondition>> task = new Task<Optional<AppQuestCondition>>() {
                @Override
                protected Optional<AppQuestCondition> call() throws Exception {
                    AppQuest quest = QuestProgress.this.quest;
                    InputStream is = PluginServices.getResourceAsStream("logbook/quest/" + quest.getNo() + ".json");
                    if (is != null) {
                        if (this.isCancelled()) {
                            return Optional.empty();
                        }
                        AppQuestCondition condition;
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.enable(Feature.ALLOW_COMMENTS);
                            condition = mapper.readValue(is, AppQuestCondition.class);
                        } finally {
                            is.close();
                        }
                        QuestCollect collect = QuestCollect.collect(quest, condition);
                        if (collect == null) {
                            return Optional.empty();
                        }
                        condition.test(collect);
                        return Optional.of(condition);
                    }
                    return Optional.empty();
                }

                @Override
                protected void succeeded() {
                    AppQuest quest = QuestProgress.this.quest;
                    Optional<AppQuestCondition> condition = this.getValue();
                    if (!condition.isPresent()) {
                        this.failed();
                    }

                    TreeItem<String> root = new TreeItem<>(quest.getQuest().getTitle());
                    root.setExpanded(true);

                    for (Condition part : condition.get().getConditions()) {
                        TreeItem<String> leaf = new TreeItem<>(part.toString());
                        QuestProgress.this.setIcon(leaf, part.getResult());
                        root.getChildren().add(leaf);
                    }
                    QuestProgress.this.setIcon(root, condition.get().getResult());
                    QuestProgress.this.condition.setRoot(root);
                }

                @Override
                protected void failed() {
                    Throwable t = this.getException();
                    TreeItem<String> root = new TreeItem<>("何らかの理由で集計出来ませんでした。" + (t != null ? String.valueOf(t) : ""));
                    QuestProgress.this.setIcon(root, false);
                    QuestProgress.this.condition.setRoot(root);
                }
            };
            this.getWindow().addEventHandler(WindowEvent.WINDOW_HIDDEN, ev -> {
                task.cancel();
            });
            ThreadManager.getExecutorService().execute(task);

        } catch (Exception e) {
            LoggerHolder.get().error("任務確認画面で例外", e);
        }
    }

    private void setIcon(TreeItem<String> item, Boolean result) {
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

        StackPane pane = new StackPane();
        pane.setPrefWidth(18);

        if (result != null) {
            if (result) {
                pane.getChildren().add(fontAwesome.create(FontAwesome.Glyph.CHECK).color(Color.GREEN));
            } else {
                pane.getChildren().add(fontAwesome.create(FontAwesome.Glyph.EXCLAMATION).color(Color.RED));
            }
        } else {
            pane.getChildren().add(fontAwesome.create(FontAwesome.Glyph.QUESTION).color(Color.GRAY));
        }
        item.setGraphic(pane);
    }
}
