package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    
    if (dbWarehouse == null) {
      throw new IllegalArgumentException("Warehouse with business unit code '" + warehouse.businessUnitCode + "' not found");
    }
    
    // Update using entity-based approach to enable optimistic locking
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    // The @Version field will be automatically incremented by Hibernate
    // and will trigger OptimisticLockException if the version has changed
    getEntityManager().merge(dbWarehouse);
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  public List<Warehouse> searchWarehouses(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize) {
    
    StringBuilder queryBuilder = new StringBuilder("SELECT w FROM DbWarehouse w WHERE w.archivedAt IS NULL");
    
    if (location != null && !location.isBlank()) {
      queryBuilder.append(" AND w.location = ?1");
    }
    if (minCapacity != null) {
      queryBuilder.append(" AND w.capacity >= ?2");
    }
    if (maxCapacity != null) {
      queryBuilder.append(" AND w.capacity <= ?3");
    }
    
    // Add sorting
    String sortField = "createdAt";
    if ("capacity".equals(sortBy)) {
      sortField = "capacity";
    }
    String sortDirection = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
    queryBuilder.append(" ORDER BY w.").append(sortField).append(" ").append(sortDirection);
    
    var query = getEntityManager().createQuery(queryBuilder.toString(), DbWarehouse.class);
    
    int paramIndex = 1;
    if (location != null && !location.isBlank()) {
      query.setParameter(1, location);
      paramIndex++;
    }
    if (minCapacity != null) {
      query.setParameter(paramIndex, minCapacity);
      paramIndex++;
    }
    if (maxCapacity != null) {
      query.setParameter(paramIndex, maxCapacity);
    }
    
    // Apply pagination
    query.setFirstResult(page * pageSize);
    query.setMaxResults(pageSize);
    
    return query.getResultList().stream().map(DbWarehouse::toWarehouse).toList();
  }

  public long countWarehouses(
      String location,
      Integer minCapacity,
      Integer maxCapacity) {
    
    StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(w) FROM DbWarehouse w WHERE w.archivedAt IS NULL");
    
    if (location != null && !location.isBlank()) {
      queryBuilder.append(" AND w.location = ?1");
    }
    if (minCapacity != null) {
      queryBuilder.append(" AND w.capacity >= ?2");
    }
    if (maxCapacity != null) {
      queryBuilder.append(" AND w.capacity <= ?3");
    }
    
    var query = getEntityManager().createQuery(queryBuilder.toString(), Long.class);
    
    int paramIndex = 1;
    if (location != null && !location.isBlank()) {
      query.setParameter(1, location);
      paramIndex++;
    }
    if (minCapacity != null) {
      query.setParameter(paramIndex, minCapacity);
      paramIndex++;
    }
    if (maxCapacity != null) {
      query.setParameter(paramIndex, maxCapacity);
    }
    
    return query.getSingleResult();
  }
}
