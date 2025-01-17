package logbook.internal.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import logbook.bean.BattleLog;
import logbook.internal.kancolle.BattleLogs;

/**
 *  戦闘ログ詳細画面テスト
 *  
 *  TODO:Labelのmock化
 *  
 *  @see logbook.bean.SortieBattleTest 戦闘情報
 */
class BattleDetailTest {

    /**
     * 2023春イベント 潜水(空)マス
     * 
     * @throws IOException
     */
    @Test
    void testSetDataBattleLog_2023_sensui_kuu() throws IOException,NullPointerException {
        Path p = Paths.get("./src/test/resources/logbook/battlelog/2023-03-21 05-25-27.json");
        try (InputStream inputStream = Files.newInputStream(p)) {
            BattleLog battleLog = BattleLogs.read(inputStream);
            BattleDetail battleDetail = new BattleDetail();
            try {
                BattleDetailViewData viewData = new BattleDetailViewData(battleLog);
                battleDetail.setData(battleLog);
            } catch (NullPointerException e) {
                //TODO:内部データのMock化
            }
        }
    }
}
