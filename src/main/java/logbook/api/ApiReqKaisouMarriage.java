package logbook.api;

import java.util.Map;

import javax.json.JsonObject;

import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_kaisou/marriage
 *
 */
@API("/kcsapi/api_req_kaisou/marriage")
public class ApiReqKaisouMarriage implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            Map<Integer, Ship> shipMap = ShipCollection.get()
                    .getShipMap();

            Integer shipId = Integer.valueOf(req.getParameter("api_id"));
            shipMap.put(shipId, Ship.toShip(data));
        }
    }

}
