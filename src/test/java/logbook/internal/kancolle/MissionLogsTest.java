package logbook.internal.kancolle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import logbook.internal.kancolle.MissionLogs.SimpleMissionLog;

class MissionLogsTest {

    @Test
    void test_old_format() {
        String old_logbook_mission_failed_str = "2014-08-01 05:00:00,失敗,東京急行,,,,";
        SimpleMissionLog simpleMissionLog_oldformat_fail = new SimpleMissionLog(old_logbook_mission_failed_str);

        Assertions.assertEquals("", simpleMissionLog_oldformat_fail.getArea());
        Assertions.assertEquals("東京急行", simpleMissionLog_oldformat_fail.getName());

        Assertions.assertEquals(0, simpleMissionLog_oldformat_fail.getFuel());
        Assertions.assertEquals(0, simpleMissionLog_oldformat_fail.getAmmo());
        Assertions.assertEquals(0, simpleMissionLog_oldformat_fail.getMetal());
        Assertions.assertEquals(0, simpleMissionLog_oldformat_fail.getBauxite());

        String old_logbook_mission_success_str = "2014-08-01 05:00:00,成功,東京急行,0,380,270,0";
        SimpleMissionLog simpleMissionLog_oldformat_success = new SimpleMissionLog(old_logbook_mission_success_str);

        Assertions.assertEquals("", simpleMissionLog_oldformat_fail.getArea());
        Assertions.assertEquals("東京急行", simpleMissionLog_oldformat_fail.getName());

        Assertions.assertEquals(0, simpleMissionLog_oldformat_success.getFuel());
        Assertions.assertEquals(380, simpleMissionLog_oldformat_success.getAmmo());
        Assertions.assertEquals(270, simpleMissionLog_oldformat_success.getMetal());
        Assertions.assertEquals(0, simpleMissionLog_oldformat_success.getBauxite());

    }

}
