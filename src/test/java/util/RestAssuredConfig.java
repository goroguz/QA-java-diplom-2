package util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class RestAssuredConfig {
    private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
        .setBaseUri("https://stellarburgers.nomoreparties.site")
        .build();

    public static RequestSpecification getBaseSpec() {
        return BASE_SPEC;
    }
}