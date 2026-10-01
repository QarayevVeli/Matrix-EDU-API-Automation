package az.matrix.api.http;

import az.matrix.api.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class Session {

    private static final Logger LOG = LogManager.getLogger(Session.class);
    private static String token;

    private Session() {
    }

    public static synchronized String token() {
        if (token == null) {
            LOG.info("Test istifadəçisi ilə login olunur: {}", ConfigReader.get("username"));
            token = given()
                    .spec(Specs.anonymous())
                    .body(Map.of("userName", ConfigReader.get("username"), "password", ConfigReader.get("password")))
                    .when()
                    .post(Endpoints.LOGIN)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("data.accessToken");
        }
        return token;
    }
}
