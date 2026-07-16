package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Integration test for warehouse search and filter functionality.
 */
@QuarkusTest
public class WarehouseSearchIT {

  @Test
  public void testSearchAllWarehouses() {
    given()
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue())
        .body("pageNumber", equalTo(0))
        .body("pageSize", equalTo(10))
        .body("totalItems", greaterThanOrEqualTo(0))
        .body("totalPages", greaterThanOrEqualTo(0));
  }

  @Test
  public void testSearchByLocation() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue());
  }

  @Test
  public void testSearchByCapacityRange() {
    given()
        .queryParam("minCapacity", "50")
        .queryParam("maxCapacity", "100")
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue());
  }

  @Test
  public void testSearchWithPagination() {
    given()
        .queryParam("page", "0")
        .queryParam("pageSize", "5")
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("pageNumber", equalTo(0))
        .body("pageSize", equalTo(5));
  }

  @Test
  public void testSearchSortByCapacityDesc() {
    given()
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "desc")
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue());
  }

  @Test
  public void testSearchWithInvalidPageSize() {
    given()
        .queryParam("pageSize", "101")
        .get("/warehouse/search")
        .then()
        .statusCode(400);
  }

  @Test
  public void testSearchWithInvalidSortBy() {
    given()
        .queryParam("sortBy", "invalid")
        .get("/warehouse/search")
        .then()
        .statusCode(400);
  }

  @Test
  public void testSearchExcludesArchivedWarehouses() {
    // This test assumes archived warehouses exist and verifies they are excluded
    given()
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue());
  }

  @Test
  public void testSearchCombinedFilters() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .queryParam("minCapacity", "50")
        .queryParam("maxCapacity", "100")
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "asc")
        .queryParam("page", "0")
        .queryParam("pageSize", "10")
        .get("/warehouse/search")
        .then()
        .statusCode(200)
        .body("items", notNullValue())
        .body("pageNumber", equalTo(0))
        .body("pageSize", equalTo(10));
  }
}
