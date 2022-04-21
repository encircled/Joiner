package cz.encircled.joiner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWithLogging {

    private static final Logger log = LoggerFactory.getLogger(TestWithLogging.class);

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        log.info("Starting unit test: " + testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) throws Exception {
        log.info("Finished unit test: " + testInfo.getDisplayName());
    }

}
