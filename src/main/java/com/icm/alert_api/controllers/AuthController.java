package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.DniLoginRequest;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @PostMapping("/login-dni")
    public ResponseEntity<?> loginByDni(@RequestBody DniLoginRequest request) {
        if (request.getDni() == null || request.getDni().isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "dni es obligatorio"));
        }

        UserModel user = userRepository.findByDni(request.getDni())
                .orElseThrow(() ->
                        new RuntimeException("DNI no encontrado o usuario inactivo")
                );

        // ⚠️ A nivel de seguridad real, aquí deberías poner alguna validación extra:
        // PIN, código temporal, restricciones de rol, etc.

        String token = jwtUtils.generateAccesToken(user.getUsername());

        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("token", token);
        httpResponse.put("message", "Autenticación por DNI correcta");
        httpResponse.put("username", user.getUsername());
        httpResponse.put("dni", user.getDni());
        httpResponse.put("role", user.getRole().name());

        return ResponseEntity.ok(httpResponse);
    }
}
