package logbook.bean;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import logbook.internal.kancolle.QuestCollect;

/**
 * AppQuestConditionのテストクラス
 *
 */
class AppQuestConditionTest {

    /** 通常出撃 */
    private static final int QUEST_DAILY = 990201;

    /** 遠征 */
    private static final int QUEST_EXPEDITION = 990402;

    /** イヤリー */
    private static final int QUEST_YEARLY = 990912;

    @Test
    void testLoadFromResourceDailyBattle() {
        AppQuestCondition condition = AppQuestCondition.loadFromResource(QUEST_DAILY);

        Assertions.assertNotNull(condition);
        Assertions.assertEquals(AppQuestCondition.Type.出撃, condition.getType());
        Assertions.assertEquals("デイリー", condition.getResetType());
        Assertions.assertNotNull(condition.getConditions());
        Assertions.assertEquals(1, condition.getConditions().size());

        AppQuestCondition.Condition battleCondition = condition.getConditions().get(0);
        Assertions.assertEquals(1, battleCondition.getCount());
        Set<String> ranks = battleCondition.getRank();
        Assertions.assertNotNull(ranks);
        Assertions.assertTrue(ranks.contains("S"));
        Assertions.assertTrue(ranks.contains("A"));
        Assertions.assertTrue(ranks.contains("B"));
    }

    @Test
    void testLoadFromResourceExpedition() {
        AppQuestCondition condition = AppQuestCondition.loadFromResource(QUEST_EXPEDITION);

        Assertions.assertNotNull(condition);
        Assertions.assertEquals(AppQuestCondition.Type.遠征, condition.getType());
        Assertions.assertEquals("デイリー", condition.getResetType());
        Assertions.assertNotNull(condition.getConditions());
        Assertions.assertEquals(1, condition.getConditions().size());

        AppQuestCondition.Condition missionCondition = condition.getConditions().get(0);
        Assertions.assertEquals(3, missionCondition.getCount());
    }

    @Test
    void testLoadFromResourceYearlyBattle() {
        AppQuestCondition condition = AppQuestCondition.loadFromResource(QUEST_YEARLY);

        Assertions.assertNotNull(condition);
        Assertions.assertEquals(AppQuestCondition.Type.出撃, condition.getType());
        Assertions.assertEquals("イヤリー", condition.getResetType());
        Assertions.assertNotNull(condition.getConditions());
        Assertions.assertEquals(7, condition.getConditions().size());
    }

    @Test
    void testCondition() {
        AppQuestCondition condition = AppQuestCondition.loadFromResource(QUEST_DAILY);

        Assertions.assertNotNull(condition);
        Assertions.assertNotNull(condition.getConditions());
        AppQuestCondition.Condition battleCondition = condition.getConditions().get(0);

        QuestCollect collect = new QuestCollect();
        collect.getTotal().setStart(1);
        collect.getTotal().getAll().setS(1);

        boolean result = condition.test(collect);

        Assertions.assertTrue(result);
        Assertions.assertTrue(battleCondition.getResult());
        Assertions.assertEquals("1", battleCondition.getCurrent());
    }
}