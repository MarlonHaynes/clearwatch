package com.clearwatch.simulator;

import java.time.Instant;

public class ActiveFailure {
    public final Long serviceId;
    public final FailureScenario scenario;
    public final Instant startedAt;
    public final Instant endsAt;

    public ActiveFailure(Long serviceId, FailureScenario scenario, Instant startedAt, Instant endsAt) {
        this.serviceId = serviceId;
        this.scenario = scenario;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }

    public boolean isActiveAt(Instant now) {
        return !now.isBefore(startedAt) && now.isBefore(endsAt);
    }
}
