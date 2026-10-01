package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.NameFactory;
import az.matrix.api.data.TrainingBody;
import az.matrix.api.data.Trainings;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class TrainingUpdateTests extends BaseTest {

    private ValidatableResponse put(Object body) {
        return given().spec(Specs.authorized()).body(body).when().put(Endpoints.TRAININGS).then();
    }

    @Test
    public void existingTrainingIsUpdated() {
        long id = Trainings.create(NameFactory.unique("QAAPS-UPD-"), "köhnə təsvir");
        put(TrainingBody.update(id, NameFactory.unique("QAAPS-UPD-"), "yeni təsvir"))
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    public void updateIsPersisted() {
        long id = Trainings.create(NameFactory.unique("QAAPS-UPD-"), "köhnə təsvir");
        String newName = NameFactory.unique("QAAPS-UPD-");
        put(TrainingBody.update(id, newName, "yeni təsvir")).statusCode(200);
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then()
                .statusCode(200)
                .body("data.name", equalTo(newName))
                .body("data.description", equalTo("yeni təsvir"));
    }

    @Test
    public void descriptionCanBeClearedByUpdate() {
        long id = Trainings.create(NameFactory.unique("QAAPS-UPD-"), "silinəcək təsvir");
        put(TrainingBody.update(id, NameFactory.unique("QAAPS-UPD-"), null)).statusCode(200);
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then()
                .body("data.description", nullValue());
    }

    @Test
    public void updateOfNotExistingTrainingReturnsNotFound() {
        put(TrainingBody.update(999999, NameFactory.unique("QAAPS-UPD-"), null))
                .statusCode(404);
    }

    @Test
    public void updateWithEmptyNameIsRejected() {
        long id = Trainings.create(NameFactory.unique("QAAPS-UPD-"), null);
        ErrorAssert.rejected(put(TrainingBody.update(id, "", null)), 400, "Təlim adı boş ola bilməz");
    }

    @Test
    public void updateWithTooLongNameIsRejected() {
        long id = Trainings.create(NameFactory.unique("QAAPS-UPD-"), null);
        ErrorAssert.rejected(put(TrainingBody.update(id, NameFactory.repeat('Q', 151), null)), 400, "Təlim adı maksimum 150 simvol ola bilər");
    }

    @Test
    public void updateWithIdZeroIsRejected() {
        put(TrainingBody.update(0, NameFactory.unique("QAAPS-UPD-"), null))
                .statusCode(anyOf(equalTo(400), equalTo(404)));
    }
}
