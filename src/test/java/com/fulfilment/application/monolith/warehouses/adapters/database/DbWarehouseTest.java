package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class DbWarehouseTest {

  @Test
  public void testToWarehouse() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "DB1";
    db.location = "AMSTERDAM-001";
    db.capacity = 1000;
    db.stock = 500;
    db.createdAt = LocalDateTime.now();

    Warehouse w = db.toWarehouse();
    assertEquals("DB1", w.businessUnitCode);
    assertEquals("AMSTERDAM-001", w.location);
    assertEquals(1000, w.capacity);
    assertEquals(500, w.stock);
    assertNotNull(w.createdAt);
  }
}