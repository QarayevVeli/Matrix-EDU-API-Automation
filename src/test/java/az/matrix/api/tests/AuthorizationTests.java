package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Session;
import az.matrix.api.http.Specs;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AuthorizationTests extends BaseTest {

    @DataProvider(name = "protectedRequests")
    public Object[][] protectedRequests() {
        return new Object[][]{
                {"GET", Endpoints.TRAININGS, null},
                {"GET", "/api/Trainings/1", null},
                {"GET", Endpoints.TRAINING_DROPDOWN, null},
                {"POST", Endpoints.TRAININGS, "{\"name\":\"QAAPS-auth\"}"},
                {"PUT", Endpoints.TRAININGS, "{\"id\":1,\"name\":\"QAAPS-auth\"}"},
                {"DELETE", "/api/Trainings/1", null}
        };
    }

    private void send(RequestSpecification spec, String method, String path, String body, int expectedStatus) {
        RequestSpecification request = given().spec(spec);
        if (body != null) {
            request.body(body);
        }
        request.when()
                .request(method, path)
                .then()
                .statusCode(expectedStatus);
    }

    @Test(dataProvider = "protectedRequests")
    public void requestWithoutTokenIsRejected(String method, String path, String body) {
        send(Specs.anonymous(), method, path, body, 401);
    }

    @Test(dataProvider = "protectedRequests")
    public void requestWithInvalidTokenIsRejected(String method, String path, String body) {
        send(Specs.withToken("invalid.token.value"), method, path, body, 401);
    }

    @Test
    public void basicAuthorizationSchemeIsRejected() {
        given()
                .spec(Specs.anonymous())
                .header("Authorization", "Basic " + Session.token())
                .when()
                .get(Endpoints.TRAININGS)
                .then()
                .statusCode(401);
    }

    @Test
    public void tokenWithoutBearerPrefixIsRejected() {
        given()
                .spec(Specs.anonymous())
                .header("Authorization", Session.token())
                .when()
                .get(Endpoints.TRAININGS)
                .then()
                .statusCode(401);
    }

    @Test
    public void emptyBearerValueIsRejected() {
        given()
                .spec(Specs.anonymous())
                .header("Authorization", "Bearer ")
                .when()
                .get(Endpoints.TRAININGS)
                .then()
                .statusCode(401);
    }

    @Test
    public void tokenInQueryStringIsRejected() {
        given()
                .spec(Specs.anonymous())
                .queryParam("access_token", Session.token())
                .when()
                .get(Endpoints.TRAININGS)
                .then()
                .statusCode(401);
    }

    @Test
    public void tamperedTokenIsRejected() {
        String token = Session.token();
        String tampered = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");
        given()
                .spec(Specs.withToken(tampered))
                .when()
                .get(Endpoints.TRAININGS)
                .then()
                .statusCode(401);
    }
}
