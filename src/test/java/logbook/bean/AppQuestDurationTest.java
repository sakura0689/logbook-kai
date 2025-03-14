package logbook.bean;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import logbook.bean.AppQuestDuration.Duration;
import logbook.core.LogBookCoreContainer;

public class AppQuestDurationTest {
    
    @BeforeEach
    public void setup() {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        container.init(Collections.emptyList());
    }
    
    @Test
    public void test() throws IOException, Exception {
        AppQuestDuration duration = new AppQuestDuration();
        Path p = Paths.get("./src/test/resources/logbook/bean/get_member_questlist.json");
        try (Reader reader = Files.newBufferedReader(p)) {
            try (JsonReader jsonReader = Json.createReader(reader)) {
                JsonObject json = jsonReader.readObject()
                        .getJsonObject("api_data");
                QuestList list = QuestList.toQuestList(json);
                Assertions.assertNotNull(list);
                list.getList().stream()
                    .map(AppQuest::toAppQuest)
                    .filter(Objects::nonNull)
                    .forEach(q -> {
                        if (q.isActive()) {
                            duration.set(q);
                        } else {
                            duration.unset(q.getNo());
                        }
                    });
                duration.unset(218);
                list.getList().get(0).setState(0);
                System.out.println(duration.getMap().keySet());
                /// Asia/Tokyo (GMT+09:00) - 5 (5amに任務リセット) = GMT+04:00
                ZonedDateTime today = ZonedDateTime.now(ZoneId.of("GMT+04:00")).truncatedTo(ChronoUnit.DAYS);
                final String dtf = "yyyy-MM-dd HH:mm:ss";
                
                String daily = today.plusDays(1).withZoneSameInstant(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern(dtf));
                Assertions.assertNotNull(duration.getMap().get(daily));
                Map<Integer, List<Duration>> value = duration.getMap().get(daily);
                Assertions.assertNotNull(value.get(218));
                Assertions.assertNotNull(value.get(218).get(0).getTo());

                String weekly = today.plusDays(8-today.getDayOfWeek().getValue()).withZoneSameInstant(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern(dtf));
                Assertions.assertNotNull(duration.getMap().get(weekly));
                value = duration.getMap().get(weekly);
                Assertions.assertNotNull(value.get(220));
                Assertions.assertNull(value.get(220).get(0).getTo());

                String monthly = today.minusDays(today.getDayOfMonth()-1).plusMonths(1).withZoneSameInstant(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern(dtf));
                Assertions.assertNotNull(duration.getMap().get(monthly));
                value = duration.getMap().get(monthly);
                Assertions.assertNotNull(value.get(249));
                Assertions.assertNull(value.get(249).get(0).getTo());

                String quarterly = today.plusMonths(3-today.getMonthValue()%3).withDayOfMonth(1).withZoneSameInstant(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern(dtf));
                Assertions.assertNotNull(duration.getMap().get(quarterly));
                value = duration.getMap().get(quarterly);
                Assertions.assertNotNull(value.get(861));
                Assertions.assertNull(value.get(861).get(0).getTo());

                ZonedDateTime tmp = today.plusMonths(1).withDayOfMonth(1);
                while (tmp.getMonthValue() != 8) {
                    tmp = tmp.plusMonths(1);
                }
                String yearly8 = tmp.withZoneSameInstant(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern(dtf));
                Assertions.assertNotNull(duration.getMap().get(yearly8));
                value = duration.getMap().get(yearly8);
                Assertions.assertNotNull(value.get(438));
                Assertions.assertNull(value.get(438).get(0).getTo());
                
                String once = "9999-12-31 05:00:00";
                Assertions.assertNotNull(duration.getMap().get(once));
                value = duration.getMap().get(once);
                Assertions.assertNotNull(value.get(924));
                Assertions.assertNull(value.get(924).get(0).getTo());
            }
        }
    }
}
