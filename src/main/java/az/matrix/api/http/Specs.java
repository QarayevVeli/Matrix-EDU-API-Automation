package az.matrix.api.http;

import az.matrix.api.config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class Specs {

    private Specs() {
    }

    private static RequestSpecBuilder builder() {
        int timeout = ConfigReader.getInt("timeout") * 1000;
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", timeout)
                .setParam("http.socket.timeout", timeout));
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.url"))
                .setConfig(config)
                .addFilter(new ApiLogFilter());
    }

    public static RequestSpecification anonymous() {
        return builder().setContentType(ContentType.JSON).build();
    }

    public static RequestSpecification withToken(String token) {
        return builder().setContentType(ContentType.JSON).addHeader("Authorization", "Bearer " + token).build();
    }

    public static RequestSpecification authorized() {
        return withToken(Session.token());
    }
}
