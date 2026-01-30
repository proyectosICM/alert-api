package com.icm.alert_api.utils;

public final class VehicleCodeNormalizer {
    private VehicleCodeNormalizer() {}

    public static String normalize(String raw) {
        if (raw == null) return null;
        // trim + upper + quitar espacios (mantiene guiones)
        String s = raw.trim().toUpperCase();
        s = s.replaceAll("\\s+", "");
        return s.isBlank() ? null : s;
    }
}
