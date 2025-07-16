package service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import util.RestAssuredConfig;

import static io.restassured.RestAssured.given;

public class UserService {

    public Response createUser(String email, String password, String name) {
        User user = new User(email, password, name);
        return createUser(user);
    }

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return  given()
            .header("Content-Type", "application/json")
            .spec(RestAssuredConfig.getBaseSpec())
            .body(user)
            .when()
            .post("/api/auth/register");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return given()
            .header("Authorization", accessToken)
            .spec(RestAssuredConfig.getBaseSpec())
            .delete("/api/auth/user");
    }

    @Step("Логин пользователя")
    public Response loginUser(String email, String password, String name) {
        User user = new User(email, password, name);
        return loginUser(user);
    }

    @Step("Логин пользователя")
    public Response loginUser(User user) {
        return given()
            .header("Content-Type", "application/json")
            .spec(RestAssuredConfig.getBaseSpec())
            .body(user)
            .when()
            .post("/api/auth/login");
    }
}
