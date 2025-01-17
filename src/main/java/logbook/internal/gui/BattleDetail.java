package logbook.internal.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import logbook.bean.BattleLog;
import logbook.bean.BattleResult;
import logbook.bean.BattleTypes;
import logbook.bean.BattleTypes.AirBaseAttack;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.BattleTypes.IAirBaseAttack;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.BattleTypes.IKouku;
import logbook.bean.BattleTypes.IMidnightBattle;
import logbook.bean.BattleTypes.INSupport;
import logbook.bean.BattleTypes.ISupport;
import logbook.bean.BattleTypes.Kouku;
import logbook.bean.BattleTypes.Stage1;
import logbook.bean.BattleTypes.Stage2;
import logbook.bean.BattleTypes.SupportAiratack;
import logbook.bean.BattleTypes.SupportInfo;
import logbook.bean.Ship;
import logbook.bean.SlotItem;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;
import logbook.constants.Rank;
import logbook.internal.kancolle.Items;
import logbook.internal.kancolle.PhaseState;
import logbook.internal.kancolle.Ships;
import lombok.Getter;

/**
 * 戦況表示
 *
 */
public class BattleDetail extends WindowController {

    /** 連合艦隊 */
    private CombinedType combinedType;

    /** 艦隊スナップショット */
    private Map<Integer, List<Ship>> deckMap;

    /** 装備スナップショット */
    private Map<Integer, SlotItem> itemMap;

    /** 退避艦IDスナップショット */
    private Set<Integer> escape;

    /** 戦闘 */
    private IFormation battle;

    /** 夜戦 */
    private IMidnightBattle midnight;

    /** 戦果報告 */
    private BattleResult result;

    /** ルート要素 */
    @FXML
    private VBox detail;

    /** フェーズ */
    @FXML
    private VBox phase;

    /** ルート情報 */
    @FXML
    private HBox routeInfo;

    /** マス */
    @FXML
    private Label mapcell;

    /** ルート */
    @FXML
    private Label route;

    /** 艦隊行動: */
    @FXML
    private Label intercept;

    /** 味方陣形 */
    @FXML
    private Label fFormation;

    /** 味方陣形 */
    @FXML
    private Label eFormation;

    /** 制空値計 */
    @FXML
    private Label seiku;

    /** 味方触接 */
    @FXML
    private Label dispSeiku;

    /** 味方触接アイコン */
    @FXML
    private ImageView fTouchPlaneImage;

    /** 味方触接 */
    @FXML
    private Label fTouchPlane;

    /** 敵触接アイコン */
    @FXML
    private ImageView eTouchPlaneImage;

    /** 敵触接 */
    @FXML
    private Label eTouchPlane;

    /** 対空CI */
    @FXML
    private Label tykuCI;

    /** 評価 */
    @FXML
    private Label judge;

    /** 基本経験値 */
    @FXML
    private Label baseExp;

    /** 獲得経験値 */
    @FXML
    private Label shipExp;

    /** 提督経験値 */
    @FXML
    private Label exp;

    /** 周期タイマー */
    private Timeline timeline = new Timeline();

    /** ハッシュ・コード */
    private int hashCode;

    /**
     * 戦況表示を定期的に更新して表示します。
     *
     * @param supplier 戦闘ログのサプライヤー
     */
    void setInterval(Supplier<BattleLog> supplier) {
        this.timeline.setCycleCount(Animation.INDEFINITE);
        this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.millis(1000),
                e -> this.setData(supplier.get())));
        this.timeline.play();
    }

    /**
     * 戦況表示
     *
     * @param log 戦闘ログ
     */
    void setData(BattleLog log) {
        if (log != null && log.getBattle() != null) {
            BattleDetailViewData battleDetailViewData = new BattleDetailViewData(log);

            CombinedType combinedType = log.getCombinedType();
            Map<Integer, List<Ship>> deckMap = log.getDeckMap();
            Map<Integer, SlotItem> itemMap = log.getItemMap();
            IFormation battle = log.getBattle();
            IMidnightBattle midnight = log.getMidnight();
            Set<Integer> escape = log.getEscape();
            BattleResult result = log.getResult();

            int hashCode = battleDetailViewData.getHashCode();
            if (this.hashCode == hashCode) {
                return;
            }
            this.hashCode = hashCode;
            
            this.setData(combinedType, deckMap, escape, itemMap, battle, midnight, result, battleDetailViewData);
        }
    }

    /**
     * 戦況表示
     * @param combinedType 連合艦隊
     * @param deckMap 艦隊スナップショット
     * @param escape 退避艦IDスナップショット
     * @param itemMap 装備
     * @param battle 戦闘
     * @param midnight 夜戦
     * @param result 戦果報告 
     */
    void setData(CombinedType combinedType, Map<Integer, List<Ship>> deckMap, Set<Integer> escape,
            Map<Integer, SlotItem> itemMap, IFormation battle, IMidnightBattle midnight, BattleResult result,
            BattleDetailViewData battleDetailViewData) {

        this.combinedType = combinedType;
        this.deckMap = deckMap;
        this.itemMap = itemMap;
        this.escape = escape;
        this.battle = battle;
        this.midnight = midnight;
        this.result = result;
        this.update(battleDetailViewData);
    }

    /**
     * 画像ファイルに保存
     *
     * @param event ActionEvent
     */
    @FXML
    void storeImageAction(ActionEvent event) {
        Control source = null;
        if (event.getSource() instanceof Control) {
            source = (Control) event.getSource();
        }
        if (source != null) {
            source.setVisible(false);
        }
        Tools.Controls.storeSnapshot(this.detail, "戦闘ログのスナップショット", this.getWindow());
        if (source != null) {
            source.setVisible(true);
        }
    }

    /**
     * 画面情報を更新します
     * @param battleDetailViewData
     */
    private void update(BattleDetailViewData battleDetailViewData) {
        this.setInfo(battleDetailViewData);
        this.setPhase(battleDetailViewData);
    }

    /**
     * インフォメーション部分の表示を行います
     * @param battleDetailViewData
     */
    private void setInfo(BattleDetailViewData battleDetailViewData) {
        PhaseState ps = battleDetailViewData.getPhaseState();
        
        //出撃/進撃情報
        if (battleDetailViewData.isMapStartNextData()) {
            // マス
            this.mapcell.setText(battleDetailViewData.getMapCellViewString());
            // ルート
            this.route.setText(battleDetailViewData.getRouteViewString());
        } else {
            this.routeInfo.getChildren().clear();
            this.routeInfo.getChildren().add(new Label("演習詳細"));
        }

        // 艦隊行動
        this.intercept.setText(battleDetailViewData.getInterceptViewString());
        // 味方陣形
        this.fFormation.setText(battleDetailViewData.getFriendFormationViewString());
        // 敵陣形
        this.eFormation.setText(battleDetailViewData.getEnemyFormationViewString());
        // 制空値計
        this.seiku.setText(battleDetailViewData.getSeikuViewString());

        // 初期化
        this.dispSeiku.setText("");
        this.dispSeiku.getStyleClass().removeIf(n -> n.startsWith("dispseiku"));
        this.fTouchPlaneImage.setImage(null);
        this.fTouchPlaneImage.setFitWidth(0);
        this.fTouchPlaneImage.setFitHeight(0);
        this.fTouchPlane.setText("");
        this.eTouchPlaneImage.setImage(null);
        this.eTouchPlaneImage.setFitWidth(0);
        this.eTouchPlaneImage.setFitHeight(0);
        this.eTouchPlane.setText("");
        this.tykuCI.setText("");

        if (this.battle.isIKouku()) {
            Kouku kouku = this.battle.asIKouku().getKouku();
            if (kouku != null) {
                Stage1 stage1 = kouku.getStage1();
                Stage2 stage2 = kouku.getStage2();

                if (stage1 != null) {
                    Map<Integer, SlotitemMst> slotitemMst = SlotitemMstCollection.get()
                            .getSlotitemMap();
                    // 制空権
                    this.dispSeiku.setText(BattleTypes.DispSeiku.toDispSeiku(stage1.getDispSeiku()).toString());
                    this.dispSeiku.getStyleClass().add("dispseiku" + stage1.getDispSeiku());
                    // 味方触接
                    SlotitemMst fTouchPlaneItem = slotitemMst.get(stage1.getTouchPlane().get(0));
                    if (fTouchPlaneItem != null) {
                        Image image = Items.itemImage(fTouchPlaneItem);
                        if (image != null) {
                            this.fTouchPlaneImage.setImage(image);
                            this.fTouchPlaneImage.setFitWidth(24);
                            this.fTouchPlaneImage.setFitHeight(24);
                        }
                        this.fTouchPlane.setText(fTouchPlaneItem.getName()
                                + "(+" + (int) (Ships.touchPlaneAttackCompensation(fTouchPlaneItem) * 100) + "%)");
                    } else {
                        this.fTouchPlaneImage.setFitWidth(0);
                        this.fTouchPlaneImage.setFitHeight(0);
                        this.fTouchPlane.setText("なし");
                    }
                    // 敵触接
                    SlotitemMst eTouchPlaneItem = slotitemMst.get(stage1.getTouchPlane().get(1));
                    if (eTouchPlaneItem != null) {
                        Image image = Items.itemImage(eTouchPlaneItem);
                        if (image != null) {
                            this.eTouchPlaneImage.setImage(image);
                            this.eTouchPlaneImage.setFitWidth(24);
                            this.eTouchPlaneImage.setFitHeight(24);
                        }
                        this.eTouchPlane.setText(eTouchPlaneItem.getName()
                                + "(+" + (int) (Ships.touchPlaneAttackCompensation(eTouchPlaneItem) * 100) + "%)");
                    } else {
                        this.eTouchPlaneImage.setFitWidth(0);
                        this.eTouchPlaneImage.setFitHeight(0);
                        this.eTouchPlane.setText("なし");
                    }
                }
                if (stage2 != null) {
                    // 対空CI
                    if (stage2.getAirFire() != null && stage2.getAirFire().getIdx() != null) {
                        // インデックスは0始まり
                        int idx = stage2.getAirFire().getIdx();
                        Ship ship;
                        // 遊撃部隊は7隻なので < 6 ではない
                        if (idx < ps.getAfterFriend().size()) {
                            ship = ps.getAfterFriend().get(idx);
                        } else {
                            ship = ps.getAfterFriendCombined().get(idx - 6);
                        }
                        this.tykuCI.setText(Ships.toName(ship)
                                + " (第" + stage2.getAirFire().getKind() + "種)");
                    }
                }
            }
        }
    }

    /**
     * 戦闘フェイズ部分の表示を行います
     * @param battleDetailViewData
     */
    private void setPhase(BattleDetailViewData battleDetailViewData) {
        PhaseState ps = battleDetailViewData.getPhaseState();

        // 評価初期化
        Judge judge = new Judge();
        judge.setBefore(ps);

        // 詳細初期化
        List<Node> phases = this.phase.getChildren();
        phases.clear();

        BattleDetailPhase phaseBeforePane = new BattleDetailPhase(ps);
        phaseBeforePane.setText("戦闘前");
        phases.add(phaseBeforePane);

        if (!(this.battle.isINightToDayBattle())) {
            // 夜戦→昼戦以外

            // 基地航空隊戦フェイズ(噴式強襲)
            this.airBaseInjectionAttack(ps, phases);
            // 航空戦フェイズ(噴式強襲)
            this.injectionKouku(ps, phases);
            // 基地航空隊戦フェイズ
            this.airBaseAttack(ps, phases);
            // 航空戦フェイズ
            this.kouku(ps, phases);
            // 支援フェイズ
            this.support(ps, phases);
            // 砲雷撃戦フェイズ
            this.sortieHougeki(ps, phases);
            // 航空戦
            this.airbattle(ps, phases);
            // 夜戦支援
            this.nSupport(ps, phases);
            // 友軍艦隊
            this.friendlyHougeki(ps, phases);
            // 特殊夜戦
            this.midnightBattle(ps, phases);
            // 夜戦
            this.midnight(ps, phases);
        } else {
            // 夜戦→昼戦

            // 夜戦支援
            this.nSupport(ps, phases);
            // 友軍艦隊
            this.friendlyHougeki(ps, phases);
            // 特殊夜戦
            this.midnightBattle(ps, phases);
            // 基地航空隊戦フェイズ(噴式強襲)
            this.airBaseInjectionAttack(ps, phases);
            // 航空戦フェイズ(噴式強襲)
            this.injectionKouku(ps, phases);
            // 基地航空隊戦フェイズ
            this.airBaseAttack(ps, phases);
            // 航空戦フェイズ
            this.kouku(ps, phases);
            // 支援フェイズ
            this.support(ps, phases);
            // 砲雷撃戦フェイズ
            this.sortieHougeki(ps, phases);
        }

        // 評価判定
        judge.setAfter(ps, this.battle);
        this.judge.setText(String.valueOf(judge.getRank())
                + "(味方損害率:" + BigDecimal.valueOf(judge.getFriendDamageRatio()).setScale(3, RoundingMode.FLOOR)
                + "/敵損害率:" + BigDecimal.valueOf(judge.getEnemyDamageRatio()).setScale(3, RoundingMode.FLOOR) + ")");

        // 経験値
        if (this.result != null) {
            this.baseExp.setText(String.valueOf(this.result.getGetBaseExp()));
            this.shipExp.setText(String.valueOf(this.result.getGetShipExp().stream()
                    .mapToInt(Integer::intValue)
                    .filter(i -> i > 0)
                    .sum()
                    + Optional.ofNullable(this.result.getGetShipExpCombined())
                            .map(v -> v.stream()
                                    .mapToInt(Integer::intValue)
                                    .filter(i -> i > 0)
                                    .sum())
                            .orElse(0)));
            this.exp.setText(this.result.getGetExp()
                    + "(" + BigDecimal.valueOf(this.result.getGetExp())
                            .divide(BigDecimal.valueOf(1428.571), 3, RoundingMode.FLOOR)
                            .toPlainString()
                    + ")");
        } else {
            this.baseExp.setText("?");
            this.shipExp.setText("?");
            this.exp.setText("?");
        }
        ((BattleDetailPhase) phases.get(phases.size() - 1)).setExpanded(true);
    }

    /**
     * 基地航空隊戦フェイズ(噴式強襲)
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void airBaseInjectionAttack(PhaseState ps, List<Node> phases) {
        // 基地航空隊戦フェイズ(噴式強襲)
        if (this.battle.isIAirBaseAttack()) {
            if (this.battle.asIAirBaseAttack().getAirBaseInjection() != null) {
                // 基地航空隊戦フェイズ適用
                ps.applyAirBaseInject((IAirBaseAttack) this.battle);

                List<Node> stage = new ArrayList<>();

                AirBaseAttack airBaseAttack = ((IAirBaseAttack) this.battle).getAirBaseInjection();
                if (airBaseAttack.getStage1() != null) {
                    Stage1 stage1 = airBaseAttack.getStage1();
                    stage.add(new BattleDetailPhaseStage1(stage1, "基地航空隊"));
                }
                if (airBaseAttack.getStage2() != null) {
                    Stage2 stage2 = airBaseAttack.getStage2();
                    stage.add(new BattleDetailPhaseStage2(stage2, "基地航空隊"));
                }
                BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
                phasePane.setText("基地航空隊戦フェイズ(噴式強襲)");
                phasePane.setExpanded(false);
                phases.add(phasePane);
            }
        }
    }

    /**
     * 航空戦フェイズ(噴式強襲)
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void injectionKouku(PhaseState ps, List<Node> phases) {
        // 航空戦フェイズ(噴式強襲)
        if (this.battle.isIKouku()) {
            if (this.battle.asIKouku().getInjectionKouku() != null) {
                // 航空戦フェイズ適用
                ps.applyInjectionKouku((IKouku) this.battle);

                List<Node> stage = new ArrayList<>();

                Kouku kouku = ((IKouku) this.battle).getInjectionKouku();
                if (kouku.getStage1() != null) {
                    Stage1 stage1 = kouku.getStage1();
                    stage.add(new BattleDetailPhaseStage1(stage1, "僚艦"));
                }
                if (kouku.getStage2() != null) {
                    Stage2 stage2 = kouku.getStage2();
                    stage.add(new BattleDetailPhaseStage2(stage2, "僚艦"));
                }
                BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
                phasePane = new BattleDetailPhase(ps);
                phasePane.setText("航空戦フェイズ(噴式強襲)");
                phasePane.setExpanded(false);
                phases.add(phasePane);
            }
        }
    }

    /**
     * 基地航空隊戦フェイズ
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void airBaseAttack(PhaseState ps, List<Node> phases) {
        // 基地航空隊戦フェイズ
        if (this.battle.isIAirBaseAttack()) {
            if (this.battle.asIAirBaseAttack().getAirBaseAttack() != null) {
                // 基地航空隊戦フェイズ適用
                ps.applyAirBaseAttack((IAirBaseAttack) this.battle);

                List<Node> stage = new ArrayList<>();

                for (AirBaseAttack airBaseAttack : ((IAirBaseAttack) this.battle).getAirBaseAttack()) {
                    if (airBaseAttack.getStage1() != null) {
                        stage.add(new BattleDetailPhaseStage1AirBase(airBaseAttack.getStage1(), airBaseAttack.getSquadronPlane()));
                        stage.add(new BattleDetailPhaseStage1(airBaseAttack.getStage1(), "基地航空隊"));
                    }
                    if (airBaseAttack.getStage2() != null) {
                        stage.add(new BattleDetailPhaseStage2(airBaseAttack.getStage2(), "基地航空隊"));
                    }
                }
                BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
                phasePane.setText("基地航空隊戦フェイズ");
                phasePane.setExpanded(false);
                phases.add(phasePane);
            }
        }
    }

    /**
     * 航空戦フェイズ
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void kouku(PhaseState ps, List<Node> phases) {
        // 航空戦フェイズ
        if (this.battle.isIKouku()) {
            if (this.battle.asIKouku().getKouku() != null) {
                // 航空戦フェイズ適用
                ps.applyKouku((IKouku) this.battle);

                List<Node> stage = new ArrayList<>();

                Kouku kouku = ((IKouku) this.battle).getKouku();
                if (kouku.getStage1() != null) {
                    Stage1 stage1 = kouku.getStage1();
                    stage.add(new BattleDetailPhaseStage1(stage1, "僚艦"));
                }
                if (kouku.getStage2() != null) {
                    Stage2 stage2 = kouku.getStage2();
                    stage.add(new BattleDetailPhaseStage2(stage2, "僚艦"));
                }
                BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
                phasePane.setText("航空戦フェイズ");
                phasePane.setExpanded(false);
                phases.add(phasePane);
            }
        }
    }

    /**
     * 支援フェイズ
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void support(PhaseState ps, List<Node> phases) {
        // 支援フェイズ
        if (this.battle.isISupport()) {
            SupportInfo support = this.battle.asISupport().getSupportInfo();
            if (support != null) {
                // 支援フェイズ適用
                ps.applySupport((ISupport) this.battle);
                this.setSupportPhase(ps, phases, support);
            }
        }
    }

    /**
     * 砲雷撃戦フェイズ
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void sortieHougeki(PhaseState ps, List<Node> phases) {
        // 砲雷撃戦フェイズ
        if (this.battle.isISortieHougeki()) {
            // 砲雷撃戦フェイズ適用
            ps.applySortieHougeki(this.battle.asISortieHougeki());

            BattleDetailPhase phasePane = new BattleDetailPhase(ps);
            phasePane.setText("砲雷撃戦フェイズ");
            phasePane.setExpanded(false);
            phases.add(phasePane);
        }
        ps.getAttackDetails().clear();
    }

    /**
     * 航空戦
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void airbattle(PhaseState ps, List<Node> phases) {
        // 航空戦
        if (this.battle.isIAirbattle()) {
            if (this.battle.asIAirbattle().getKouku2() != null) {
                // 航空戦適用
                ps.applyAirbattle(this.battle.asIAirbattle());

                List<Node> stage = new ArrayList<>();

                Kouku kouku = this.battle.asIAirbattle().getKouku2();
                if (kouku.getStage1() != null) {
                    Stage1 stage1 = kouku.getStage1();
                    stage.add(new BattleDetailPhaseStage1(stage1, "僚艦"));
                }
                if (kouku.getStage2() != null) {
                    Stage2 stage2 = kouku.getStage2();
                    stage.add(new BattleDetailPhaseStage2(stage2, "僚艦"));
                }
                BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
                phasePane.setText("航空戦");
                phasePane.setExpanded(false);
                phases.add(phasePane);
            }
        }
    }

    /**
     * 夜戦支援
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void nSupport(PhaseState ps, List<Node> phases) {
        // 夜戦支援
        if (this.battle.isINSupport()) {
            SupportInfo support = this.battle.asINSupport().getNSupportInfo();
            if (support != null) {
                // 支援フェイズ適用
                ps.applySupport((INSupport) this.battle);
                this.setSupportPhase(ps, phases, support);
            }
        }
    }

    /**
     * 特殊夜戦
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void midnightBattle(PhaseState ps, List<Node> phases) {
        // 特殊夜戦
        if (this.battle.isIMidnightBattle()) {
            // 特殊夜戦適用
            ps.applyMidnightBattle(this.battle.asIMidnightBattle());

            BattleDetailPhase phasePane = new BattleDetailPhase(ps);
            phasePane.setText("特殊夜戦");
            phasePane.setExpanded(false);
            phases.add(phasePane);
        }
        if (this.battle.isINightToDayBattle()) {
            // 特殊夜戦適用
            ps.applyMidnightBattle(this.battle.asINightToDayBattle());

            BattleDetailPhase phasePane = new BattleDetailPhase(ps);
            phasePane.setText("特殊夜戦");
            phasePane.setExpanded(false);
            phases.add(phasePane);
        }
        ps.getAttackDetails().clear();
    }

    /**
     * 夜戦(友軍艦隊)
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void friendlyHougeki(PhaseState ps, List<Node> phases) {
        // 夜戦(友軍艦隊)
        if (this.midnight != null && this.midnight.getFriendlyInfo() != null) {
            // 夜戦(友軍艦隊)適用
            ps.applyFriendlyHougeki(this.midnight);

            BattleDetailPhase phasePane = new BattleDetailPhase(ps, null, true);
            phasePane.setText("夜戦(友軍艦隊)");
            phasePane.setExpanded(false);
            phases.add(phasePane);
        }
        ps.getAttackDetails().clear();
    }

    /**
     * 夜戦
     * @param ps フェーズ
     * @param phases 表示ノード
     */
    private void midnight(PhaseState ps, List<Node> phases) {
        // 夜戦
        if (this.midnight != null) {
            // 夜戦適用
            ps.applyMidnightBattle(this.midnight);

            BattleDetailPhase phasePane = new BattleDetailPhase(ps);
            phasePane.setText("夜戦");
            phasePane.setExpanded(false);
            phases.add(phasePane);
        }
        ps.getAttackDetails().clear();
    }

    /**
     * 支援共通
     * @param ps フェーズ
     * @param phases 表示ノード
     * @param support 支援
     */
    private void setSupportPhase(PhaseState ps, List<Node> phases, SupportInfo support) {
        SupportAiratack supportAir = support.getSupportAiratack();
        List<Node> stage = new ArrayList<>();
        if (supportAir != null) {
            if (supportAir.getStage1() != null) {
                Stage1 stage1 = supportAir.getStage1();
                stage.add(new BattleDetailPhaseStage1(stage1, "支援艦隊"));
            }
            if (supportAir.getStage2() != null) {
                Stage2 stage2 = supportAir.getStage2();
                stage.add(new BattleDetailPhaseStage2(stage2, "支援艦隊"));
            }
        }
        BattleDetailPhase phasePane = new BattleDetailPhase(ps, stage);
        phasePane.setText("支援フェイズ");
        phasePane.setExpanded(false);
        phases.add(phasePane);
    }

    /**
     * ウインドウを閉じる時のアクション
     *
     * @param e WindowEvent
     */
    @Override
    protected void onWindowHidden(WindowEvent e) {
        if (this.timeline != null) {
            this.timeline.stop();
        }
    }

    /**
     * 勝敗判定
     */
    private static class Judge {

        private double beforeFriendTotalHp;

        private int beforeFriendAliveCount;

        private double beforeEnemyTotalHp;

        private int beforeEnemyAliveCount;

        private double afterFriendTotalHp;

        private int afterFriendAliveCount;

        private double afterEnemyTotalHp;

        private int afterEnemyAliveCount;

        /** 味方損害率 */
        @Getter
        private double friendDamageRatio;

        /** 敵損害率 */
        @Getter
        private double enemyDamageRatio;

        /** 勝敗 */
        @Getter
        private Rank rank;

        /**
         * 戦闘前の状態を設定します。
         * @param ps フェイズ
         */
        public void setBefore(PhaseState ps) {
            this.beforeFriendTotalHp = ps.friendTotalHp();
            this.beforeFriendAliveCount = ps.friendAliveCount();
            this.beforeEnemyTotalHp = ps.enemyTotalHp();
            this.beforeEnemyAliveCount = ps.enemydAliveCount();
        }

        /**
         * 戦闘後の状態を設定します。
         * @param ps フェイズ
         */
        public void setAfter(PhaseState ps, IFormation battle) {
            this.afterFriendTotalHp = ps.friendTotalHp();
            this.afterFriendAliveCount = ps.friendAliveCount();
            this.afterEnemyTotalHp = ps.enemyTotalHp();
            this.afterEnemyAliveCount = ps.enemydAliveCount();
            this.friendDamageRatio = this.damageRatio(this.beforeFriendTotalHp, this.afterFriendTotalHp);
            this.enemyDamageRatio = this.damageRatio(this.beforeEnemyTotalHp, this.afterEnemyTotalHp);
            this.rank = this.judge(ps, battle);
        }

        /**
         * 勝敗判定
         * @param ps フェイズ
         * @param battle 戦闘
         * @return ランク
         */
        private Rank judge(PhaseState ps, IFormation battle) {
            if (battle.isILdAirbattle() || battle.isILdShooting()) {
                if (this.friendDamageRatio <= 0) {
                    return Rank.S完全勝利;
                }
                if (this.friendDamageRatio < 10) {
                    return Rank.A勝利;
                }
                if (this.friendDamageRatio < 20) {
                    return Rank.B戦術的勝利;
                }
                if (this.friendDamageRatio < 50) {
                    return Rank.C戦術的敗北;
                }
                if (this.friendDamageRatio < 80) {
                    return Rank.D敗北;
                }
                return Rank.E敗北;
            } else {
                if (this.beforeFriendAliveCount == this.afterFriendAliveCount) {
                    if (this.afterEnemyAliveCount == 0) {
                        if (this.afterFriendTotalHp >= this.beforeFriendTotalHp) {
                            return Rank.S完全勝利;
                        } else {
                            return Rank.S勝利;
                        }
                    } else if (this.beforeEnemyAliveCount > 1
                            && (this.beforeEnemyAliveCount
                                    - this.afterEnemyAliveCount) >= (int) (this.beforeEnemyAliveCount * 0.7D)) {
                        return Rank.A勝利;
                    }
                }
                if (Ships.isLost(ps.getAfterEnemy().get(0))
                        && (this.beforeFriendAliveCount - this.afterFriendAliveCount) < (this.beforeEnemyAliveCount
                                - this.afterEnemyAliveCount)) {
                    return Rank.B戦術的勝利;
                }
                if (this.beforeFriendAliveCount == 1 && Ships.isBadlyDamage(ps.getAfterFriend().get(0))) {
                    return Rank.D敗北;
                }
                if (Math.floor(this.enemyDamageRatio) > 2.5 * Math.floor(this.friendDamageRatio)) {
                    return Rank.B戦術的勝利;
                }
                if (Math.floor(this.enemyDamageRatio) > 0.9 * Math.floor(this.friendDamageRatio)) {
                    return Rank.C戦術的敗北;
                }
                if (this.beforeFriendAliveCount > this.afterFriendAliveCount && this.afterFriendAliveCount == 1) {
                    return Rank.E敗北;
                }
                return Rank.D敗北;
            }
        }

        /**
         * 損害率を計算する
         * @param before 前HP
         * @param after 後HP
         * @return 損害率
         */
        private double damageRatio(double before, double after) {
            return (before - after) / before * 100;
        }
    }
}
