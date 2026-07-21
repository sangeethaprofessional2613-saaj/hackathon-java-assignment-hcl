package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseResourceImplUnitTest {

  @InjectMock
  com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository warehouseRepository;

  @InjectMock
  com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation createOp;

  @InjectMock
  com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation archiveOp;

  @InjectMock
  com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation replaceOp;

  @Inject
  WarehouseResourceImpl resource;

  @Test
  public void testListAllWarehouses() {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w.businessUnitCode = "B1";
    w.location = "AMSTERDAM-001";
    List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> list = new ArrayList<>();
    list.add(w);

    when(warehouseRepository.getAll()).thenReturn(list);

    List<Warehouse> res = resource.listAllWarehousesUnits();
    assertEquals(1, res.size());
    assertEquals("B1", res.get(0).getBusinessUnitCode());
  }

  @Test
  public void testCreateSuccess() {
    Warehouse bean = new Warehouse();
    bean.setBusinessUnitCode("NEW1");
    bean.setLocation("AMSTERDAM-001");
    bean.setCapacity(100);
    bean.setStock(10);

    // createOp does nothing
    doNothing().when(createOp).create(any());

    Warehouse resp = resource.createANewWarehouseUnit(bean);
    assertEquals("NEW1", resp.getBusinessUnitCode());
  }

  @Test
  public void testCreateFailure() {
    Warehouse bean = new Warehouse();
    bean.setBusinessUnitCode("NEW2");
    bean.setLocation("UNKNOWN");
    bean.setCapacity(1000);
    bean.setStock(900);

    doThrow(new IllegalArgumentException("bad")).when(createOp).create(any());

    assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(bean));
  }

  @Test
  public void testGetNotFound() {
    when(warehouseRepository.findByBusinessUnitCode("X")).thenReturn(null);
    assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("X"));
  }

  @Test
  public void testGetFound() {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w.businessUnitCode = "B2";
    w.location = "TILBURG-001";
    when(warehouseRepository.findByBusinessUnitCode("B2")).thenReturn(w);

    Warehouse resp = resource.getAWarehouseUnitByID("B2");
    assertEquals("B2", resp.getBusinessUnitCode());
  }

  @Test
  public void testArchiveNotFound() {
    when(warehouseRepository.findByBusinessUnitCode("X")).thenReturn(null);
    assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("X"));
  }

  @Test
  public void testArchiveSuccess() {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w.businessUnitCode = "B3";
    when(warehouseRepository.findByBusinessUnitCode("B3")).thenReturn(w);
    doNothing().when(archiveOp).archive(w);

    resource.archiveAWarehouseUnitByID("B3");
    // no exception = success
  }

  @Test
  public void testReplaceFailure() {
    Warehouse bean = new Warehouse();
    bean.setLocation("AMSTERDAM-001");
    bean.setCapacity(500);
    bean.setStock(200);

    doThrow(new IllegalArgumentException("bad")).when(replaceOp).replace(any());
    when(warehouseRepository.findByBusinessUnitCode("R1")).thenReturn(null);

    assertThrows(WebApplicationException.class, () -> resource.replaceTheCurrentActiveWarehouse("R1", bean));
  }

  @Test
  public void testSearchInvalidPage() {
    assertThrows(WebApplicationException.class, () -> resource.searchWarehouses(null, null, null, "createdAt", "asc", -1, 10));
  }

  @Test
  public void testSearchInvalidPageSize() {
    assertThrows(WebApplicationException.class, () -> resource.searchWarehouses(null, null, null, "createdAt", "asc", 0, 101));
  }

  @Test
  public void testSearchInvalidSortBy() {
    assertThrows(WebApplicationException.class, () -> resource.searchWarehouses(null, null, null, "unknown", "asc", 0, 10));
  }

  @Test
  public void testSearchInvalidSortOrder() {
    assertThrows(WebApplicationException.class, () -> resource.searchWarehouses(null, null, null, "createdAt", "up", 0, 10));
  }

  @Test
  public void testSearchSuccess() {
    when(warehouseRepository.countWarehouses(null, null, null)).thenReturn(2L);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w1 = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w1.businessUnitCode = "S1";
    when(warehouseRepository.searchWarehouses(null, null, null, "createdAt", "asc", 0, 10)).thenReturn(List.of(w1));

    WarehouseSearchResponse resp = resource.searchWarehouses(null, null, null, "createdAt", "asc", 0, 10);
    assertEquals(1, resp.items.size());
    assertEquals(2L, resp.totalItems);
  }
}
