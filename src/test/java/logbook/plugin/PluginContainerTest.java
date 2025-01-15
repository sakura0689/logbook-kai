package logbook.plugin;

import java.util.Collections;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import logbook.core.LogBookCoreContainer;

class PluginContainerTest {

    @Test
    void test() {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        container.init(Collections.emptyList());
        
        Assert.assertEquals(0, container.getPlugins().size());
    }
}
