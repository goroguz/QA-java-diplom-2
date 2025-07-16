package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import service.UserService;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class UserCreationTests {

    private String accessToken;
    private final UserService userService = new UserService();
    Faker faker = new Faker();

    @Test
    @Description("Создание уникального пользователя")
    public void createUniqueUser() {
        String email = faker.internet().emailAddress();
        Response response = userService.createUser(email, "password123", faker.name().fullName());

        response.then()
            .statusCode(SC_OK)
            .body("success", equalTo(true))
            .body("user.email", equalTo(email.toLowerCase()));

        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @Description("Создание пользователя, который уже зарегистрирован")
    public void createDuplicateUser() {
        String email = faker.internet().emailAddress();

        Response response1 = userService.createUser(email, "password123", faker.name().fullName());
        response1
            .then()
            .statusCode(SC_OK)
            .body("success", equalTo(true));

        Response response2 = userService.createUser(email, "password123", faker.name().fullName());
        response2
            .then()
            .statusCode(SC_FORBIDDEN)
            .body("success", equalTo(false))
            .body("message", containsString("User already exists"));
    }

    @Test
    @Description("Создание пользователя без пароля")
    public void createUserMissingPassword() {
        Response response = userService.createUser(faker.internet().emailAddress(), null, faker.name().fullName());

        response
            .then()
            .statusCode(SC_FORBIDDEN)
            .body("success", equalTo(false))
            .body("message", containsString("Email, password and name are required fields"));
    }

    @Test
    @Description("Создание пользователя без email")
    public void createUserMissingEmail() {
        Response response = userService.createUser(null, "password123", faker.name().fullName());

        response
            .then()
            .statusCode(SC_FORBIDDEN)
            .body("success", equalTo(false))
            .body("message", containsString("Email, password and name are required fields"));
    }

    @Test
    @Description("Создание пользователя без имени")
    public void createUserMissingName() {
        Response response = userService.createUser(faker.internet().emailAddress(), "password123", null);

        response
            .then()
            .statusCode(SC_FORBIDDEN)
            .body("success", equalTo(false))
            .body("message", containsString("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userService.deleteUser(accessToken)
                .then()
                .statusCode(SC_ACCEPTED);
        }
    }
}
