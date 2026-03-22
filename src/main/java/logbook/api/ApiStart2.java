package logbook.api;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import logbook.bean.AppConfig;
import logbook.bean.EquipExslotShip;
import logbook.bean.EquipExslotShipCollection;
import logbook.bean.Maparea;
import logbook.bean.MapareaCollection;
import logbook.bean.MapinfoMst;
import logbook.bean.MapinfoMstCollection;
import logbook.bean.Mission;
import logbook.bean.MissionCollection;
import logbook.bean.ShipMst;
import logbook.bean.ShipMstCollection;
import logbook.bean.Shipgraph;
import logbook.bean.ShipgraphCollection;
import logbook.bean.Shipupgrade;
import logbook.bean.ShipupgradeCollection;
import logbook.bean.SlotitemEquipLimitExslotCollection;
import logbook.bean.SlotitemEquipShipCollection;
import logbook.bean.SlotitemEquiptype;
import logbook.bean.SlotitemEquiptypeCollection;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;
import logbook.bean.Stype;
import logbook.bean.StypeCollection;
import logbook.bean.UseitemMst;
import logbook.bean.UseitemMstCollection;
import logbook.internal.Config;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_start2
 *
 */
@API("/kcsapi/api_start2/getData")
public class ApiStart2 implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            this.apiMstShip(data.getJsonArray("api_mst_ship"));
            this.apiMstShipgraph(data.getJsonArray("api_mst_shipgraph"));
            this.apiMstSlotitemEquiptype(data.getJsonArray("api_mst_slotitem_equiptype"));
            this.apiMstStype(data.getJsonArray("api_mst_stype"));
            this.apiMstSlotitem(data.getJsonArray("api_mst_slotitem"));
            this.apiMstUseitem(data.getJsonArray("api_mst_useitem"));
            this.apiMstMission(data.getJsonArray("api_mst_mission"));
            this.apiMstMaparea(data.getJsonArray("api_mst_maparea"));
            this.apiMstMapinfo(data.getJsonArray("api_mst_mapinfo"));
            this.apiMstShipupgrade(data.getJsonArray("api_mst_shipupgrade"));
            this.apiMstEquipLimitExslot(data.getJsonObject("api_mst_equip_limit_exslot"));
            this.apiMstEquipShip(data.getJsonObject("api_mst_equip_ship"));
            this.apiMstEquipExslotShip(data.getJsonObject("api_mst_equip_exslot_ship"));
            this.store(data);
            
            if (LoggerHolder.get().isDebugEnabled()) {
                Set<String> remainingKeys = new HashSet<>(data.keySet());
                remainingKeys.remove("api_mst_ship");
                remainingKeys.remove("api_mst_shipgraph");
                remainingKeys.remove("api_mst_slotitem_equiptype");
                remainingKeys.remove("api_mst_stype");
                remainingKeys.remove("api_mst_slotitem");
                remainingKeys.remove("api_mst_useitem");
                remainingKeys.remove("api_mst_mission");
                remainingKeys.remove("api_mst_maparea");
                remainingKeys.remove("api_mst_mapinfo");
                
                remainingKeys.remove("api_mst_equip_exslot"); //拡張Slot装備可能カテゴリ
                remainingKeys.remove("api_mst_furniture"); //家具
                remainingKeys.remove("api_mst_furnituregraph"); //家具
                remainingKeys.remove("api_mst_bgm"); //BGM
                remainingKeys.remove("api_mst_mapbgm"); //BGM
                remainingKeys.remove("api_mst_payitem"); //アイテム屋商品一覧                
                remainingKeys.remove("api_mst_shipupgrade"); //艦娘特殊改装情報
                remainingKeys.remove("api_mst_equip_limit_exslot"); //艦娘別補強スロット装備制限
                remainingKeys.remove("api_mst_equip_ship"); //艦娘別装備制限
                remainingKeys.remove("api_mst_equip_exslot_ship"); //装備別補強スロット装備追加条件
                
                if (!remainingKeys.isEmpty()) {
                    for (String key : remainingKeys) {
                        try {
                            LoggerHolder.get().debug("未使用key:" + key + " data:" + data.get(key));
                        } catch (Exception e) {
                            LoggerHolder.get().debug("未使用key:" + key, e);
                        }
                    }
                }
            }
        }
        Config.getDefault().store();
    }

    /**
     * api_data.api_mst_ship
     *
     * @param array api_mst_ship
     */
    private void apiMstShip(JsonArray array) {
        ShipMstCollection.get()
                .setShipMap(JsonHelper.toMap(array, ShipMst::getId, ShipMst::toShip));
    }

    /**
     * api_data.api_mst_shipgraph
     *
     * @param array api_mst_shipgraph
     */
    private void apiMstShipgraph(JsonArray array) {
        Map<Integer, ShipMst> map = ShipMstCollection.get()
                .getShipMap();
        for (JsonValue val : array) {
            JsonObject json = (JsonObject) val;
            Integer key = json.getInt("api_id");
            ShipMst bean = map.get(key);
            if (bean != null) {
                bean.setGraph(json.getString("api_filename"));
            }
        }
        ShipgraphCollection.get()
                .setShipgraphMap(JsonHelper.toMap(array, Shipgraph::getId, Shipgraph::toShipgraph));
    }

    /**
     * api_data.api_mst_slotitem_equiptype
     *
     * @param array api_mst_slotitem_equiptype
     */
    private void apiMstSlotitemEquiptype(JsonArray array) {
        SlotitemEquiptypeCollection.get()
                .setEquiptypeMap(
                        JsonHelper.toMap(array, SlotitemEquiptype::getId, SlotitemEquiptype::toSlotitemEquiptype));
    }

    /**
     * api_data.api_mst_stype
     *
     * @param array api_mst_stype
     */
    private void apiMstStype(JsonArray array) {
        StypeCollection.get()
                .setStypeMap(JsonHelper.toMap(array, Stype::getId, Stype::toStype));
    }

    /**
     * api_data.api_mst_slotitem
     *
     * @param array api_mst_slotitem
     */
    private void apiMstSlotitem(JsonArray array) {
        SlotitemMstCollection.get()
                .setSlotitemMap(JsonHelper.toMap(array, SlotitemMst::getId, SlotitemMst::toSlotitem));
    }

    /**
     * api_data.api_mst_useitem
     *
     * @param array api_mst_useitem
     */
    private void apiMstUseitem(JsonArray array) {
        UseitemMstCollection.get()
                .setUseitemMap(JsonHelper.toMap(array, UseitemMst::getId, UseitemMst::toUseitem));
    }

    /**
     * api_data.api_mst_mission
     *
     * @param array api_mst_mission
     */
    private void apiMstMission(JsonArray array) {
        MissionCollection.get()
                .setMissionMap(JsonHelper.toMap(array, Mission::getId, Mission::toMission));
    }

    /**
     * api_data.api_mst_maparea
     *
     * @param array api_mst_maparea
     */
    private void apiMstMaparea(JsonArray array) {
        MapareaCollection.get()
                .getMaparea().putAll(JsonHelper.toMap(array, Maparea::getId, Maparea::toMaparea));
    }

    /**
     * api_data.api_mst_mapinfo
     *
     * @param array api_mst_mapinfo
     */
    private void apiMstMapinfo(JsonArray array) {
        MapinfoMstCollection.get()
                .getMapinfo().putAll(JsonHelper.toMap(array, MapinfoMst::getId, MapinfoMst::toMapinfoMst));
    }

    /**
     * api_data.api_mst_shipupgrade
     *
     * @param array api_mst_shipupgrade
     */
    private void apiMstShipupgrade(JsonArray array) {
        if (array != null) {
            ShipupgradeCollection.get()
                    .setShipupgradeMap(JsonHelper.toMap(array, Shipupgrade::getCurrentShipId, Shipupgrade::toShipupgrade));
        }
    }

    /**
     * api_data.api_mst_equip_limit_exslot
     *
     * @param json api_mst_equip_limit_exslot
     */
    private void apiMstEquipLimitExslot(JsonObject json) {
        if (json != null) {
            SlotitemEquipLimitExslotCollection.get()
                    .setShipLimitMap(JsonHelper.toMap(json, Integer::valueOf, JsonHelper::toIntegerSet));
        }
    }

    /**
     * api_data.api_mst_equip_ship
     *
     * @param json api_mst_equip_ship
     */
    private void apiMstEquipShip(JsonObject json) {
        if (json != null) {
            SlotitemEquipShipCollection.get()
                    .setEquipShipMap(JsonHelper.toMap(json, Integer::valueOf, v -> {
                        JsonObject obj = (JsonObject) v;
                        JsonObject equipType = obj.getJsonObject("api_equip_type");
                        if (equipType != null) {
                            return JsonHelper.toMap(equipType, Integer::valueOf, JsonHelper::checkedToIntegerList);
                        }
                        return Collections.emptyMap();
                    }));
        }
    }

    /**
     * api_data.api_mst_equip_exslot_ship
     *
     * @param json api_mst_equip_exslot_ship
     */
    private void apiMstEquipExslotShip(JsonObject json) {
        if (json != null) {
            EquipExslotShipCollection.get()
                    .setEquipExslotShipMap(
                            JsonHelper.toMap(json, Integer::valueOf, EquipExslotShip::toEquipExslotShip));
        }
    }

    /**
     * store
     * 
     * @param root api_data
     */
    private void store(JsonObject root) {
        if (AppConfig.get().isStoreApiStart2()) {
            try {
                String dir = AppConfig.get().getStoreApiStart2Dir();
                if (dir == null || "".equals(dir))
                    return;

                Path dirPath = Paths.get(dir);
                Path parent = dirPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }

                JsonWriterFactory factory = Json
                        .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
                for (Entry<String, JsonValue> entry : root.entrySet()) {
                    String key = entry.getKey();
                    JsonValue val = entry.getValue();
                    JsonObject obj = Json.createObjectBuilder().add(key, val).build();

                    Path outPath = dirPath.resolve(key + ".json");

                    try (OutputStream out = Files.newOutputStream(outPath)) {
                        try (JsonWriter writer = factory.createWriter(out)) {
                            writer.write(obj);
                        }
                    }
                }
            } catch (Exception e) {
                LoggerHolder.get().warn("api_start2の保存に失敗しました", e);
            }
        }
    }
}
