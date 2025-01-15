package logbook.api;

import java.util.Arrays;
import java.util.Map;

import javax.json.JsonObject;

import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.SlotItem;
import logbook.bean.SlotItemCollection;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_kousyou/destroyship
 *
 */
@API("/kcsapi/api_req_kousyou/destroyship")
public class ApiReqKousyouDestroyship implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        String apiShipId = req.getParameter("api_ship_id");
        String apiSlotDestFlag = req.getParameter("api_slot_dest_flag");

        if (apiShipId != null) {
            boolean slotDest = "1".equals(apiSlotDestFlag);
            Arrays.stream(apiShipId.split(","))
                    .map(Integer::parseInt)
                    .forEach(id -> this.destroyShip(id, slotDest));
        }
    }

    private void destroyShip(Integer shipId, boolean slotDest) {
        // 艦娘を外す
        Ship ship = ShipCollection.get()
                .getShipMap()
                .remove(shipId);
        if (slotDest && ship != null) {
            Map<Integer, SlotItem> itemMap = SlotItemCollection.get()
                    .getSlotitemMap();
            // 持っている装備を廃棄する
            for (Integer itemId : ship.getSlot()) {
                itemMap.remove(itemId);
            }
            // 補強増設
            itemMap.remove(ship.getSlotEx());
        }
    }
}
