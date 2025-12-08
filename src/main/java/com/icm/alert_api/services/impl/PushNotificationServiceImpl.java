package com.icm.alert_api.services.impl;

import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.DeviceRegistrationModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.DeviceRegistrationRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceRegistrationRepository deviceRepo;
    private final NotificationGroupRepository groupRepository;
    private final UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    @Override
    public void sendNewAlert(AlertModel alert) {
        String vehicleCode = alert.getVehicleCode();

        if (vehicleCode == null || vehicleCode.isBlank()) {
            log.warn("Alerta {} sin vehicleCode; no se puede resolver grupos/usuarios para push.", alert.getId());
            return;
        }

        // 1) Buscar todos los grupos que tienen asignado ese montacargas
        List<NotificationGroupModel> groups =
                groupRepository.findByVehicleCodeAssigned(vehicleCode);

        if (groups.isEmpty()) {
            log.info("No hay grupos con el montacargas {}. No se envían notificaciones.", vehicleCode);
            return;
        }

        // 2) Obtener IDs de grupo
        Set<Long> groupIds = groups.stream()
                .map(NotificationGroupModel::getId)
                .collect(Collectors.toSet());

        // 3) Usuarios que pertenecen a cualquiera de esos grupos
        List<UserModel> users = userRepository.findByNotificationGroup_IdIn(groupIds);

        if (users.isEmpty()) {
            log.info("No hay usuarios en grupos {} para vehicleCode={}", groupIds, vehicleCode);
            return;
        }

        // 4) IDs de usuario (sin duplicados)
        Set<Long> userIds = users.stream()
                .map(UserModel::getId)
                .collect(Collectors.toSet());

        // 5) Dispositivos activos de esos usuarios
        List<DeviceRegistrationModel> devices =
                deviceRepo.findByUserIdInAndActiveTrue(userIds);

        if (devices.isEmpty()) {
            log.info("No hay dispositivos activos para usuarios {} (vehicleCode={})", userIds, vehicleCode);
            return;
        }

        // 6) Construir mensajes para Expo
        List<Map<String, Object>> messages = new ArrayList<>();

        for (DeviceRegistrationModel dev : devices) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("to", dev.getExpoPushToken());
            msg.put("sound", "default");
            msg.put("title", "Alerta " + alert.getAlertType());
            msg.put("body",
                    Optional.ofNullable(alert.getShortDescription())
                            .filter(s -> !s.isBlank())
                            .orElse("Nueva alerta del montacargas " + vehicleCode)
            );
            msg.put("data", Map.of(
                    "alertId", alert.getId(),
                    "vehicleCode", vehicleCode
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
