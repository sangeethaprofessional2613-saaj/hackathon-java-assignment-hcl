package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    LOGGER.info("Listing all warehouse units");
    List<Warehouse> warehouses = warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    LOGGER.debug("Retrieved " + warehouses.size() + " warehouses");
    return warehouses;
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.info("Creating new warehouse unit with business code: " + data.getBusinessUnitCode());
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      createWarehouseOperation.create(domainWarehouse);
      LOGGER.info("Warehouse created successfully: " + domainWarehouse.businessUnitCode);
      return toWarehouseResponse(domainWarehouse);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Failed to create warehouse", e);
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.info("Fetching warehouse unit with business code: " + id);
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
    
    if (domainWarehouse == null) {
      LOGGER.warn("Warehouse not found with business code: " + id);
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }
    
    LOGGER.debug("Warehouse found: " + id);
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.info("Archiving warehouse unit with business code: " + id);
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      LOGGER.warn("Cannot archive - warehouse not found with business code: " + id);
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      archiveWarehouseOperation.archive(domainWarehouse);
      LOGGER.info("Warehouse archived successfully: " + id);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Failed to archive warehouse", e);
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    LOGGER.info("Replacing warehouse with business code: " + businessUnitCode);
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode;
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      replaceWarehouseOperation.replace(domainWarehouse);
      var updated = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
      LOGGER.info("Warehouse replaced successfully: " + businessUnitCode);
      return toWarehouseResponse(updated);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Failed to replace warehouse", e);
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @GET
  @Path("/search")
  public WarehouseSearchResponse searchWarehouses(
      @QueryParam("location") String location,
      @QueryParam("minCapacity") Integer minCapacity,
      @QueryParam("maxCapacity") Integer maxCapacity,
      @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
      @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
    
    LOGGER.info("Searching warehouses with filters - location: " + location + ", page: " + page + ", pageSize: " + pageSize);
    
    if (page < 0) {
      throw new WebApplicationException("Page number must be >= 0", 400);
    }
    if (pageSize < 1 || pageSize > 100) {
      throw new WebApplicationException("Page size must be between 1 and 100", 400);
    }
    
    if (!("createdAt".equals(sortBy) || "capacity".equals(sortBy))) {
      throw new WebApplicationException("Invalid sortBy value. Must be 'createdAt' or 'capacity'", 400);
    }
    if (!("asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder))) {
      throw new WebApplicationException("Invalid sortOrder value. Must be 'asc' or 'desc'", 400);
    }
    
    long totalItems = warehouseRepository.countWarehouses(location, minCapacity, maxCapacity);
    
    List<Warehouse> results = warehouseRepository
        .searchWarehouses(location, minCapacity, maxCapacity, sortBy, sortOrder, page, pageSize)
        .stream()
        .map(this::toWarehouseResponse)
        .toList();
    
    LOGGER.debug("Search returned " + results.size() + " results out of " + totalItems + " total");
    return new WarehouseSearchResponse(results, page, pageSize, totalItems);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
