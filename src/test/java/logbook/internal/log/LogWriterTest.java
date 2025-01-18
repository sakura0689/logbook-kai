package logbook.internal.log;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogWriterTest {

    private Path dummyFile;

    @BeforeEach
    void setUp() throws IOException {
        dummyFile =  Files.createTempFile("testFile", ".txt");
    }

    @Test
    void testWrite() throws IOException, InterruptedException {
        LogWriter logWriter = LogWriter.getInstance(DummyFormat::new);
        logWriter.filePath(this.dummyFile);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            int lineNumber = i;
            executor.submit(() -> {
                 logWriter.write("test");
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        List<String> content = Files.readAllLines(this.dummyFile);
        boolean isHeader = true;
        for (String line : content) {
            if (isHeader) {
                isHeader = false;
            } else {
                assertEquals("test", line);
            }
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(dummyFile)) {
//            List<String> content = Files.readAllLines(this.dummyFile);
//            for (String line : content) {
//                System.out.println(line);
//            }
            Files.delete(dummyFile);
        }
    }

    class DummyFormat extends LogFormatBase<String> {

        @Override
        public String name() {
            return "DummyFileName";
        }

        @Override
        public String header() {
            return "DummyHeader";
        }

        @Override
        public String format(String value) {
            return value;
        }

    }
}
