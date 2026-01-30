package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.alert.AlertDetailDto;
import com.icm.alert_api.dto.alert.AlertSummaryDto;
import com.icm.alert_api.dto.alert.CreateAlertRequest;
import com.icm.alert_api.dto.alert.UpdateAlertRequest;
import com.icm.alert_api.mappers.AlertMapper;
import com.icm.alert_api.models.AlertModel;
import com.icm.alert_api.models.CompanyModel;
import com.icm.alert_api.models.NotificationGroupModel;
import com.icm.alert_api.models.UserModel;
import com.icm.alert_api.repositories.*;
import com.icm.alert_api.services.AlertService;
import com.icm.alert_api.services.FleetService;
import com.icm.alert_api.services.PushNotificationService;
import com.icm.alert_api.specs.AlertSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final NotificationGroupRepository groupRepository;
    private final CompanyRepository companyRepository;
    private final PushNotificationService pushNotificationService;
    private final AlertMapper alertMapper;

    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final FleetService fleetService;

    // ============== CRUD ==============

    @Override
    public AlertDetailDto create(CreateAlertRequest request) {
        String vehicleCode = request.getVehicleCode();
        if (vehicleCode == null || vehicleCode.isBlank()) {
            throw new IllegalArgumentException("vehicleCode is required to create an alert");
        }

        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required to create an alert");
        }

        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        /*
        var groups = groupRepository.findByVehicleCode(vehicleCode);

        if (groups.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se encontró ningún grupo con el vehicleCode: " + vehicleCode
            );
        }

        // Opcional: si hay más de un grupo, verificar que todas las empresas sean la misma
        CompanyModel company = groups.get(0).getCompany();
        boolean multipleCompanies = groups.stream()
                .map(g -> g.getCompany().getId())
                .distinct()
                .count() > 1;

        if (multipleCompanies) {
            throw new IllegalArgumentException(
                    "El vehicleCode " + vehicleCode + " está asociado a múltiples compañías; " +
                            "no se puede determinar companyId de forma unívoca."
            );
        }
*/

        AlertModel model = alertMapper.toEntity(request);
        model.setCompany(company);

        AlertModel saved = alertRepository.save(model);

        // 3) Notificar por push a los usuarios de los grupos correspondientes
        pushNotificationService.sendNewAlert(saved);

        return alertMapper.toDetailDto(saved);
    }


    @Override
    public AlertDetailDto update(Long companyId, Long alertId, UpdateAlertRequest request) {
        AlertModel model = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Alert does not belong to company: " + companyId);
        }

        // PATCH con MapStruct (ignora nulls)
        alertMapper.updateEntityFromDto(request, model);

        AlertModel updated = alertRepository.save(model);
        return alertMapper.toDetailDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlertDetailDto> findById(Long companyId, Long alertId) {
        return alertRepository.findById(alertId)
                .filter(a -> a.getCompany().getId().equals(companyId))
                .map(alertMapper::toDetailDto);
    }

    @Override
    public void deleteById(Long companyId, Long alertId) {
        AlertModel model = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Alert does not belong to company: " + companyId);
        }

        alertRepository.delete(model);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listAll(Long companyId, Pageable pageable) {
        Page<AlertModel> page = alertRepository.findByCompanyIdOrderByEventTimeDesc(companyId, pageable);
        return page.map(alertMapper::toSummaryDto);
    }

    // ============== Historial por grupo ==============

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByGroup(Long companyId, Long groupId, Pageable pageable) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<AlertModel> page = alertRepository
                .findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(companyId, vehicleCodes, pageable);

        return page.map(alertMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByGroupAndDateRange(
            Long companyId,
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    ) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<AlertModel> page = alertRepository
                .findByCompanyIdAndVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
                        companyId, vehicleCodes, from, to, pageable
                );

        return page.map(alertMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLast24hForGroup(Long companyId, Long groupId) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        Set<String> vehicleCodes = group.getVehicleCodes();
        if (vehicleCodes == null || vehicleCodes.isEmpty()) {
            return 0L;
        }

        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(24);
        return alertRepository.countByCompanyIdAndVehicleCodeInAndEventTimeAfter(companyId, vehicleCodes, cutoff);
    }

    @Override
    public AlertDetailDto acknowledge(Long companyId, Long alertId) {
        AlertModel model = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Alert does not belong to company: " + companyId);
        }

        if (!model.isAcknowledged()) {
            model.setAcknowledged(true);
            alertRepository.save(model);
        }

        return alertMapper.toDetailDto(model);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByUser(Long companyId, Long userId, Pageable pageable) {
        // 1) Validar que el usuario existe
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2) Validar que pertenece a la empresa
        if (!user.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException(
                    "User does not belong to company: " + companyId
            );
        }

        // 3) Obtener grupos activos del usuario
        var memberships = groupUserRepository.findByUser_IdAndActiveTrue(userId);
        if (memberships.isEmpty()) {
            return Page.empty(pageable);
        }

        // 4) Unir todos los vehicleCodes de los grupos
        Set<String> vehicleCodes = memberships.stream()
                .map(m -> m.getGroup())
                .filter(g -> g.getVehicleCodes() != null)
                .flatMap(g -> g.getVehicleCodes().stream())
                .collect(java.util.stream.Collectors.toSet());

        if (vehicleCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        // 5) Buscar alertas por company + vehicleCodes, ordenadas por fecha desc
        Page<AlertModel> page = alertRepository
                .findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(
                        companyId, vehicleCodes, pageable
                );

        return page.map(alertMapper::toSummaryDto);
    }

    @Override
    public long countByDay(Long companyId, LocalDate day, ZoneId zone, Long fleetId) {
        ZonedDateTime from = day.atStartOfDay(zone);
        ZonedDateTime to = day.plusDays(1).atStartOfDay(zone);

        if (fleetId == null) {
            return alertRepository.countByCompany_IdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                    companyId, from, to
            );
        }

        List<String> fleetCodes = fleetService.getVehicleCodes(companyId, fleetId);
        Set<String> codes = fleetCodes.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        if (codes.isEmpty()) return 0L;

        return alertRepository.countByCompany_IdAndVehicleCodeInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                companyId, codes, from, to
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> search(
            Long companyId,
            Set<String> alertTypes,
            Long fleetId,
            Long groupId,
            ZonedDateTime from,
            ZonedDateTime to,
            Boolean acknowledged,
            Pageable pageable
    ) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }

        // 1) Resolver vehicleCodes según fleetId / groupId
        Set<String> vehicleCodes = null;

        if (fleetId != null) {
            List<String> fleetCodes = fleetService.getVehicleCodes(companyId, fleetId);
            Set<String> fleetSet = fleetCodes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toSet());
            vehicleCodes = (vehicleCodes == null) ? fleetSet : intersect(vehicleCodes, fleetSet);
        }

        if (groupId != null) {
            NotificationGroupModel group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

            if (!group.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("Group does not belong to company: " + companyId);
            }

            Set<String> groupSet = (group.getVehicleCodes() == null)
                    ? Set.of()
                    : group.getVehicleCodes().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toSet());

            vehicleCodes = (vehicleCodes == null) ? new HashSet<>(groupSet) : intersect(vehicleCodes, groupSet);
        }

        // Si pidieron filtro por fleet/group y quedó vacío => no hay nada que buscar
        if ((fleetId != null || groupId != null) && (vehicleCodes == null || vehicleCodes.isEmpty())) {
            return Page.empty(pageable);
        }

        // 2) Normalizar alertTypes
        Set<String> typesNorm = null;
        if (alertTypes != null && !alertTypes.isEmpty()) {
            typesNorm = alertTypes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toSet());
            if (typesNorm.isEmpty()) typesNorm = null;
        }

        // 3) Pageable con sort default si no viene sort
        Pageable effectivePageable = pageable;
        if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "eventTime")
            );
        }

        // 4) Construir Specification
        Specification<AlertModel> spec = Specification.where(AlertSpecifications.companyId(companyId));

        if (typesNorm != null) {
            spec = spec.and(AlertSpecifications.alertTypeIn(typesNorm));
        }

        if (vehicleCodes != null && !vehicleCodes.isEmpty()) {
            spec = spec.and(AlertSpecifications.vehicleCodeIn(vehicleCodes));
        }

        if (acknowledged != null) {
            spec = spec.and(AlertSpecifications.acknowledged(acknowledged));
        }

        if (from != null) {
            spec = spec.and(AlertSpecifications.eventTimeFrom(from));
        }

        if (to != null) {
            spec = spec.and(AlertSpecifications.eventTimeTo(to));
        }

        // 5) Ejecutar query y mapear
        return alertRepository.findAll(spec, effectivePageable)
                .map(alertMapper::toSummaryDto);
    }

    private Set<String> intersect(Set<String> a, Set<String> b) {
        Set<String> res = new HashSet<>(a);
        res.retainAll(b);
        return res;
    }

}
