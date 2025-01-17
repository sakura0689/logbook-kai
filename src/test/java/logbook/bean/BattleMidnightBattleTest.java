package logbook.bean;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class BattleMidnightBattleTest {

    /**
     * {@link logbook.bean.BattleMidnightBattle#toBattle(javax.json.JsonObject)} のためのテスト・メソッド。
     * @throws IOException
     */
    @Test
    public void testToBattle() throws IOException {
        Path p = Paths.get("./src/test/resources/logbook/bean/req_battle_midnight_battle.json");
        try (Reader reader = Files.newBufferedReader(p)) {
            try (JsonReader jsonReader = Json.createReader(reader)) {
                JsonObject json = jsonReader.readObject()
                        .getJsonObject("api_data");
                BattleMidnightBattle.toBattle(json);
            }
        }
    }

}
