package com.clearwatch.config;

import com.clearwatch.domain.*;
import com.clearwatch.repository.AlertRuleRepository;
import com.clearwatch.repository.AppUserRepository;
import com.clearwatch.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds the admin user, the fictional fleet of services, and default alert rules on first boot. */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public static final List<String[]> FLEET = List.of(
            new String[]{"auth-service", "Authentication and session management"},
            new String[]{"orders-api", "Order creation and lifecycle API"},
            new String[]{"payments", "Payment processing and settlement"},
            new String[]{"inventory", "Stock levels and warehouse sync"},
            new String[]{"notifications", "Email, SMS and push delivery"},
            new String[]{"gateway", "Public API gateway and routing"}
    );

    private final AppUserRepository appUserRepository;
    private final ServiceRepository serviceRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClearWatchProperties properties;

    public DataSeeder(AppUserRepository appUserRepository,
                       ServiceRepository serviceRepository,
                       AlertRuleRepository alertRuleRepository,
                       PasswordEncoder passwordEncoder,
                       ClearWatchProperties properties) {
        this.appUserRepository = appUserRepository;
        this.serviceRepository = serviceRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedServices();
        seedAlertRules();
    }

    private void seedAdmin() {
        if (appUserRepository.count() == 0) {
            String email = properties.getAdmin().getEmail();
            AppUser user = new AppUser(email, passwordEncoder.encode(properties.getAdmin().getPassword()), Role.ADMIN);
            appUserRepository.save(user);
            log.info("Seeded admin user '{}'", email);
        }
    }

    private void seedServices() {
        for (String[] entry : FLEET) {
            serviceRepository.findByName(entry[0]).orElseGet(() -> {
                ServiceEntity saved = serviceRepository.save(new ServiceEntity(entry[0], entry[1]));
                log.info("Seeded service '{}'", entry[0]);
                return saved;
            });
        }
    }

    private void seedAlertRules() {
        if (alertRuleRepository.count() > 0) {
            return;
        }
        alertRuleRepository.save(new AlertRule("High error rate", null, MetricType.ERROR_RATE,
                Comparator.GREATER_THAN, 5.0, 60, Severity.HIGH, true));
        alertRuleRepository.save(new AlertRule("Critical error rate", null, MetricType.ERROR_RATE,
                Comparator.GREATER_THAN, 25.0, 30, Severity.CRITICAL, true));
        alertRuleRepository.save(new AlertRule("High p95 latency", null, MetricType.LATENCY_P95,
                Comparator.GREATER_THAN, 800.0, 60, Severity.MEDIUM, true));
        alertRuleRepository.save(new AlertRule("Severe p95 latency", null, MetricType.LATENCY_P95,
                Comparator.GREATER_THAN, 2000.0, 30, Severity.CRITICAL, true));
        alertRuleRepository.save(new AlertRule("Request rate collapse", null, MetricType.REQUEST_RATE,
                Comparator.LESS_THAN, 2.0, 30, Severity.CRITICAL, true));
        log.info("Seeded default alert rules");
    }
}
