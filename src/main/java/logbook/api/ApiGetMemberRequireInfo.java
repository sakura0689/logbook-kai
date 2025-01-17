package logbook.api;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import logbook.bean.Basic;
import logbook.bean.SlotItem;
import logbook.bean.SlotItemCollection;
import logbook.bean.Useitem;
import logbook.bean.UseitemCollection;
import logbook.internal.util.JsonHelper;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_get_member/require_info
 *
 */
@API("/kcsapi/api_get_member/require_info")
public class ApiGetMemberRequireInfo implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            this.apiBasic(data.getJsonObject("api_basic"));
            this.apiSlotItem(data.getJsonArray("api_slot_item"));
            this.apiUseitem(data.getJsonArray("api_useitem"));
        }
    }

    /**
     * api_data.api_basic
     *
     * @param object api_basic
     */
    private void apiBasic(JsonObject object) {
        Basic.updateBasic(Basic.get(), object);
        // require_info の時の api_basic は戦果が送られてこないので更新すべきでない
        //AppExpRecords.get().update(Basic.get());
    }

    /**
     * api_data.api_slot_item
     *
     * @param array api_slot_item
     */
    private void apiSlotItem(JsonArray array) {
        SlotItemCollection.get()
                .setSlotitemMap(JsonHelper.toMap(array, SlotItem::getId, SlotItem::toSlotItem));
    }

    /**
     * api_data.api_useitem
     *
     * @param array api_useitem
     */
    private void apiUseitem(JsonArray array) {
        UseitemCollection.get()
                .setUseitemMap(JsonHelper.toMap(array, Useitem::getId, Useitem::toUseitem));
    }

}
