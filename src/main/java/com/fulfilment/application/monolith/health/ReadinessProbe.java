package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@ApplicationScoped
@Readiness
public class ReadinessProbe implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      probe.checkRepositoryAccess();
      return HealthCheckResponse.up("Application Ready");
    } catch (Exception e) {
      return HealthCheckResponse.down("Application Not Ready - " + e.getMessage());
    }
  }
}
