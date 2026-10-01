package az.matrix.api.base;

import az.matrix.api.data.Trainings;
import az.matrix.api.http.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {

    private static final Logger LOG = LogManager.getLogger(BaseTest.class);

    @BeforeSuite(alwaysRun = true)
    public void openSession() {
        Session.token();
        LOG.info("Sessiya açıldı");
    }

    @AfterSuite(alwaysRun = true)
    public void cleanUp() {
        LOG.info("Test datası təmizlənir: {} təlim", Trainings.created().size());
        try {
            Trainings.tryDeleteAll();
        } catch (Exception e) {
            LOG.warn("Təmizləmə xətası: {}", e.getMessage());
        }
    }
}
