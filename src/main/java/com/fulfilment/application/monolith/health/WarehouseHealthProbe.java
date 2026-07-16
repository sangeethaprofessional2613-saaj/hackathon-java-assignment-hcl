package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseHealthProbe {

  private static final Logger LOGGER = Logger.getLogger(WarehouseHealthProbe.class.getName());

  @Inject
  private EntityManager entityManager;

  public void checkDatabaseConnectivity() {
    try {
      entityManager.createQuery("SELECT 1").getSingleResult();
      LOGGER.debug("Database connectivity check passed");
    } catch (Exception e) {
      LOGGER.error("Database connectivity check failed", e);
      throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
    }
  }

  public void checkRepositoryAccess() {
    try {
      // Try a simple query to verify repository is accessible
      entityManager.createQuery("SELECT COUNT(w) FROM DbWarehouse w").getSingleResult();
      LOGGER.debug("Repository access check passed");
    } catch (Exception e) {
      LOGGER.error("Repository access check failed", e);
      throw new RuntimeException("Repository access failed: " + e.getMessage(), e);
    }
  }
}
