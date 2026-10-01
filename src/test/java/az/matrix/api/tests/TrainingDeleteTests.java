package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.NameFactory;
import az.matrix.api.data.Trainings;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TrainingDeleteTests extends BaseTest {

    private ValidatableResponse delete(Object id) {
        return given().spec(Specs.authorized()).when().delete(Endpoints.TRAINING_BY_ID, id).then();
    }

    @Test
    public void existingTrainingIsDeleted() {
        long id = Trainings.create(NameFactory.unique("QAAPS-DEL-"), null);
        delete(id)
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    public void deletedTrainingIsNoLongerReturned() {
        long id = Trainings.create(NameFactory.unique("QAAPS-DEL-"), null);
        delete(id).statusCode(200);
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then()
                .statusCode(404);
    }

    @Test
    public void deletingNotExistingTrainingReturnsNotFound() {
        delete(999999).statusCode(404);
    }

    @Test
    public void deletingWithMalformedIdIsRejected() {
        ErrorAssert.rejected(delete("abc"), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void deletingTheSameTrainingTwiceReturnsNotFoundSecondTime() {
        long id = Trainings.create(NameFactory.unique("QAAPS-DEL-"), null);
        delete(id).statusCode(200);
        delete(id).statusCode(404);
    }
}
