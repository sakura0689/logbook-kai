package logbook.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.CombinedBattleMidnightBattle;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.internal.kancolle.PhaseState;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_req_combined_battle/midnight_battle
 *
 */
@API("/kcsapi/api_req_combined_battle/midnight_battle")
public class ApiReqCombinedBattleMidnightBattle implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {

            BattleLog log = AppCondition.get().getBattleResult();
            if (log != null) {
                log.setMidnight(CombinedBattleMidnightBattle.toBattle(data));
                // ローデータを設定する
                if (AppConfig.get().isIncludeRawData()) {
                    BattleLog.setRawData(log, BattleLog.RawData::setMidnight, data, req);
                }
                if (AppConfig.get().isApplyBattle()) {
                    // 艦隊を更新
                    PhaseState p = new PhaseState(log);
                    p.apply(log.getBattle());
                    p.apply(log.getMidnight());
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
