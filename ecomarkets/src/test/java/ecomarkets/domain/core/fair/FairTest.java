package ecomarkets.domain.core.fair;

import ecomarkets.FixtureFactory;
import ecomarkets.domain.core.farmer.Farmer;
import ecomarkets.domain.core.product.Product;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class FairTest {

    Product product;


    Farmer farmer;


    Fair fair = FixtureFactory.createFair();


    @BeforeEach
    @Transactional
    public void before(){
        product = FixtureFactory.createProduct();
        product.persist();

        farmer = FixtureFactory.createFarmer();
        farmer.persist();

        fair = FixtureFactory.createFair();
        fair.persist();
    }

    @Test
    @TestTransaction
    public void addProductTest(){
        final Integer AMOUNT = 10;

        Integer id = given().contentType("application/json")
                .body("""
                        {
                            "farmerId": {"id": %d},
                            "amount": %d
                        }
                        """.formatted(farmer.farmerId().id(), AMOUNT))
                .when()
                .post("/api/fair/%d/product/%d".formatted(fair.id, product.id))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(notNullValue()))
                .extract().path("id");

        ProductAvailableInFair productAvailableInFair = ProductAvailableInFair.findById(id);
        assertThat(productAvailableInFair.getFairId(), Matchers.is(fair.fairId()));
        assertThat(productAvailableInFair.getProductId(), Matchers.is(product.productId()));
        assertThat(productAvailableInFair.getFarmerId(), Matchers.is(farmer.farmerId()));
        assertThat(productAvailableInFair.getAmount(), Matchers.is(AMOUNT));
    }

    @Test
    public void testValidityPeriod(){
        assertThrows(RuntimeException.class, () -> {
            ShoppingPeriod.of(LocalDateTime.now(), LocalDateTime.now().plusWeeks(-1));
        });
    }
}
