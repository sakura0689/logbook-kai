package logbook.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import logbook.bean.BattleLog;
import logbook.bean.SortieBattle;
import logbook.internal.kancolle.BattleLogs.SimpleBattleLog;

public class BattleLogsTest {
    @Test
    public void testCsvLine() {
        String line = "2020-09-18 13:32:40,7-1 ブルネイ泊地沖,4,出撃,S,Ｔ字戦不利,単横陣,単横陣,制空権確保,,,深海潜水艦隊 II群,,,Fletcher Mk.II(Lv148),40/43,夕張改二特(Lv149),47/47,Janus改(Lv99),31/31,Johnston改(Lv99),34/34,朝潮改二丁(Lv99),29/34,,,,,,,,,,,,,,,潜水ソ級(elite),45/45,潜水カ級,19/19,潜水カ級,19/19,潜水カ級,19/19,,,,,,,,,,,,,,,,,,1170,140";
        SimpleBattleLog log = new SimpleBattleLog(line);
        Assert.assertEquals(1170, Integer.parseInt(log.getShipExp()));
        line = "2020-09-18 12:16:27,7-3 ペナン島沖,4,,S,同航戦,複縦陣,単縦陣,制空権確保,,,深海5,500t級軽巡洋艦,,,羽黒改二(Lv99),57/57,阿武隈改二(Lv172),51/51,日進甲(Lv98),49/49,神風改(Lv97),23/23,,,,,,,,,,,,,,,,,軽巡ホ級(flagship),53/53,駆逐イ級,20/20,駆逐イ級,20/20,,,,,,,,,,,,,,,,,,,,660,160";
        log = new SimpleBattleLog(line);
        Assert.assertEquals(660, Integer.parseInt(log.getShipExp()));
        Assert.assertEquals("深海5,500t級軽巡洋艦", log.getEfleet());
        line = "2020-09-18 12:16:27,7-3 ペナン島沖,4,,S,同航戦,複縦陣,単縦陣,制空権確保,,,\"深海5,500t級軽巡洋艦\",,,羽黒改二(Lv99),57/57,阿武隈改二(Lv172),51/51,日進甲(Lv98),49/49,神風改(Lv97),23/23,,,,,,,,,,,,,,,,,軽巡ホ級(flagship),53/53,駆逐イ級,20/20,駆逐イ級,20/20,,,,,,,,,,,,,,,,,,,,660,160";
        log = new SimpleBattleLog(line);
        Assert.assertEquals(660, Integer.parseInt(log.getShipExp()));
        Assert.assertEquals("深海5,500t級軽巡洋艦", log.getEfleet());
        Assert.assertEquals("4", log.getCell());
        Assert.assertEquals("制空権確保", log.getDispseiku());
        line = "2020-09-18 12:16:27,7-3 ペナン島沖,4,,S,同航戦,複縦陣,単縦陣,制空権確保,,,\"深海5,500t級軽巡洋\"\"艦\",,,羽黒改二(Lv99),57/57,阿武隈改二(Lv172),51/51,日進甲(Lv98),49/49,神風改(Lv97),23/23,,,,,,,,,,,,,,,,,軽巡ホ級(flagship),53/53,駆逐イ級,20/20,駆逐イ級,20/20,,,,,,,,,,,,,,,,,,,,660,160";
        log = new SimpleBattleLog(line);
        Assert.assertEquals(660, Integer.parseInt(log.getShipExp()));
        Assert.assertEquals("深海5,500t級軽巡洋\"艦", log.getEfleet());
        line = "2020-09-18 12:16:27,7-3 ペナン島沖,4,,S,同航戦,複縦陣,単縦陣,制空権確保,,,\"深海5,500t級軽巡洋\"\"\"\",\"\"艦\",,,羽黒改二(Lv99),57/57,阿武隈改二(Lv172),51/51,日進甲(Lv98),49/49,神風改(Lv97),23/23,,,,,,,,,,,,,,,,,軽巡ホ級(flagship),53/53,駆逐イ級,20/20,駆逐イ級,20/20,,,,,,,,,,,,,,,,,,,,660,160";
        log = new SimpleBattleLog(line);
        Assert.assertEquals(660, Integer.parseInt(log.getShipExp()));
        Assert.assertEquals("深海5,500t級軽巡洋\"\",\"艦", log.getEfleet());
        line = "2020-09-18 12:16:27,7-3 ペナン島沖,4,,S,同航戦,複縦陣,単縦陣,制空権確保,,,\"深海5,500t級軽巡洋\"\"\"\",\"\"艦\",,,羽黒改二(Lv99),57/57,阿武隈改二(Lv172),51/51,日進甲(Lv98),49/49,神風改(Lv97),23/23,,,,,,,,,,,,,,,,,軽巡ホ級(flagship),53/53,駆逐イ級,20/20,駆逐イ級,20/20,,,,,,,,,,,,,,,,,,,,660,";
        log = new SimpleBattleLog(line);
        Assert.assertEquals(660, Integer.parseInt(log.getShipExp()));
        Assert.assertEquals("深海5,500t級軽巡洋\"\",\"艦", log.getEfleet());
    }
    
    @Test
    public void testWriteButtleLog() throws IOException {
        BattleLog log = new BattleLog();
        SortieBattle bean = null;
        Path p = Paths.get("./src/test/resources/logbook/bean/req_sortie_battle_2024Spring_newformat.json");
        try (Reader reader = Files.newBufferedReader(p)) {
            try (JsonReader jsonReader = Json.createReader(reader)) {
                JsonObject json = jsonReader.readObject()
                        .getJsonObject("api_data");
                bean = SortieBattle.toBattle(json);
            }
        }
        log.setBattle(bean);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        OutputStream out = new ByteArrayOutputStream();
        try {
            mapper.writeValue(out, log);
        } finally {
            out.close();
        }
        
        String outString = out.toString();        
    }
}
