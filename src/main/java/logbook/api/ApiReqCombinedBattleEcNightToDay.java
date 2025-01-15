package logbook.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.BattleTypes.CombinedType;
import logbook.internal.kancolle.PhaseState;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;
import logbook.bean.CombinedBattleEcNightToDay;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;

/**
 * /kcsapi/api_req_combined_battle/ec_night_to_day
 *
 */
@API("/kcsapi/api_req_combined_battle/ec_night_to_day")
public class ApiReqCombinedBattleEcNightToDay implements APIListenerSpi {

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

                log.setBattle(CombinedBattleEcNightToDay.toBattle(data));
                // ローデータを設定する
                if (AppConfig.get().isIncludeRawData()) {
                    BattleLog.setRawData(log, BattleLog.RawData::setBattle, data, req);
                }
                // 艦隊スナップショットを作る
                if (log.getCombinedType() != CombinedType.未結成 && AppCondition.get().getDeckId() == 1) {
                    BattleLog.snapshot(log, 1, 2);
                } else {
                    BattleLog.snapshot(log, AppCondition.get().getDeckId());
                }
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
