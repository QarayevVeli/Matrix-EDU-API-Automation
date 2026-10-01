package az.matrix.api.tests;

import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.equalTo;

public final class ErrorAssert {

    public static final String INVALID_FORMAT = "Göndərilən məlumat düzgün formatda deyil";

    private ErrorAssert() {
    }

    public static void rejected(ValidatableResponse response, int status, String message) {
        response.statusCode(status)
                .body("isSuccess", equalTo(false))
                .body("statusCode", equalTo(status))
                .body("data", nullValue())
                .body("error.errors", hasItem(message));
    }
}
