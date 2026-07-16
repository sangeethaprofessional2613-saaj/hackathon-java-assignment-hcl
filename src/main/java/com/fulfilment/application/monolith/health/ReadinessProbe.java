package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@ApplicationScoped
@Readiness
public class ReadinessProbe implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      return HealthCheckResponse.up("Application Ready").build();
    } catch (Exception e) {
      return HealthCheckResponse.down("Application Not Ready")
          .withData("reason", "Database not accessible: " + e.getMessage())
          .build();
    }
  }
}
