package service;

import io.restassured.response.Response;
import model.User;
import util.RestAssuredConfig;

import static io.restassured.RestAssured.given;

public class UserService {

    public Response createUser(String email, String password, String name) {
        User user = new User(email, password, name);
        return createUser(user);
    }

    public Response createUser(User user) {
        return  given()
            .header("Content-Type", "application/json")
            .spec(RestAssuredConfig.getBaseSpec())
            .body(user)
            .when()
            .post("/api/auth/register");
    }

    public Response deleteUser(String accessToken) {
        return given()
            .header("Authorization", accessToken)
            .spec(RestAssuredConfig.getBaseSpec())
            .delete("/api/auth/user");
    }

    public Response loginUser(String email, String password, String name) {
        User user = new User(email, password, name);
        return loginUser(user);
    }

    public Response loginUser(User user) {
        return given()
            .header("Content-Type", "application/json")
            .spec(RestAssuredConfig.getBaseSpec())
            .body(user)
            .when()
            .post("/api/auth/login");
    }
}
