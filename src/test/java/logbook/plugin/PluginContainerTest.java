package logbook.plugin;

import java.util.Collections;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class PluginContainerTest {

    @Test
    void test() {
        PluginContainer container = PluginContainer.getInstance();
        container.init(Collections.emptyList());
        
        Assert.assertEquals(0, container.getPlugins().size());
    }
}
