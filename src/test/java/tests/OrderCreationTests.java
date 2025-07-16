package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import service.OrderService;
import service.UserService;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

@RunWith(Parameterized.class)
public class OrderCreationTests {

    private final String[] ingredients;
    private final boolean shouldSucceed;
    private final boolean useAuth;

    public OrderCreationTests(String[] ingredients, boolean shouldSucceed, boolean useAuth, String displayName) {
        this.ingredients = ingredients;
        this.shouldSucceed = shouldSucceed;
        this.useAuth = useAuth;
    }

    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final Faker faker = new Faker();
    private String accessToken;

    @Parameterized.Parameters(name = "Test {3}")
    public static Collection<Object[]> testData() {
        OrderService tempService = new OrderService();
        String[] validIngredients = tempService.getValidIngredientHashes();

        return Arrays.asList(new Object[][]{
            {validIngredients, true, true, "With valid ingredients"},
            {validIngredients, true, false, "With valid ingredients, without access token"},
            {new String[]{}, false, true, "Without ingredients"},
            {new String[]{"invalid123hash"}, false, true, "With invalid ingredient hash"},
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        String email = faker.internet().emailAddress();
        String password = "password123";
        String name = faker.name().fullName();

        Response registerResponse = userService.createUser(email, password, name);
        accessToken = registerResponse.then().extract().path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userService.deleteUser(accessToken).then().statusCode(SC_ACCEPTED);
        }
    }

    @Test
    @Description("Проверка создания заказа с разными наборами ингредиентов")
    public void testOrderCreationVariants() {
        String tokenToUse = useAuth ? accessToken : null;
        Response response = orderService.createOrder(tokenToUse, ingredients);

        if (shouldSucceed) {
            response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
        } else {
            response.then()
                .statusCode(anyOf(is(SC_BAD_REQUEST), is(SC_FORBIDDEN), is(SC_INTERNAL_SERVER_ERROR)));

            if (response.getContentType() != null && response.getContentType().contains("application/json")) {
                Boolean success = response.jsonPath().get("success");
                if (success != null) {
                    org.junit.Assert.assertFalse("Expected success == false", success);
                }
            }
        }
    }
}

