package logbook.bean;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.BattleTypes.IMidnightBattle;
import logbook.internal.kancolle.PhaseState;
import logbook.internal.logger.LoggerHolder;
import logbook.net.RequestMetaData;
import lombok.Data;

/**
 * 戦闘ログ
 *
 */
@Data
public class BattleLog implements Serializable {

    private static final long serialVersionUID = -6163406897520116392L;

    /** 連合艦隊 */
    private CombinedType combinedType = CombinedType.未結成;

    /** 開始/進撃(順番に複数存在する) */
    private List<MapStartNext> next = new ArrayList<>();

    /** 戦闘(昼戦、特殊夜戦) */
    private IFormation battle;

    /** 夜戦 */
    private IMidnightBattle midnight;

    /** 戦闘結果 */
    private BattleResult result;

    /** 艦隊スナップショット */
    private Map<Integer, List<Ship>> deckMap;

    /** 装備スナップショット */
    private Map<Integer, SlotItem> itemMap;

    /** 退避艦IDスナップショット */
    private Set<Integer> escape;

    /** 日時(戦闘結果の取得日時) */
    private String time;

    /** 戦闘カウント */
    private Integer battleCount;

    /** ルート */
    private List<String> route;

    /** ローデータ */
    private RawData raw;

    /** 演習かどうか */
    private boolean isPractice;

    /**
     * 艦隊スナップショットを作成します
     * 
     * @param log     戦闘ログ
     * @param dockIds 艦隊ID
     */
    public static void snapshot(BattleLog log, Integer... dockIds) {
        Map<Integer, Ship> shipMap = ShipCollection.get()
                .getShipMap();
        Map<Integer, SlotItem> itemMap = SlotItemCollection.get()
                .getSlotitemMap();

        Map<Integer, List<Ship>> deckMap = new HashMap<>();
        Map<Integer, SlotItem> cloneItem = new HashMap<>();

        for (Integer dockId : dockIds) {
            List<Ship> ships = new ArrayList<>();
            for (Integer shipId : DeckPortCollection.get()
                    .getDeckPortMap()
                    .get(dockId)
                    .getShip()) {
                Ship ship = shipMap.get(shipId);
                if (ship != null) {
                    ship = ship.clone();
                    if (ship.getSlot() != null) {
                        for (Integer itemId : ship.getSlot()) {
                            SlotItem item = itemMap.get(itemId);
                            if (item != null) {
                                cloneItem.put(itemId, item);
                            }
                        }
                        {
                            SlotItem item = itemMap.get(ship.getSlotEx());
                            if (item != null) {
                                cloneItem.put(ship.getSlotEx(), item);
                            }
                        }
                    }
                }
                ships.add(ship);
            }
            deckMap.put(dockId, ships);
        }
        log.setDeckMap(deckMap);
        log.setItemMap(cloneItem);
        log.setEscape(AppCondition.get().getEscape());
    }

    /**
     * ローデータを設定する
     *
     * @param log      戦闘ログ
     * @param consumer setter
     * @param json     設定するjson
     * @param req      リクエスト
     */
    @SuppressWarnings("unchecked")
    public static void setRawData(BattleLog log, BiConsumer<RawData, ApiData> consumer,
            JsonObject json, RequestMetaData req) {
        RawData rawData = log.getRaw();
        if (rawData == null) {
            rawData = new RawData();
            log.setRaw(rawData);
        }
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter(1024 * 4);
        try (JsonWriter jw = Json.createWriter(sw)) {
            jw.writeObject(json);
        }
        try {
            ApiData data = new ApiData();
            data.setUri(req.getRequestURI());
            data.setApidata(mapper.readValue(sw.toString(), Map.class));

            consumer.accept(rawData, data);
        } catch (Exception e) {
            LoggerHolder.get().warn("ローデータの設定に失敗しました", e);
        }
    }

    /**
     * ローデータ
     */
    @Data
    public static class RawData implements Serializable {

        private static final long serialVersionUID = 4291532219144781718L;

        /** 戦闘(昼戦、特殊夜戦) */
        private ApiData battle;

        /** 夜戦 */
        private ApiData midnight;

        /** 戦闘結果 */
        private ApiData result;
    }

    /**
     * ローデータ
     */
    @Data
    public static class ApiData implements Serializable {

        private static final long serialVersionUID = 8729885890089448397L;

        /** URI */
        private String uri;

        /** api_data */
        @JsonProperty("api_data")
        private Map<Object, Object> apidata;
    }

    /**
     * 戦闘フェイズの情報で戦闘ログを更新し、複製を返します。
     *
     * @param log 元の戦闘ログ
     * @param p   戦闘フェイズ情報
     * @return 更新された戦闘ログの複製
     */
    public static BattleLog updatePhaseState(BattleLog log, PhaseState p) {
        BattleLog newLog = new BattleLog();
        newLog.setCombinedType(log.getCombinedType());
        newLog.setNext(log.getNext());
        newLog.setMidnight(log.getMidnight());
        newLog.setResult(log.getResult());
        newLog.setItemMap(log.getItemMap());
        newLog.setEscape(log.getEscape());
        newLog.setTime(log.getTime());
        newLog.setBattleCount(log.getBattleCount());
        newLog.setRoute(log.getRoute());
        newLog.setRaw(log.getRaw());
        newLog.setPractice(log.isPractice());

        IFormation battle = log.getBattle();

        // deckMapのディープコピーとHPの更新をマージ
        if (log.getDeckMap() != null) {
            Map<Integer, List<Ship>> newDeckMap = new HashMap<>();
            boolean combined = battle != null && battle.isICombinedBattle();
            int dockId = battle != null ? battle.getDockId() : 1;

            for (Map.Entry<Integer, List<Ship>> entry : log.getDeckMap().entrySet()) {
                List<Ship> list = entry.getValue();
                if (list != null) {
                    List<Ship> newList = new ArrayList<>();
                    // 対応する戦闘後艦隊情報を取得
                    List<Ship> afterShips = null;
                    if (combined) {
                        if (entry.getKey() == 1) {
                            afterShips = p.getAfterFriend();
                        } else if (entry.getKey() == 2) {
                            afterShips = p.getAfterFriendCombined();
                        }
                    } else if (entry.getKey() == dockId) {
                        afterShips = p.getAfterFriend();
                    }

                    for (int i = 0; i < list.size(); i++) {
                        Ship s = list.get(i);
                        if (s != null) {
                            Ship cloned = s.clone();
                            if (afterShips != null && i < afterShips.size()) {
                                Ship as = afterShips.get(i);
                                if (as != null) {
                                    cloned.setNowhp(as.getNowhp());
                                }
                            }
                            newList.add(cloned);
                        } else {
                            newList.add(null);
                        }
                    }
                    newDeckMap.put(entry.getKey(), newList);
                }
            }
            newLog.setDeckMap(newDeckMap);
        }

        // battleのディープコピーとHP更新
        if (battle != null) {
            try {
                IFormation copiedBattle = (IFormation) battle.copy();
                copiedBattle.setFNowhps(extractHps(p.getAfterFriend()));
                copiedBattle.setENowhps(extractHps(p.getAfterEnemy()));
                if (copiedBattle.isICombinedBattle()) {
                    copiedBattle.asICombinedBattle().setFNowhpsCombined(extractHps(p.getAfterFriendCombined()));
                }
                if (copiedBattle.isICombinedEcBattle()) {
                    copiedBattle.asICombinedEcBattle().setENowhpsCombined(extractHps(p.getAfterEnemyCombined()));
                }
                newLog.setBattle(copiedBattle);
            } catch (Exception e) {
                LoggerHolder.get().warn("戦闘ログの戦闘後ステータス更新に失敗しました(battle)", e);
                newLog.setBattle(battle);
            }
        }

        return newLog;
    }

    private static List<Integer> extractHps(List<? extends Chara> charas) {
        if (charas == null)
            return null;
        return charas.stream()
                .map(c -> c != null ? c.getNowhp() : 0)
                .collect(Collectors.toList());
    }
}
