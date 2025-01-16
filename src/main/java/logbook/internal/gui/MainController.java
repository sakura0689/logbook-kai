package logbook.internal.gui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import logbook.bean.AppBouyomiConfig;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.AppExpRecords;
import logbook.bean.AppQuest;
import logbook.bean.AppQuestCollection;
import logbook.bean.Basic;
import logbook.bean.DeckPort;
import logbook.bean.DeckPortCollection;
import logbook.bean.Mission;
import logbook.bean.MissionCollection;
import logbook.bean.Ndock;
import logbook.bean.NdockCollection;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.ShipMst;
import logbook.bean.SlotItemCollection;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;
import logbook.common.Messages;
import logbook.constants.SlotItemType;
import logbook.core.LogBookCoreServices;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.Tuple;
import logbook.internal.bouyomi.BouyomiChanUtils;
import logbook.internal.bouyomi.BouyomiChanUtils.Type;
import logbook.internal.kancolle.Ships;
import logbook.internal.proxy.ProxyHolder;
import logbook.internal.util.AudiosUtil;
import logbook.plugin.lifecycle.StartUp;

/**
 * UIコントローラー
 *
 */
public class MainController extends WindowController {

    /** 装備|母港枠の警告cssクラス名 */
    private static final String FULLY_CLASS = "fully";

    /** 通知 */
    private static final Duration NOTIFY = Duration.ofMinutes(1);

    private String itemFormat;

    private String shipFormat;

    /** 艦隊コレクションのハッシュ・コード */
    private long portHashCode;

    /** 入渠ドックコレクションのハッシュ・コード */
    private long ndockHashCode;

    /** 任務コレクションのハッシュ・コード */
    private long questHashCode;

    /** 戦果のハッシュ・コード */
    private long achievementHashCode;

    /** 遠征通知のタイムスタンプ */
    private Map<Integer, Long> timeStampMission = new HashMap<>();

    /** 入渠通知のタイムスタンプ */
    private Map<Integer, Long> timeStampNdock = new HashMap<>();

    @FXML
    private MainMenuController mainMenuController;

    @FXML
    private Button item;

    @FXML
    private Button ship;

    @FXML
    private TabPane fleetTab;

    @FXML
    private TitledPane achievementPane;

    @FXML
    private Label achievementLabel1;

    @FXML
    private Label achievementValue1;

    @FXML
    private Label achievementLabel2;

    @FXML
    private Label achievementValue2;

    @FXML
    private TitledPane missionPane;

    @FXML
    private VBox missionbox;

    @FXML
    private TitledPane ndockPane;

    @FXML
    private VBox akashiTimer;

    @FXML
    private VBox ndockbox;

    @FXML
    private TitledPane questPane;

    @FXML
    private VBox questbox;

    private AudioClip clip;

    @FXML
    void initialize() {
        try {
            // サーバーの起動に失敗した場合にダイアログを表示するために、UIスレッドの初期化後にサーバーを起動する必要がある
            ProxyHolder.getInstance().start();

            this.itemFormat = this.item.getText();
            this.shipFormat = this.ship.getText();

            // メニューにメイン画面のコントローラを渡す
            this.mainMenuController.setParentController(this);

            Timeline timeline = new Timeline(1);
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.getKeyFrames().add(new KeyFrame(
                    javafx.util.Duration.seconds(1),
                    this::update));

            // 古い任務を除く
            AppQuestCollection.get()
                    .update();

            timeline.play();

            // 開始処理
            LogBookCoreServices.getServiceProviders(StartUp.class)
                    .map(Thread::new)
                    .peek(t -> t.setDaemon(true))
                    .forEach(Thread::start);
        } catch (Exception e) {
            LoggerHolder.get().error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * 所有装備
     *
     * @param e ActionEvent
     */
    @FXML
    void items(ActionEvent e) {
        try {
            InternalFXMLLoader.showWindow("logbook/gui/item.fxml", this.getWindow(), "所有装備一覧");
        } catch (Exception ex) {
            LoggerHolder.get().error("所有装備一覧の初期化に失敗しました", ex);
        }
    }

    /**
     * 所有艦娘
     *
     * @param e ActionEvent
     */
    @FXML
    void ships(ActionEvent e) {
        try {
            InternalFXMLLoader.showWindow("logbook/gui/ship.fxml", this.getWindow(), "所有艦娘一覧");
        } catch (Exception ex) {
            LoggerHolder.get().error("所有艦娘一覧の初期化に失敗しました", ex);
        }
    }

    /**
     * アイテム一覧
     * @param e ActionEvent
     */
    public void useitems(ActionEvent e) {
        try {
            InternalFXMLLoader.showWindow("logbook/gui/useitem.fxml", this.getWindow(), "アイテム一覧");
        } catch (Exception ex) {
            LoggerHolder.get().error("アイテム一覧の初期化に失敗しました", ex);
        }
    }

    /**
     * 画面の更新
     *
     * @param e
     */
    void update(ActionEvent e) {
        try {
            // 所有装備/所有艦娘
            this.button();
            // 戦果
            this.achievement();
            // 艦隊タブ・遠征
            this.checkPort();
            // 泊地修理タイマー
            this.akashiTimer();
            // 入渠ドック
            this.ndock();
            // 任務
            this.quest();

            // 遠征・入渠完了時に通知をする
            if (AppConfig.get().isUseNotification()) {
                // 遠征の通知
                this.checkNotifyMission();
                // 入渠ドックの通知
                this.checkNotifyNdock();
            }
        } catch (Exception ex) {
            LoggerHolder.get().error("設定の初期化に失敗しました", ex);
        }
    }

    /**
     * 所有装備/所有艦娘の更新
     */
    private void button() {
        // 装備
        Map<Integer, SlotitemMst> itemMstMap = SlotitemMstCollection.get().getSlotitemMap();
        long slotitem = SlotItemCollection.get()
                .getSlotitemMap()
                .values()
                .stream()
                .filter(item -> SlotItemType.toSlotItemType(itemMstMap.get(item.getSlotitemId())).isCount())
                .count();
        Integer maxSlotitem = Basic.get()
                .getMaxSlotitem();
        this.item.setText(MessageFormat.format(this.itemFormat, slotitem, maxSlotitem));

        boolean itemFully = maxSlotitem - slotitem <= AppConfig.get().getItemFullyThreshold();
        if (itemFully) {
            if (!this.item.getStyleClass().contains(FULLY_CLASS)) {
                this.item.getStyleClass().add(FULLY_CLASS);
            }
        } else {
            this.item.getStyleClass().remove(FULLY_CLASS);
        }

        // 艦娘
        Integer chara = ShipCollection.get()
                .getShipMap()
                .size();
        Integer maxChara = Basic.get()
                .getMaxChara();
        this.ship.setText(MessageFormat.format(this.shipFormat, chara, maxChara));

        boolean shipFully = maxChara - chara <= AppConfig.get().getShipFullyThreshold();
        if (shipFully) {
            if (!this.ship.getStyleClass().contains(FULLY_CLASS)) {
                this.ship.getStyleClass().add(FULLY_CLASS);
            }
        } else {
            this.ship.getStyleClass().remove(FULLY_CLASS);
        }
    }

    /**
     * 戦果の計算
     */
    private void achievement() {
        long exp = Optional.ofNullable(Basic.get()).map(Basic::getExperience).map(Integer::longValue).orElse(0L);
        boolean show = AppConfig.get().isShowAchievement();
        Map<String, Object> map = new HashMap<>();
        map.put("exp", exp);
        long hash = hashCode(map, show);
        if (hash == this.achievementHashCode) {
            return;
        }
        this.achievementHashCode = hash;

        this.achievementPane.setVisible(show);
        this.achievementPane.setManaged(show);

        final ZoneId JST = ZoneId.of("UTC+07:00");
        final DecimalFormat format = new DecimalFormat("0.000");
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d日a");
        AppExpRecords records = AppExpRecords.get();
        if (records.getExp12h() != null) {
            ZonedDateTime start = Instant.ofEpochMilli(records.getTime12h()).atZone(JST);
            this.achievementLabel2.setText(start.format(dateFormatter) + "(" + (start.getHour()+2) + "- )");
            this.achievementValue2.setText(format.format((exp - records.getExp12h())*7f/10000));
        } else {
            this.achievementLabel2.setText("");
            this.achievementValue2.setText("");
        }
        if (records.getExp1d() != null && records.getTime1d() != records.getTime12h()) {
            ZonedDateTime start = Instant.ofEpochMilli(records.getTime1d()).atZone(JST);
            this.achievementLabel1.setText(start.format(dateFormatter) + "(" + (start.getHour()+2) + "-" + (start.getHour()/12*12+14)+")");
            this.achievementValue1.setText(format.format((records.getExp12h()-records.getExp1d())*7f/10000));
        } else {
            this.achievementLabel1.setText("");
            this.achievementValue1.setText("");
        }
    }

    /**
     * 艦隊の確認
     */
    private void checkPort() {
        Map<Integer, DeckPort> ports = DeckPortCollection.get()
                .getDeckPortMap();
        boolean show = AppConfig.get().isShowMission();
        long newHashCode = hashCode(ports, show);
        boolean change = this.portHashCode != newHashCode;
        this.portHashCode = newHashCode;

        this.fleetTab(change);
        this.mission(change, show);
    }

    /**
     * 艦隊タブの更新
     *
     * @param change 艦隊の変更フラグ
     */
    private void fleetTab(boolean change) {
        Map<Integer, DeckPort> ports = DeckPortCollection.get()
                .getDeckPortMap();
        ObservableList<Tab> tabs = this.fleetTab.getTabs();
        if (change) {
            int tabsize = (int) tabs.stream()
                    .map(Tab::getContent)
                    .filter(e -> e instanceof FleetTabPane)
                    .count();
            if (ports.size() != tabsize) {
                tabs.removeIf(e -> e.getContent() instanceof FleetTabPane);
                for (DeckPort port : ports.values()) {
                    FleetTabPane pane = new FleetTabPane(port);
                    Tab tab = new Tab(port.getName(), pane);
                    tab.setClosable(false);
                    tab.getStyleClass().removeIf(s -> !s.equals("tab"));
                    Optional.ofNullable(pane.tabStyle())
                            .ifPresent(tab::setStyle);
                    tabs.add(tab);
                }
            } else {
                Iterator<DeckPort> portIte = ports.values().iterator();
                Iterator<Tab> tabIte = tabs.iterator();
                while (portIte.hasNext()) {
                    DeckPort port = portIte.next();
                    Tab tab = null;
                    while (tabIte.hasNext()) {
                        tab = tabIte.next();
                        if (tab.getContent() instanceof FleetTabPane)
                            break;
                    }
                    if (tab != null) {
                        tab.setText(port.getName());
                        FleetTabPane pane = (FleetTabPane) tab.getContent();
                        pane.update(port);
                        tab.getStyleClass().removeIf(s -> !s.equals("tab"));
                        Optional.ofNullable(pane.tabStyle())
                                .ifPresent(tab::setStyle);
                    }
                }
            }
        } else {
            for (Tab tab : tabs) {
                Node node = tab.getContent();
                if (node instanceof FleetTabPane) {
                    FleetTabPane pane = (FleetTabPane) node;
                    pane.update();
                    tab.getStyleClass().removeIf(s -> !s.equals("tab"));
                    Optional.ofNullable(pane.tabStyle())
                            .ifPresent(tab::setStyle);
                }
            }
        }
    }

    /**
     * 遠征の更新
     *
     * @param change 艦隊の変更フラグ
     * @param show 表示するフラグ
     */
    private void mission(boolean change, boolean show) {
        ObservableList<Node> mission = this.missionbox.getChildren();
        if (change) {
            this.missionPane.setVisible(show);
            this.missionPane.setManaged(show);

            Map<Integer, DeckPort> ports = DeckPortCollection.get()
                    .getDeckPortMap();
            mission.clear();
            ports.values().stream()
                    .skip(1)
                    .map(MissionPane::new)
                    .forEach(mission::add);
        } else {
            for (Node node : mission) {
                if (node instanceof MissionPane) {
                    ((MissionPane) node).update();
                }
            }
        }
    }

    /**
     * 泊地修理タイマー
     */
    private void akashiTimer() {
        ObservableList<Node> nodes = this.akashiTimer.getChildren();

        if (AppCondition.get().getAkashiTimer() == 0) {
            if (!nodes.isEmpty()) {
                nodes.clear();
            }
        } else {
            if (nodes.isEmpty()) {
                nodes.add(new AkashiTimerPane());
            } else {
                ((AkashiTimerPane) nodes.get(0)).update();
            }
        }
    }

    /**
     * 入渠ドックの更新
     */
    private void ndock() {
        Map<Integer, Ndock> ndockMap = NdockCollection.get()
                .getNdockMap();
        ObservableList<Node> ndock = this.ndockbox.getChildren();
        boolean show = AppConfig.get().isShowNdock();
        long newHashCode = hashCode(ndockMap, show);
        if (this.ndockHashCode != newHashCode) {
            this.ndockPane.setVisible(show);
            this.ndockPane.setManaged(show);

            // ハッシュ・コードが変わっている場合入渠ドックの更新
            ndock.clear();
            ndockMap.values()
                    .stream()
                    .filter(n -> 1 < n.getCompleteTime())
                    .map(NdockPane::new)
                    .forEach(ndock::add);
            // ハッシュ・コードの更新
            this.ndockHashCode = newHashCode;
        } else {
            // ハッシュ・コードが変わっていない場合updateメソッドを呼ぶ
            for (Node node : ndock) {
                if (node instanceof NdockPane) {
                    ((NdockPane) node).update();
                }
            }
        }
    }

    /**
     * 任務の更新
     */
    private void quest() {
        Map<Integer, AppQuest> questMap = AppQuestCollection.get()
                .getQuest();
        boolean show = AppConfig.get().isShowQuest();
        long newHashCode = hashCode(questMap, show);
        if (this.questHashCode != newHashCode) {
            this.questPane.setVisible(show);
            this.questPane.setManaged(show);
            // ハッシュ・コードが変わっている場合任務の更新
            ObservableList<Node> quest = this.questbox.getChildren();
            quest.clear();
            questMap.values()
                    .stream()
                    // 受諾中の任務を上に持ってくる
                    .sorted(Comparator.comparing(AppQuest::isActive).reversed())
                    .map(QuestPane::new)
                    .forEach(quest::add);
            // ハッシュ・コードの更新
            this.questHashCode = newHashCode;
        }
    }

    /**
     * 遠征の通知をチェックします
     */
    private void checkNotifyMission() {
        Map<Integer, DeckPort> ports = DeckPortCollection.get()
                .getDeckPortMap();
        for (DeckPort port : ports.values()) {
            // 0=未出撃, 1=遠征中, 2=遠征帰還, 3=遠征中止
            int state = port.getMission().get(0).intValue();
            // 帰還時間
            long time = port.getMission().get(2);

            if (0 == state) {
                this.timeStampMission.put(port.getId(), 0L);
            } else {
                // 残り時間を計算
                Duration now = Duration.ofMillis(time - System.currentTimeMillis());
                // 前回の通知の時間
                long timeStamp = this.timeStampMission.getOrDefault(port.getId(), 0L);
                if (this.requireNotify(now, timeStamp, AppConfig.get().isUseRemind())) {
                    this.timeStampMission.put(port.getId(), System.currentTimeMillis());
                    this.pushNotifyMission(port);
                }
            }
        }
    }

    /**
     * 遠征通知
     *
     * @param port 艦隊
     */
    private void pushNotifyMission(DeckPort port) {
        if (AppConfig.get().isUseToast()) {
            String message = Messages.getString("mission.complete", port.getName()); //$NON-NLS-1$
            Tools.Controls.showNotify(null, "遠征完了", message);
        }
        if (AppConfig.get().isUseSound()) {
            this.soundNotify(Paths.get(AppConfig.get().getMissionSoundDir()));
        }
        // 棒読みちゃん連携
        if (AppBouyomiConfig.get().isEnable()) {
            this.sendBouyomiMissionComplete(port);
        }
    }

    /**
     * 入渠ドックの通知をチェックします
     */
    private void checkNotifyNdock() {
        Map<Integer, Ndock> ndockMap = NdockCollection.get()
                .getNdockMap();

        for (Ndock ndock : ndockMap.values()) {
            // 完了時間
            long time = ndock.getCompleteTime();

            if (1 > time) {
                this.timeStampNdock.put(ndock.getId(), 0L);
            } else {
                // 残り時間を計算
                Duration now = Duration.ofMillis(time - System.currentTimeMillis());
                // 前回の通知の時間
                long timeStamp = this.timeStampNdock.getOrDefault(ndock.getId(), 0L);

                if (this.requireNotify(now, timeStamp, false)) {
                    this.timeStampNdock.put(ndock.getId(), System.currentTimeMillis());
                    this.pushNotifyNdock(ndock);
                }
            }
        }
    }

    /**
     * 入渠ドックの通知
     *
     * @param ndock 入渠ドック
     */
    private void pushNotifyNdock(Ndock ndock) {
        if (AppConfig.get().isUseToast()) {
            Ship ship = ShipCollection.get()
                    .getShipMap()
                    .get(ndock.getShipId());
            String name = Ships.shipMst(ship)
                    .map(ShipMst::getName)
                    .orElse("");
            String message = Messages.getString("ship.ndock", name, ship.getLv()); //$NON-NLS-1$

            ImageView img = new ImageView(Ships.shipWithItemImage(ship));

            Tools.Controls.showNotify(img, "修復完了", message);
        }
        if (AppConfig.get().isUseSound()) {
            this.soundNotify(Paths.get(AppConfig.get().getNdockSoundDir()));
        }
        // 棒読みちゃん連携
        if (AppBouyomiConfig.get().isEnable()) {
            this.sendBouyomiNdockComplete(ndock);
        }
    }

    /**
     * 通知するか判断します
     *
     * @param now 残り時間
     * @param timeStamp 前回の通知の時間
     * @param remind リマインド
     */
    private boolean requireNotify(Duration now, long timeStamp, boolean remind) {
        if (now.compareTo(NOTIFY) <= 0) {
            // 前回の通知からの経過時間
            Duration course = Duration.ofMillis(System.currentTimeMillis() - timeStamp);
            // リマインド間隔
            Duration interval = Duration.ofSeconds(AppConfig.get().getRemind());
            if (course.compareTo(interval) >= 0) {
                if (timeStamp == 0L || remind) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * サウンド通知
     */
    private void soundNotify(Path dir) {
        if (this.clip == null || !this.clip.isPlaying()) {
            try {
                Path p = AudiosUtil.randomAudioFile(dir);
                if (p != null) {
                    this.clip = new AudioClip(p.toUri().toString());
                    this.clip.setVolume(AppConfig.get().getSoundLevel() / 100D);
                    this.clip.play();
                }
            } catch (Exception e) {
                LoggerHolder.get().warn("サウンド通知に失敗しました", e);
            }
        }
    }

    /**
     * 棒読みちゃん連携
     *
     * @param port 艦隊
     */
    private void sendBouyomiMissionComplete(DeckPort port) {
        int target = port.getMission().get(1).intValue();
        Optional<Mission> mission = Optional.ofNullable(MissionCollection.get()
                .getMissionMap()
                .get(target));
        String missionName = mission.map(Mission::getName).orElse("");

        BouyomiChanUtils.speak(Type.MissionComplete,
                Tuple.of("${fleetName}", port.getName()),
                Tuple.of("${fleetNumber}", String.valueOf(port.getId())),
                Tuple.of("${missionName}", missionName));
    }

    /**
     * 棒読みちゃん連携
     *
     * @param ndock 入渠ドック
     */
    private void sendBouyomiNdockComplete(Ndock ndock) {
        Ship ship = ShipCollection.get()
                .getShipMap()
                .get(ndock.getShipId());

        String hiragana = Ships.shipMst(ship)
                .map(ShipMst::getYomi)
                .orElse("");
        String kanji = Ships.shipMst(ship)
                .map(ShipMst::getName)
                .orElse("");

        BouyomiChanUtils.speak(Type.NdockComplete,
                Tuple.of("${hiraganaName}", hiragana),
                Tuple.of("${kanjiName}", kanji));
    }

    private static long hashCode(Map<?, ?> map, boolean show) {
        long h = 59;
        Iterator<?> i = map.entrySet().iterator();
        while (i.hasNext()) {
            h *= 63;
            h += i.next().hashCode();
        }
        h *= 63;
        h += Boolean.hashCode(show);
        return h;
    }
}
