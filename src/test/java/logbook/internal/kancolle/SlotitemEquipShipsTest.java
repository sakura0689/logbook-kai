package logbook.internal.kancolle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import logbook.bean.ShipMst;
import logbook.bean.ShipMstCollection;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;

class SlotitemEquipShipsTest {

    @Test
    void testGetShips_ValidItemId() {
        Path testConfigPath = Paths.get("./src/test/resources/logbook/config"); // ソースパスを直接指定してテスト

        try (MockedStatic<logbook.internal.Config> mockedConfig = mockStatic(logbook.internal.Config.class);
             MockedStatic<logbook.bean.AppConfig> mockedAppConfig = mockStatic(logbook.bean.AppConfig.class)) {

            logbook.internal.Config testConfig = new logbook.internal.Config(testConfigPath);
            logbook.bean.AppConfig mockAppConfig = mock(logbook.bean.AppConfig.class);

            mockedConfig.when(logbook.internal.Config::getDefault).thenReturn(testConfig);
            mockedAppConfig.when(logbook.bean.AppConfig::get).thenReturn(mockAppConfig);

            // getInstance() を呼ぶことで、MockされたConfigを使用して初期化される
            SlotitemEquipShips service = SlotitemEquipShips.getInstance();

            // ユーザーの例: ItemID 268 -> ShipID {100, 101}
            List<Integer> ships = service.getShips(268);
            assertNotNull(ships);
            assertTrue(ships.contains(100), "ShipID 100 should be present for ItemID 268");
            assertTrue(ships.contains(101), "ShipID 101 should be present for ItemID 268");

            List<Integer> shipsFor346 = service.getShips(346);
            assertTrue(shipsFor346.contains(187), "ShipID 187 should be present for ItemID 346");

            List<Integer> shipsFor347 = service.getShips(347);
            assertTrue(shipsFor347.contains(187), "ShipID 187 should be present for ItemID 347");
        }
    }

    @Test
    void testGetShips_InvalidItemId() {
        Path testConfigPath = Paths.get("./src/test/resources/logbook/config");

        try (MockedStatic<logbook.internal.Config> mockedConfig = mockStatic(logbook.internal.Config.class);
             MockedStatic<logbook.bean.AppConfig> mockedAppConfig = mockStatic(logbook.bean.AppConfig.class)) {

            logbook.internal.Config testConfig = new logbook.internal.Config(testConfigPath);
            logbook.bean.AppConfig mockAppConfig = mock(logbook.bean.AppConfig.class);

            mockedConfig.when(logbook.internal.Config::getDefault).thenReturn(testConfig);
            mockedAppConfig.when(logbook.bean.AppConfig::get).thenReturn(mockAppConfig);

            SlotitemEquipShips service = SlotitemEquipShips.getInstance();

            // 存在しないItemIDを指定
            List<Integer> ships = service.getShips(99999);
            assertNotNull(ships);
            assertTrue(ships.isEmpty(), "Should return empty list for unknown item ID");
        }
    }
    
    /**
     * データ確認用
     */
    @Test
    void testGetShips_getAllSlotitemEquipShips() {
        Path testConfigPath = Paths.get("./src/test/resources/logbook/config");

        try (MockedStatic<logbook.internal.Config> mockedConfig = mockStatic(logbook.internal.Config.class);
             MockedStatic<logbook.bean.AppConfig> mockedAppConfig = mockStatic(logbook.bean.AppConfig.class)) {

            logbook.internal.Config testConfig = new logbook.internal.Config(testConfigPath);
            logbook.bean.AppConfig mockAppConfig = mock(logbook.bean.AppConfig.class);

            mockedConfig.when(logbook.internal.Config::getDefault).thenReturn(testConfig);
            mockedAppConfig.when(logbook.bean.AppConfig::get).thenReturn(mockAppConfig);

            SlotitemEquipShips service = SlotitemEquipShips.getInstance();

            ShipMstCollection shipMstCollection = ShipMstCollection.get();
            SlotitemMstCollection slotItemMstCollection = SlotitemMstCollection.get();
            Map<Integer, ShipMst> shipMstMap = shipMstCollection.getShipMap();
            Map<Integer, SlotitemMst> slotItemMstMap = slotItemMstCollection.getSlotitemMap();
            
            Map<Integer, List<Integer>> allSlotitemEquipShipsMap = service.getAllSlotitemEquipShips();
            
            for (Entry<Integer, List<Integer>> entry : allSlotitemEquipShipsMap.entrySet()) {
                Integer itemId = entry.getKey();
                List<Integer> ships = entry.getValue();
                
                //System.out.println(slotItemMstMap.get(itemId).toString());
                for (Integer shipid : ships) {
                    String data = shipMstMap.get(shipid) != null ? shipMstMap.get(shipid).toString() : "null";
                    //System.out.println(" " +  data);
                }
            }
            
        }
    }
}
