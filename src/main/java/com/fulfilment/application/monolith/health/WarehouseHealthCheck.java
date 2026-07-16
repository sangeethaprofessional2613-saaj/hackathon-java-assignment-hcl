package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@ApplicationScoped
@Health
public class WarehouseHealthCheck implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      probe.checkRepositoryAccess();
      
      return HealthCheckResponse.up("Warehouse Service")
          .withData("database", "connected")
          .withData("repository", "accessible")
          .build();
    } catch (Exception e) {
      return HealthCheckResponse.down("Warehouse Service")
          .withData("error", e.getMessage())
          .build();
    }
  }
}
