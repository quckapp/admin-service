package com.quckapp.admin.domain;

import java.util.*;

public final class EnvironmentChain {

    private EnvironmentChain() {}

    private static final List<String> CHAIN = List.of(
            "local", "dev", "qa", "uat", "staging", "production", "live"
    );

    private static final Set<String> UAT_VARIANTS = Set.of("uat", "uat1", "uat2", "uat3");

    public static String normalize(String environment) {
        Objects.requireNonNull(environment);
        return UAT_VARIANTS.contains(environment.toLowerCase()) ? "uat" : environment.toLowerCase();
    }

    public static Optional<String> previousOf(String environment) {
        int index = indexOf(environment);
        return index <= 0 ? Optional.empty() : Optional.of(CHAIN.get(index - 1));
    }

    public static Optional<String> nextOf(String environment) {
        int index = indexOf(environment);
        return index >= CHAIN.size() - 1 ? Optional.empty() : Optional.of(CHAIN.get(index + 1));
    }

    public static boolean isUnrestricted(String environment) {
        return "local".equalsIgnoreCase(environment);
    }

    public static boolean isValidPromotion(String from, String to) {
        int fromIndex = indexOf(from);
        int toIndex = indexOf(to);
        return toIndex == fromIndex + 1;
    }

    private static int indexOf(String environment) {
        String normalized = normalize(environment);
        int index = CHAIN.indexOf(normalized);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown environment: " + environment
                    + ". Valid: " + CHAIN);
        }
        return index;
    }
}
