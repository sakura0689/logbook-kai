package logbook.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import logbook.bean.Mapinfo;
import logbook.bean.Mapinfo.AirBase;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_air_corps/set_action
 *
 */
@API("/kcsapi/api_req_air_corps/set_action")
public class ApiReqAirCorpsSetAction implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        int areaId = Integer.parseInt(req.getParameter("api_area_id"));
        List<Integer> baseIds = Arrays.stream(req.getParameter("api_base_id").split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        List<Integer> kinds = Arrays.stream(req.getParameter("api_action_kind").split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        for (int i = 0; i < baseIds.size(); i++) {
            int index = i;
            AirBase airBase = Mapinfo.get()
                    .getAirBase()
                    .stream()
                    .filter(b -> b.getAreaId() == areaId)
                    .filter(b -> b.getRid().equals(baseIds.get(index)))
                    .findFirst()
                    .orElse(null);
            if (airBase != null) {
                airBase.setActionKind(kinds.get(index));
            }
        }
    }
}
