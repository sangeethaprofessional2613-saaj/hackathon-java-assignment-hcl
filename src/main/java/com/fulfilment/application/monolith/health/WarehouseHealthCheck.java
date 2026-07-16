package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

@ApplicationScoped
@Startup
public class WarehouseHealthCheck implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      probe.checkRepositoryAccess();
      
      return HealthCheckResponse.up("Warehouse Service is ready");
    } catch (Exception e) {
      return HealthCheckResponse.down("Warehouse Service failed: " + e.getMessage());
    }
  }
}
