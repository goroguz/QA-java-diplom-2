package service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.Order;
import util.RestAssuredConfig;

import static io.restassured.RestAssured.given;

public class OrderService {

    @Step("Создание заказа")
    public Response createOrder(String accessToken, String[] ingredientHashes) {
        RequestSpecification request = given()
            .header("Content-Type", "application/json")
            .spec(RestAssuredConfig.getBaseSpec())
            .body(new Order(ingredientHashes));

        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("Authorization", accessToken);
        }

        return request.when().post("/api/orders");
    }

    @Step("Получение списка валидных хэшей ингредиентов")
    public String[] getValidIngredientHashes() {
        Response response = given()
            .spec(RestAssuredConfig.getBaseSpec())
            .get("/api/ingredients");

        java.util.List<String> list = response.then()
            .statusCode(200)
            .extract()
            .path("data._id");

        return list.toArray(new String[0]);
    }
}
