package logbook.core;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogBookCoreContainerTest {

    @Test
    void test() {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        container.init(Collections.emptyList());

        Assertions.assertEquals(0, container.getPlugins().size());
    }
}
