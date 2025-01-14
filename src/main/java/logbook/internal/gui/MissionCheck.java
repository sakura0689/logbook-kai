package logbook.internal.gui;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.controlsfx.control.SegmentedButton;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import logbook.bean.DeckPort;
import logbook.bean.DeckPortCollection;
import logbook.bean.Maparea;
import logbook.bean.MapareaCollection;
import logbook.bean.Mission;
import logbook.bean.MissionCollection;
import logbook.bean.MissionCondition;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.Stype;
import logbook.bean.StypeCollection;
import logbook.internal.Missions;
import logbook.internal.logger.LoggerHolder;

/**
 * 遠征確認画面
 *
 */
public class MissionCheck extends WindowController {

    @FXML
    private SegmentedButton fleet;

    @FXML
    private TreeView<String> conditionTree;

    private ObjectMapper mapper = new ObjectMapper();

    private Set<Mission> expanded = new HashSet<>();

    public MissionCheck() {
        this.mapper.configure(Feature.ALLOW_COMMENTS, true);
    }

    @FXML
    void initialize() {
        for (DeckPort deck : DeckPortCollection.get().getDeckPortMap().values()) {
            ToggleButton button = new ToggleButton(deck.getName());
            button.setUserData(deck);
            this.fleet.getButtons().add(button);
        }
        this.fleet.getToggleGroup().selectedToggleProperty().addListener((ob, o, n) -> {
            DeckPort deck = null;
            if (n != null) {
                deck = (DeckPort) n.getUserData();
            }
            this.buildTree(deck);
        });
        this.fleet.getButtons().stream()
                .skip(1)
                .findFirst()
                .ifPresent(b -> b.setSelected(true));
    }

    @FXML
    void update(ActionEvent event) {
        Toggle toggle = this.fleet.getToggleGroup().getSelectedToggle();
        DeckPort deck = null;
        if (toggle != null) {
            deck = (DeckPort) toggle.getUserData();
        }
        this.buildTree(deck);
    }

    private void buildTree(DeckPort deck) {
        TreeItem<String> root = new TreeItem<>();
        if (deck != null) {
            Map<Integer, Ship> shipMap = ShipCollection.get()
                    .getShipMap();
            List<Ship> fleet = DeckPortCollection.get().getDeckPortMap().get(deck.getId()).getShip()
                    .stream()
                    .map(shipMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<Integer, List<Mission>> missionMap = MissionCollection.get().getMissionMap().values().stream()
                    .sorted(Comparator.comparing(Mission::getMapareaId, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Mission::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.groupingBy(Mission::getMapareaId, LinkedHashMap::new, Collectors.toList()));

            for (Map.Entry<Integer, List<Mission>> missionEntry : missionMap.entrySet()) {
                TreeItem<String> subTree = new TreeItem<>();
                subTree.setExpanded(true);

                List<Mission> missions = missionEntry.getValue();
                Integer mapareaId = missions.get(0).getMapareaId();

                String area = Optional.ofNullable(MapareaCollection.get().getMaparea().get(mapareaId))
                        .filter(map -> map.getId() != null && map.getId() <= 40)
                        .map(Maparea::getName)
                        .orElse("イベント海域");
                subTree.setValue(area);

                for (Mission mission : missions) {

                    TreeItem<String> sub = this.buildTree0(mission, fleet);
                    if (sub != null) {
                        sub.setExpanded(this.expanded.contains(mission));
                        sub.expandedProperty().addListener((ob, ov, nv) -> {
                            if (nv != null && nv) {
                                this.expanded.add(mission);
                            } else {
                                this.expanded.remove(mission);
                            }
                        });
                        subTree.getChildren().add(sub);
                    }
                }

                if (!subTree.getChildren().isEmpty()) {
                    root.getChildren().add(subTree);
                }
            }
        }
        this.conditionTree.setRoot(root);
    }
    
    private static MissionCondition createDefaultGreatSuccessCondition() {
        MissionCondition success = new MissionCondition();
        success.setType("艦隊");
        success.setCountType("キラキラ");
        success.setValue(6);
        return success;
    }

    private TreeItem<String> buildTree0(Mission mission, List<Ship> fleet) {
        try {
            TreeItem<String> item;
            Optional<MissionCondition> condition = Missions.getMissionCondition(mission.getId());
            if (condition.isPresent()) {
                MissionCondition cond = condition.get();
                cond.test(fleet);
                if (cond.getGreatSuccessCondition() == null) {
                    cond.setGreatSuccessCondition(createDefaultGreatSuccessCondition());
                }
                cond.getGreatSuccessCondition().test(fleet);
                item = this.buildLeaf(cond);
            } else if (mission.getSampleFleet() != null) {
                item = new TreeItem<>();
                setIcon(item, null);
            } else {
                return null;
            }
            
            item.setValue(mission.toString()+" ["+Missions.getDurationText(mission) + "]");
            if (mission.getDamageType() != null && mission.getDamageType().intValue() > 0) {
                String label;
                switch (mission.getDamageType()) {
                case 1:
                    label = "交戦型";
                    break;
                case 2:
                    label = "交戦II型";
                    break;
                default:
                    label = "交戦型(" + mission.getDamageType() + ")";
                    break;
                }
                TreeItem<String> battle = new TreeItem<>(label);
                ImageView image = new ImageView(Missions.damageTypeIcon(mission.getDamageType()));
                image.setFitWidth(18);
                image.setFitHeight(18);
                battle.setGraphic(image);
                item.getChildren().add(0, battle);
            }
            if (mission.getSampleFleet() != null) {
                TreeItem<String> sample = new TreeItem<>("サンプル編成");
                setIcon(sample, FontAwesome.Glyph.INFO, null);
                for (Integer type : mission.getSampleFleet()) {
                    Optional.ofNullable(StypeCollection.get()
                            .getStypeMap()
                            .get(type))
                            .map(Stype::getName)
                            .ifPresent(name -> sample.getChildren().add(new TreeItem<>(name)));
                }
                item.getChildren().add(sample);
            }
            return item;
        } catch (Exception e) {
            LoggerHolder.get().error("遠征確認画面で例外", e);
        }
        return null;
    }

    private TreeItem<String> buildLeaf(MissionCondition condition) {
        TreeItem<String> item = new TreeItem<>(condition.toString());
        this.setIcon(item, condition.getResult());
        // 大成功条件をチェック
        Optional.ofNullable(condition.getGreatSuccessCondition())
            .ifPresent(cond -> {
                TreeItem<String> greatSuccess;
                if (condition.getResult()) {
                    if (cond.getResult()) {
                        greatSuccess = new TreeItem<>("大成功");
                        setIcon(greatSuccess, true);
                    } else {
                        greatSuccess = new TreeItem<>("成功");
                        setIcon(greatSuccess, FontAwesome.Glyph.CHECK, Color.ORANGE);
                        setIcon(item, FontAwesome.Glyph.CHECK, Color.ORANGE);
                    }
                } else {
                    greatSuccess = new TreeItem<>("失敗");
                    setIcon(greatSuccess, false);
                }
                greatSuccess.getChildren().add(buildLeaf(cond));
                item.getChildren().add(greatSuccess);
            });

        if (condition.getConditions() != null) {
            if (condition.getConditions().size() == 1 && !condition.getOperator().startsWith("N")) {
                item.setValue(condition.getConditions().get(0).toString());
            } else {
                for (MissionCondition subcondition : condition.getConditions()) {
                    item.getChildren().add(this.buildLeaf(subcondition));
                }
            }
        }
        return item;
    }

    private void setIcon(TreeItem<String> item, FontAwesome.Glyph glyph, Color color) {
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        StackPane pane = new StackPane();
        pane.setPrefWidth(18);
        Glyph g = fontAwesome.create(glyph);
        Optional.ofNullable(color).ifPresent(g::color);
        pane.getChildren().add(g);
        item.setGraphic(pane);
    }

    private void setIcon(TreeItem<String> item, Boolean result) {
        if (result != null) {
            if (result) {
                setIcon(item, FontAwesome.Glyph.CHECK, Color.GREEN);
            } else {
                setIcon(item, FontAwesome.Glyph.EXCLAMATION, Color.RED);
            }
        } else {
            setIcon(item, FontAwesome.Glyph.QUESTION, Color.GRAY);
        }
    }
}
