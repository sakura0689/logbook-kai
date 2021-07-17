package logbook.bean;

import org.junit.jupiter.api.Test;

class AppQuestConditionTest {

    @Test
    void test() {
        AppQuestCondition appQuestCondition = AppQuestCondition.loadFromResource(990201);
        appQuestCondition = AppQuestCondition.loadFromResource(990402);
        appQuestCondition = AppQuestCondition.loadFromResource(990912);
    }
}
