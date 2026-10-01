package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.NameFactory;
import az.matrix.api.data.TrainingBody;
import az.matrix.api.data.Trainings;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class TrainingCreateTests extends BaseTest {

    private static final String NAME_TOO_LONG = "Təlim adı maksimum 150 simvol ola bilər";
    private static final String DESCRIPTION_TOO_LONG = "Təsvir maksimum 500 simvol ola bilər";
    private static final String NAME_EMPTY = "Təlim adı boş ola bilməz";

    private Response post(Object body) {
        return given().spec(Specs.authorized()).body(body).when().post(Endpoints.TRAININGS);
    }

    private long registered(Response response) {
        long id = response.jsonPath().getLong("data.id");
        Trainings.register(id);
        return id;
    }

    @Test
    public void trainingWithNameOnlyIsCreated() {
        String name = NameFactory.unique("QAAPS-");
        Response response = post(TrainingBody.nameOnly(name));
        registered(response);
        response.then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.id", greaterThan(0))
                .body("data.name", equalTo(name))
                .body("data.description", nullValue());
    }

    @Test
    public void trainingWithNameAndDescriptionIsCreated() {
        String name = NameFactory.unique("QAAPS-");
        Response response = post(TrainingBody.of(name, "Yeni təlimin təsviri"));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.name", equalTo(name))
                .body("data.description", equalTo("Yeni təlimin təsviri"));
    }

    @Test
    public void createdTrainingCanBeReadById() {
        String name = NameFactory.unique("QAAPS-");
        long id = registered(post(TrainingBody.of(name, "Oxunma yoxlaması")));
        given().spec(Specs.authorized())
                .when().get(Endpoints.TRAINING_BY_ID, id)
                .then()
                .statusCode(200)
                .body("data.name", equalTo(name))
                .body("data.description", equalTo("Oxunma yoxlaması"));
    }

    @Test
    public void createdTrainingAppearsInTheList() {
        String name = NameFactory.unique("QAAPS-");
        registered(post(TrainingBody.nameOnly(name)));
        given().spec(Specs.authorized())
                .queryParam("Filter", "name=" + name)
                .when().get(Endpoints.TRAININGS)
                .then()
                .statusCode(200)
                .body("data.totalCount", equalTo(1));
    }

    @Test
    public void trainingWithNameOf150CharactersIsCreated() {
        String name = NameFactory.unique("QAAPS-");
        name = name + NameFactory.repeat('Z', 150 - name.length());
        Response response = post(TrainingBody.nameOnly(name));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.name", equalTo(name));
    }

    @Test
    public void trainingWithDescriptionOf500CharactersIsCreated() {
        String description = NameFactory.repeat('D', 500);
        Response response = post(TrainingBody.of(NameFactory.unique("QAAPS-"), description));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.description", equalTo(description));
    }

    @Test
    public void nullDescriptionIsStoredAsNull() {
        Response response = post(TrainingBody.of(NameFactory.unique("QAAPS-"), null));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.description", nullValue());
    }

    @Test
    public void emptyDescriptionIsAccepted() {
        Response response = post(TrainingBody.of(NameFactory.unique("QAAPS-"), ""));
        registered(response);
        response.then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    public void unknownFieldsAreIgnored() {
        String body = "{\"name\":\"" + NameFactory.unique("QAAPS-") + "\",\"hack\":\"value\"}";
        Response response = post(body);
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.hack", nullValue());
    }

    @Test
    public void idInTheBodyIsIgnored() {
        String body = "{\"id\":987654,\"name\":\"" + NameFactory.unique("QAAPS-") + "\"}";
        Response response = post(body);
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.id", not(equalTo(987654)));
    }

    @Test
    public void spacesAroundTheNameAreTrimmed() {
        String core = NameFactory.unique("QAAPS-");
        Response response = post(TrainingBody.nameOnly("  " + core + "  "));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.name", equalTo(core));
    }

    @DataProvider(name = "specialNames")
    public Object[][] specialNames() {
        return new Object[][]{
                {"Ümid & Şirkət"},
                {"<b>bold</b>"},
                {"'; DROP TABLE Trainings;--"},
                {"Тренинг"},
                {"İşçi təlimi (Java)"}
        };
    }

    @Test(dataProvider = "specialNames")
    public void specialCharactersAreStoredAsPlainText(String text) {
        String name = NameFactory.unique("QAAPS-") + " " + text;
        Response response = post(TrainingBody.nameOnly(name));
        registered(response);
        response.then()
                .statusCode(200)
                .body("data.name", equalTo(name));
    }

    @Test
    public void duplicateNameIsRejected() {
        String name = NameFactory.unique("QAAPS-");
        registered(post(TrainingBody.nameOnly(name)));
        Response second = post(TrainingBody.nameOnly(name));
        if (second.statusCode() == 200) {
            registered(second);
        }
        Assert.assertTrue(second.statusCode() >= 400 && second.statusCode() < 500,
                "Eyni adlı ikinci təlim qəbul edildi, status: " + second.statusCode());
    }

    @Test
    public void nullNameIsRejected() {
        ErrorAssert.rejected(post("{\"name\":null}").then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void missingNameIsRejected() {
        ErrorAssert.rejected(post("{}").then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void emptyNameIsRejected() {
        ErrorAssert.rejected(post(TrainingBody.nameOnly("")).then(), 400, NAME_EMPTY);
    }

    @DataProvider(name = "blankNames")
    public Object[][] blankNames() {
        return new Object[][]{{" "}, {"     "}, {"\t"}};
    }

    @Test(dataProvider = "blankNames")
    public void blankNameIsRejected(String name) {
        ErrorAssert.rejected(post(TrainingBody.nameOnly(name)).then(), 400, NAME_EMPTY);
    }

    @Test
    public void nameOf151CharactersIsRejected() {
        ErrorAssert.rejected(post(TrainingBody.nameOnly(NameFactory.repeat('Q', 151))).then(), 400, NAME_TOO_LONG);
    }

    @Test
    public void descriptionOf501CharactersIsRejected() {
        ErrorAssert.rejected(post(TrainingBody.of(NameFactory.unique("QAAPS-"), NameFactory.repeat('D', 501))).then(), 400, DESCRIPTION_TOO_LONG);
    }

    @Test
    public void descriptionWithoutNameIsRejected() {
        ErrorAssert.rejected(post("{\"description\":\"Yalnız təsvir\"}").then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void invalidJsonIsRejected() {
        ErrorAssert.rejected(post("{\"name\":").then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void emptyBodyIsRejected() {
        ErrorAssert.rejected(post("").then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @DataProvider(name = "wrongTypeBodies")
    public Object[][] wrongTypeBodies() {
        return new Object[][]{{"{\"name\":123}"}, {"{\"name\":[\"a\"]}"}};
    }

    @Test(dataProvider = "wrongTypeBodies")
    public void nameOfWrongTypeIsRejected(String body) {
        ErrorAssert.rejected(post(body).then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void plainTextContentTypeIsRejected() {
        given().spec(Specs.authorized())
                .contentType(ContentType.TEXT)
                .body("{\"name\":\"QAAPS-text\"}")
                .when().post(Endpoints.TRAININGS)
                .then()
                .statusCode(415);
    }
}
