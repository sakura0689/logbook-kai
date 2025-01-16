package logbook.api;

import java.util.Map;

import jakarta.json.JsonObject;
import logbook.bean.SlotItem;
import logbook.bean.SlotItemCollection;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_kousyou/destroyitem2
 *
 */
@API("/kcsapi/api_req_kousyou/destroyitem2")
public class ApiReqKousyouDestroyitem2 implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        String apiSlotitemIds = req.getParameter("api_slotitem_ids");
        if (apiSlotitemIds != null) {
            Map<Integer, SlotItem> itemMap = SlotItemCollection.get()
                    .getSlotitemMap();
            for (String apiSlotitemId : apiSlotitemIds.split(",")) {
                Integer itemId = Integer.valueOf(apiSlotitemId);
                // 装備を廃棄する
                itemMap.remove(itemId);
            }
        }
    }

}
