package logbook.internal;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import logbook.internal.kancolle.MissionLogs.SimpleMissionLog;

class MissionLogsTest {

    @Test
    void test_old_format() {
        String old_logbook_mission_failed_str = "2014-08-01 05:00:00,失敗,東京急行,,,,";
        SimpleMissionLog simpleMissionLog_oldformat_fail = new SimpleMissionLog(old_logbook_mission_failed_str);

        Assert.assertEquals("", simpleMissionLog_oldformat_fail.getArea());
        Assert.assertEquals("東京急行", simpleMissionLog_oldformat_fail.getName());

        Assert.assertEquals(0, simpleMissionLog_oldformat_fail.getFuel());
        Assert.assertEquals(0, simpleMissionLog_oldformat_fail.getAmmo());
        Assert.assertEquals(0, simpleMissionLog_oldformat_fail.getMetal());
        Assert.assertEquals(0, simpleMissionLog_oldformat_fail.getBauxite());

        String old_logbook_mission_success_str = "2014-08-01 05:00:00,成功,東京急行,0,380,270,0";
        SimpleMissionLog simpleMissionLog_oldformat_success = new SimpleMissionLog(old_logbook_mission_success_str);

        Assert.assertEquals("", simpleMissionLog_oldformat_fail.getArea());
        Assert.assertEquals("東京急行", simpleMissionLog_oldformat_fail.getName());

        Assert.assertEquals(0, simpleMissionLog_oldformat_success.getFuel());
        Assert.assertEquals(380, simpleMissionLog_oldformat_success.getAmmo());
        Assert.assertEquals(270, simpleMissionLog_oldformat_success.getMetal());
        Assert.assertEquals(0, simpleMissionLog_oldformat_success.getBauxite());

    }

}
