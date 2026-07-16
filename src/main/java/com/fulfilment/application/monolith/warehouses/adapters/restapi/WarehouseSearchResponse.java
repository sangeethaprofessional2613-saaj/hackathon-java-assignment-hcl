package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.warehouse.api.beans.Warehouse;
import java.util.List;

public class WarehouseSearchResponse {
  public List<Warehouse> items;
  public int pageNumber;
  public int pageSize;
  public long totalItems;
  public int totalPages;

  public WarehouseSearchResponse(
      List<Warehouse> items,
      int pageNumber,
      int pageSize,
      long totalItems) {
    this.items = items;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
    this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
  }
}
