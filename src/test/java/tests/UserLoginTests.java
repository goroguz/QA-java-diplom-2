package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.UserService;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class UserLoginTests {

    private final UserService userService = new UserService();
    private final Faker faker = new Faker();

    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        testUser = new User(
            faker.internet().emailAddress(),
            "password123",
            faker.name().fullName()
        );
        Response response = userService.createUser(testUser);
        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @Description("Успешный логин под существующим пользователем")
    public void loginWithValidCredentials() {
        Response response = userService.loginUser(testUser);
        response.then()
            .statusCode(SC_OK)
            .body("success", equalTo(true))
            .body("accessToken", not(emptyOrNullString()));
    }

    @Test
    @Description("Логин с несуществующим логином")
    public void loginWithInvalidCredentials() {
        Response response = userService.loginUser(
            faker.internet().emailAddress(),
            "password123",
            null
        );

        response.then()
            .statusCode(SC_UNAUTHORIZED)
            .body("success", equalTo(false))
            .body("message", containsString("email or password are incorrect"));
    }

    @Test
    @Description("Логин с неверным паролем")
    public void loginWithInvalidPassword() {
        Response response = userService.loginUser(
            testUser.getEmail(),
            "wrongpass",
            testUser.getName()
        );

        response.then()
            .statusCode(SC_UNAUTHORIZED)
            .body("success", equalTo(false))
            .body("message", containsString("email or password are incorrect"));
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