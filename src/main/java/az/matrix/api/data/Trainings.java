package az.matrix.api.data;

import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.restassured.RestAssured.given;

public final class Trainings {

    private static final Logger LOG = LogManager.getLogger(Trainings.class);
    private static final List<Long> CREATED = new CopyOnWriteArrayList<>();

    private Trainings() {
    }

    public static long create(String name, String description) {
        long id = given()
                .spec(Specs.authorized())
                .body(TrainingBody.of(name, description))
                .when()
                .post(Endpoints.TRAININGS)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("data.id");
        CREATED.add(id);
        LOG.info("Test təlimi yaradıldı: id={}, ad={}", id, name);
        return id;
    }

    public static void register(long id) {
        CREATED.add(id);
    }

    public static List<Long> created() {
        return CREATED;
    }

    public static void tryDeleteAll() {
        for (Long id : CREATED) {
            int status = given()
                    .spec(Specs.authorized())
                    .when()
                    .delete(Endpoints.TRAINING_BY_ID, id)
                    .then()
                    .extract()
                    .statusCode();
            if (status == 200) {
                LOG.info("Test təlimi silindi: id={}", id);
            } else {
                LOG.warn("Test təlimi silinmədi: id={}, status={}", id, status);
            }
        }
    }
}
