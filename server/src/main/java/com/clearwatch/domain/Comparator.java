package com.clearwatch.domain;

public enum Comparator {
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL;

    public boolean test(double value, double threshold) {
        return switch (this) {
            case GREATER_THAN -> value > threshold;
            case GREATER_THAN_OR_EQUAL -> value >= threshold;
            case LESS_THAN -> value < threshold;
            case LESS_THAN_OR_EQUAL -> value <= threshold;
        };
    }
}
