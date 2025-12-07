package com.icm.alert_api.services.impl;

import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.DeviceRegistrationModel;
import com.icm.alert_api.repositories.DeviceRegistrationRepository;
import com.icm.alert_api.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceRegistrationRepository deviceRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    @Override
    public void sendNewAlert(AlertModel alert) {
        // 🔹 Aquí decides a quién notificar:
        //   por ejemplo, todos los dispositivos del grupo 1, o todos los del sistema
        Long userId = 1L; // TODO: relacionar alert con grupo/usuarios
        List<DeviceRegistrationModel> devices =
                deviceRepo.findByUserIdAndActiveTrue(userId);

        if (devices.isEmpty()) {
            log.info("No hay dispositivos registrados para userId={}", userId);
            return;
        }

        List<Map<String, Object>> messages = new ArrayList<>();

        for (DeviceRegistrationModel dev : devices) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("to", dev.getExpoPushToken());
            msg.put("sound", "default");
            msg.put("title", "Alerta " + alert.getAlertType());
            msg.put("body", alert.getShortDescription());
            msg.put("data", Map.of(
                    "alertId", alert.getId(),
                    "vehicleCode", alert.getVehicleCode()
            ));
            messages.add(msg);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, Object>>> entity =
                    new HttpEntity<>(messages, headers);

            ResponseEntity<String> resp =
                    restTemplate.postForEntity(EXPO_PUSH_URL, entity, String.class);

            log.info("Expo push status={}, body={}", resp.getStatusCode(), resp.getBody());
        } catch (Exception e) {
            log.error("Error enviando push a Expo", e);
        }
    }
}
