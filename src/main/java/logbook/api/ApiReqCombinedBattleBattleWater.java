package logbook.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.CombinedBattleBattleWater;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.internal.kancolle.PhaseState;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_req_combined_battle/battle_water
 *
 */
@API("/kcsapi/api_req_combined_battle/battle_water")
public class ApiReqCombinedBattleBattleWater implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {

            AppCondition condition = AppCondition.get();
            BattleLog log = condition.getBattleResult();
            if (log != null) {
                condition.setBattleCount(condition.getBattleCount() + 1);
                log.setBattleCount(condition.getBattleCount());
                log.setRoute(condition.getRoute());

                log.setBattle(CombinedBattleBattleWater.toBattle(data));
                // ローデータを設定する
                if (AppConfig.get().isIncludeRawData()) {
                    BattleLog.setRawData(log, BattleLog.RawData::setBattle, data, req);
                }
                // 艦隊スナップショットを作る
                BattleLog.snapshot(log, 1, 2);
                if (AppConfig.get().isApplyBattle()) {
                    // 艦隊を更新
                    PhaseState p = new PhaseState(log);
                    p.apply(log.getBattle());
                    ShipCollection.get()
                            .getShipMap()
                            .putAll(Stream.of(p.getAfterFriend(), p.getAfterFriendCombined())
                                    .flatMap(List::stream)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toMap(Ship::getId, v -> v)));
                }
            }
        }
    }

}
