package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import service.UserService;
import static org.apache.http.HttpStatus.*;

import static org.hamcrest.Matchers.*;

@RunWith(JUnit4.class)
public class UserLoginTests {

    private String accessToken;
    private final UserService userService = new UserService();
    Faker faker = new Faker();

    @Test
    @Description("Успешный логин под существующим пользователем")
    public void loginWithValidCredentials() {
        User user = new User(faker.internet().emailAddress(), "password123", faker.name().fullName());
        userService.createUser(user);

        Response response = userService.loginUser(user);
        response.then()
            .statusCode(SC_OK)
            .body("success", equalTo(true))
            .body("accessToken", not(emptyOrNullString()));

        accessToken = response.then().extract().path("accessToken");
    }

    @Test
    @Description("Логин с несуществующим логином")
    public void loginWithInvalidCredentials() {
        Response response = userService.loginUser(faker.internet().emailAddress(), "password123", null);

        response
            .then()
            .statusCode(SC_UNAUTHORIZED)
            .body("success", equalTo(false))
            .body("message", containsString("email or password are incorrect"));
    }

    @Test
    @Description("Логин с неверным паролем")
    public void loginWithInvalidPassw() {
        User user = new User(faker.internet().emailAddress(), "password123", faker.name().fullName());
        Response createUserResponse = userService.createUser(user);
        Response response = userService.loginUser(user.getEmail(), "wrongpass", user.getName());

        response
            .then()
            .statusCode(SC_UNAUTHORIZED)
            .body("success", equalTo(false))
            .body("message", containsString("email or password are incorrect"));

        accessToken = createUserResponse.then().extract().path("accessToken");
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
