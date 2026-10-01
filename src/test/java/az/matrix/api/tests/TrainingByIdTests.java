package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.Seed;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class TrainingByIdTests extends BaseTest {

    private Seed seed;

    @BeforeClass
    public void prepareData() {
        seed = Seed.get();
    }

    @Test
    public void existingIdReturnsTheTraining() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, seed.alphaId())
                .then()
                .statusCode(200)
                .body("data.id", equalTo((int) seed.alphaId()))
                .body("data.name", equalTo(seed.sortPrefix() + "-a"))
                .body("data.description", equalTo("alpha təsvir"));
    }

    @Test
    public void existingIdReturnsSuccessEnvelope() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, seed.betaId())
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("statusCode", equalTo(200))
                .body("error", nullValue());
    }

    @Test
    public void trainingWithoutDescriptionReturnsNullDescription() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, seed.gammaId())
                .then()
                .statusCode(200)
                .body("data.description", nullValue());
    }

    @Test
    public void trainingByIdMatchesTheListItem() {
        given().spec(Specs.authorized())
                .queryParam("Filter", "id=" + seed.betaId())
                .when().get(Endpoints.TRAININGS)
                .then()
                .statusCode(200)
                .body("data.items[0].name", equalTo(seed.sortPrefix() + "-B"))
                .body("data.items[0].description", equalTo("Beta təsvir"));
    }

    @Test
    public void responseIsJson() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, seed.alphaId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @DataProvider(name = "notExistingIds")
    public Object[][] notExistingIds() {
        return new Object[][]{{"999999"}, {"0"}, {"-1"}};
    }

    @Test(dataProvider = "notExistingIds")
    public void notExistingIdReturnsNotFound(String id) {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then()
                .statusCode(404);
    }

    @DataProvider(name = "malformedIds")
    public Object[][] malformedIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Test(dataProvider = "malformedIds")
    public void malformedIdIsRejected(String id) {
        ErrorAssert.rejected(given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then(), 400, ErrorAssert.INVALID_FORMAT);
    }
}
