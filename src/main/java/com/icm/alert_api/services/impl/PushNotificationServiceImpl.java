package com.icm.alert_api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper; // 👈 IMPORT CORRECTO

import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.DeviceRegistrationModel;
import com.icm.alert_api.models.GroupUserModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.repositories.DeviceRegistrationRepository;
import com.icm.alert_api.repositories.GroupUserRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup; // 👈 jsoup
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceRegistrationRepository deviceRepo;
    private final NotificationGroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    /**
     * Convierte un string HTML a texto plano sin etiquetas
     * y con entidades (&nbsp;, &amp;, etc.) resueltas.
     */
    private String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

    @Override
    public void sendNewAlert(AlertModel alert) {
        String vehicleCode = alert.getVehicleCode();

        if (vehicleCode == null || vehicleCode.isBlank()) {
            log.warn("Alerta {} sin vehicleCode; no se puede resolver grupos/usuarios para push.", alert.getId());
            return;
        }

        Long companyId = alert.getCompany().getId();

        // 1) Buscar todos los grupos de esa empresa que tienen asignado ese montacargas
        List<NotificationGroupModel> groups =
                groupRepository.findByCompanyAndVehicleCodeAssigned(companyId, vehicleCode);

        if (groups.isEmpty()) {
            log.info("No hay grupos (companyId={}, vehicleCode={}) con ese montacargas. No se envían notificaciones.",
                    companyId, vehicleCode);
            return;
        }

        Set<Long> groupIds = groups.stream()
                .map(NotificationGroupModel::getId)
                .collect(Collectors.toSet());

        // 2) Membresías activas de esos grupos
        List<GroupUserModel> memberships =
                groupUserRepository.findByGroup_IdInAndActiveTrue(groupIds);

        if (memberships.isEmpty()) {
            log.info("No hay usuarios activos en grupos {} para vehicleCode={}", groupIds, vehicleCode);
            return;
        }

        // 3) IDs de usuario
        Set<Long> userIds = memberships.stream()
                .map(gu -> gu.getUser().getId())
                .collect(Collectors.toSet());

        // 4) Dispositivos activos de esos usuarios
        List<DeviceRegistrationModel> devices =
                deviceRepo.findByUserIdInAndActiveTrue(userIds);

        if (devices.isEmpty()) {
            log.info("No hay dispositivos activos para usuarios {} (vehicleCode={})", userIds, vehicleCode);
            return;
        }

        // 5) Construir mensajes para Expo
        List<Map<String, Object>> messages = new ArrayList<>();

        for (DeviceRegistrationModel dev : devices) {

            // 🔹 Limpiar HTML de la shortDescription
            String rawDescription = alert.getShortDescription();
            String plainDescription = toPlainText(rawDescription);

            String bodyText =
                    (plainDescription != null && !plainDescription.isBlank())
                            ? plainDescription
                            : "Nueva alerta del montacargas " + vehicleCode;

            Map<String, Object> msg = new HashMap<>();
            msg.put("to", dev.getExpoPushToken());
            msg.put("sound", "default");
            msg.put("title", "Alerta " + alert.getAlertType());
            msg.put("body", bodyText); // 👈 ya sin etiquetas HTML
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
