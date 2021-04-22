package cz.encircled.joiner;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * @author Kisel on 26.01.2016.
 */
public class TestDataListener implements TestExecutionListener {

    public void beforeTestClass(TestContext testContext) throws Exception {

    }

    public void prepareTestInstance(TestContext testContext) throws Exception {
        TestData bean = testContext.getApplicationContext().getBean(TestData.class);
        bean.prepareData();
    }

    public void beforeTestMethod(TestContext testContext) throws Exception {

    }

    public void afterTestMethod(TestContext testContext) throws Exception {

    }

    public void afterTestClass(TestContext testContext) throws Exception {

    }
}
