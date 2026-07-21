package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseUnitTest {

  @Test
  public void testCreateSuccess() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);

    when(store.findByBusinessUnitCode("C1")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(new Location("AMSTERDAM-001", 5, 1000));

    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "C1";
    w.location = "AMSTERDAM-001";
    w.capacity = 500;
    w.stock = 100;

    assertDoesNotThrow(() -> useCase.create(w));
    verify(store, times(1)).create(w);
    assertNotNull(w.createdAt);
  }

  @Test
  public void testCreateDuplicateBusinessCode() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "DUP";
    when(store.findByBusinessUnitCode("DUP")).thenReturn(existing);

    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "DUP";
    w.location = "AMSTERDAM-001";
    w.capacity = 10;
    w.stock = 1;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateInvalidLocation() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);

    when(store.findByBusinessUnitCode("L1")).thenReturn(null);
    when(resolver.resolveByIdentifier("BAD")).thenReturn(null);

    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse w = new Warehouse();
    w.businessUnitCode = "L1";
    w.location = "BAD";
    w.capacity = 10;
    w.stock = 1;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateCapacityExceedsLocation() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);

    when(store.findByBusinessUnitCode("CAP1")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(new Location("AMSTERDAM-001", 1, 100));

    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse w = new Warehouse();
    w.businessUnitCode = "CAP1";
    w.location = "AMSTERDAM-001";
    w.capacity = 200; // exceeds location max 100
    w.stock = 0;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateStockExceedsCapacity() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);

    when(store.findByBusinessUnitCode("S1")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(new Location("AMSTERDAM-001", 1, 1000));

    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse w = new Warehouse();
    w.businessUnitCode = "S1";
    w.location = "AMSTERDAM-001";
    w.capacity = 100;
    w.stock = 200;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
  }
}
