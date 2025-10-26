package logbook.internal.kancolle;

import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import logbook.bean.AppConfig;
import logbook.bean.AppQuest;
import logbook.bean.AppQuestCollection;
import logbook.bean.AppQuestCondition;
import logbook.internal.Config;

class QuestTest {

    @Test
    void test() {
        Path testConfigPath = Paths.get("./target/test-classes/logbook/config");
        Config testConfig = new Config(testConfigPath);
        
        Path testButtlePath = Paths.get("./target/test-classes/logbook/buttlelog");
        AppConfig mockAppConfig = mock(AppConfig.class);
        when(mockAppConfig.getReportPath()).thenReturn(testButtlePath.toString());
        
        try (MockedStatic<Config> mockedConfig = mockStatic(Config.class);
                MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class)) {
            mockedConfig.when(Config::getDefault).thenReturn(testConfig);
            mockedAppConfig.when(AppConfig::get).thenReturn(mockAppConfig);
            
            AppQuestCollection appQuestCollection = AppQuestCollection.get();        
            ConcurrentSkipListMap<Integer,AppQuest> questList = appQuestCollection.getQuest(); 
            AppQuest quest = questList.get(1040);
            AppQuestCondition condition = AppQuestCondition.loadFromResource(quest.getNo());
        
            QuestCollect collect = QuestCollect.collect(quest, condition);
            condition.test(collect);
            Optional.of(condition);
        }
    }

}
