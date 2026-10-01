package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.Seed;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;

public class TrainingDropdownTests extends BaseTest {

    private Seed seed;

    @BeforeClass
    public void prepareData() {
        seed = Seed.get();
    }

    @Test
    public void dropdownReturnsSuccessEnvelope() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("statusCode", equalTo(200))
                .body("error", nullValue());
    }

    @Test
    public void dropdownItemContainsIdAndName() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then()
                .statusCode(200)
                .body("data[0]", hasKey("id"))
                .body("data[0]", hasKey("name"));
    }

    @Test
    public void dropdownContainsTheSeededTraining() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then()
                .statusCode(200)
                .body("data.id", hasItem((int) seed.alphaId()));
    }

    @Test
    public void dropdownCountEqualsTheTotalCountOfTheList() {
        int total = given().spec(Specs.authorized())
                .when().get(Endpoints.TRAININGS)
                .then().statusCode(200)
                .extract().path("data.totalCount");
        int dropdown = given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then().statusCode(200)
                .extract().path("data.size()");
        Assert.assertEquals(dropdown, total, "Dropdown və siyahıdakı say fərqlidir");
    }

    @Test
    public void dropdownIsOrderedByNameIgnoringLetterCase() {
        List<String> names = given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then().statusCode(200)
                .extract().path("data.name");
        List<String> mine = names.stream().filter(name -> name.startsWith(seed.sortPrefix())).toList();
        Assert.assertEquals(mine, List.of(seed.sortPrefix() + "-a", seed.sortPrefix() + "-B", seed.sortPrefix() + "-c"));
    }

    @Test
    public void dropdownResponseIsJson() {
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_DROPDOWN)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
