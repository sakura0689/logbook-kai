package logbook.internal.kancolle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.jupiter.api.Test;

import logbook.bean.AppQuest;
import logbook.bean.AppQuestCollection;
import logbook.bean.AppQuestCondition;
import logbook.internal.Config;

class QuestTest {

    @Test
    void test() {
        Path testConfigPath = Paths.get("./target/test-classes/logbook/config");
        Config testConfig = new Config(testConfigPath);
        
        AppQuestCollection appQuestCollection = testConfig.get(AppQuestCollection.class, AppQuestCollection::new);        
        ConcurrentSkipListMap<Integer,AppQuest> questList = appQuestCollection.getQuest(); 
        AppQuest quest = questList.get(1040);
        AppQuestCondition condition = AppQuestCondition.loadFromResource(quest.getNo());
        
        QuestCollect collect = QuestCollect.collect(quest, condition);
        condition.test(collect);
        Optional.of(condition);
    }

}
