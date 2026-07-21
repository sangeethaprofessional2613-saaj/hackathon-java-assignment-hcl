package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductResourceUnitTest {

  @InjectMock
  ProductRepository productRepository;

  @Inject
  ProductResource productResource;

  @Test
  public void testGetAllProducts() {
    Product p1 = new Product("P1");
    Product p2 = new Product("P2");
    List<Product> list = new ArrayList<>();
    list.add(p1); list.add(p2);

    when(productRepository.listAll(any())).thenReturn(list);

    List<Product> res = productResource.get();
    assertEquals(2, res.size());
  }

  @Test
  public void testGetSingleFound() {
    Product p = new Product("Found");
    p.id = 1L;
    when(productRepository.findById(1L)).thenReturn(p);

    Product res = productResource.getSingle(1L);
    assertEquals("Found", res.name);
  }

  @Test
  public void testGetSingleNotFound() {
    when(productRepository.findById(999L)).thenReturn(null);
    assertThrows(WebApplicationException.class, () -> productResource.getSingle(999L));
  }

  @Test
  public void testCreateProductSuccess() {
    Product p = new Product("New");
    p.price = new BigDecimal("10.00");
    p.stock = 5;

    doNothing().when(productRepository).persist(p);

    Response r = productResource.create(p);
    assertEquals(201, r.getStatus());
    verify(productRepository, times(1)).persist(p);
  }

  @Test
  public void testCreateProductWithIdInvalid() {
    Product p = new Product("Invalid");
    p.id = 10L;

    assertThrows(WebApplicationException.class, () -> productResource.create(p));
  }

  @Test
  public void testUpdateProductSuccess() {
    Product existing = new Product("Old");
    existing.id = 1L;
    existing.name = "Old";
    when(productRepository.findById(1L)).thenReturn(existing);

    Product update = new Product();
    update.name = "NewName";
    update.description = "desc";
    update.price = new BigDecimal("20.00");
    update.stock = 10;

    Product res = productResource.update(1L, update);
    assertEquals("NewName", res.name);
    verify(productRepository, times(1)).persist(existing);
  }

  @Test
  public void testUpdateProductNotFound() {
    when(productRepository.findById(999L)).thenReturn(null);
    Product update = new Product();
    update.name = "X";
    assertThrows(WebApplicationException.class, () -> productResource.update(999L, update));
  }

  @Test
  public void testDeleteProductSuccess() {
    Product existing = new Product("ToDelete");
    existing.id = 3L;
    when(productRepository.findById(3L)).thenReturn(existing);

    Response r = productResource.delete(3L);
    assertEquals(204, r.getStatus());
    verify(productRepository, times(1)).delete(existing);
  }

  @Test
  public void testDeleteProductNotFound() {
    when(productRepository.findById(999L)).thenReturn(null);
    assertThrows(WebApplicationException.class, () -> productResource.delete(999L));
  }
}