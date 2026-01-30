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
import java.util.*;
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

    // ============================================================
    // Helpers: normalización + regla "placa manda"
    // ============================================================

    private Set<String> normalizeSet(Set<String> in) {
        if (in == null) return Set.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> normalizeListToSet(List<String> in) {
        if (in == null) return Set.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> intersect(Set<String> a, Set<String> b) {
        Set<String> res = new HashSet<>(a);
        res.retainAll(b);
        return res;
    }

    private record VehicleFilter(Set<String> plates, Set<String> codes) {
        boolean hasPlates() { return plates != null && !plates.isEmpty(); }
        boolean hasCodes()  { return codes != null && !codes.isEmpty(); }
    }

    /**
     * Regla:
     * - Si hay placas => usar placas (principal)
     * - Si no hay placas pero hay codes => usar codes (fallback)
     */
    private VehicleFilter groupVehicleFilter(NotificationGroupModel group) {
        Set<String> plates = normalizeSet(group.getVehiclePlates());
        Set<String> codes  = normalizeSet(group.getVehicleCodes());
        return new VehicleFilter(plates, codes);
    }

    /**
     * Si fleetService todavía NO tiene getVehiclePlates, agrega ese método.
     * Si aún no puedes, puedes mapear codes -> placas en otro lado, pero lo ideal:
     * FleetService.getVehiclePlates(companyId, fleetId)
     */
    private VehicleFilter fleetVehicleFilter(Long companyId, Long fleetId) {
        Set<String> plates = Set.of();
        try {
            // ✅ esperado (recomendado)
            List<String> fleetPlates = fleetService.getVehiclePlates(companyId, fleetId);
            plates = normalizeListToSet(fleetPlates);
        } catch (Exception ignored) {
            // Si tu FleetService aún no tiene getVehiclePlates, no revientes:
            plates = Set.of();
        }

        List<String> fleetCodes = fleetService.getVehicleCodes(companyId, fleetId);
        Set<String> codes = normalizeListToSet(fleetCodes);

        return new VehicleFilter(plates, codes);
    }

    /**
     * Para search: combinar filtros de fleet + group con intersección,
     * pero siempre priorizando placas si existen.
     */
    private VehicleFilter resolveVehicleFilter(Long companyId, Long fleetId, Long groupId) {
        Set<String> plates = null;
        Set<String> codes = null;

        if (fleetId != null) {
            VehicleFilter fv = fleetVehicleFilter(companyId, fleetId);
            Set<String> fleetPlates = fv.plates();
            Set<String> fleetCodes  = fv.codes();

            if (!fleetPlates.isEmpty()) {
                plates = (plates == null) ? new HashSet<>(fleetPlates) : intersect(plates, fleetPlates);
            }
            if (!fleetCodes.isEmpty()) {
                codes = (codes == null) ? new HashSet<>(fleetCodes) : intersect(codes, fleetCodes);
            }
        }

        if (groupId != null) {
            NotificationGroupModel group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

            if (!group.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("Group does not belong to company: " + companyId);
            }

            VehicleFilter gv = groupVehicleFilter(group);
            Set<String> groupPlates = gv.plates();
            Set<String> groupCodes  = gv.codes();

            if (!groupPlates.isEmpty()) {
                plates = (plates == null) ? new HashSet<>(groupPlates) : intersect(plates, groupPlates);
            }
            if (!groupCodes.isEmpty()) {
                codes = (codes == null) ? new HashSet<>(groupCodes) : intersect(codes, groupCodes);
            }
        }

        if (plates == null) plates = Set.of();
        if (codes == null) codes = Set.of();

        return new VehicleFilter(plates, codes);
    }

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

        AlertModel model = alertMapper.toEntity(request);
        model.setCompany(company);

        AlertModel saved = alertRepository.save(model);

        // Notificar por push
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

    // ============== Historial por grupo (placa primero) ==============

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByGroup(Long companyId, Long groupId, Pageable pageable) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        VehicleFilter vf = groupVehicleFilter(group);

        if (vf.hasPlates()) {
            Page<AlertModel> page = alertRepository
                    .findByCompanyIdAndLicensePlateInOrderByEventTimeDesc(companyId, vf.plates(), pageable);
            return page.map(alertMapper::toSummaryDto);
        }

        if (vf.hasCodes()) {
            Page<AlertModel> page = alertRepository
                    .findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(companyId, vf.codes(), pageable);
            return page.map(alertMapper::toSummaryDto);
        }

        return Page.empty(pageable);
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

        VehicleFilter vf = groupVehicleFilter(group);

        if (vf.hasPlates()) {
            Page<AlertModel> page = alertRepository
                    .findByCompanyIdAndLicensePlateInAndEventTimeBetweenOrderByEventTimeDesc(
                            companyId, vf.plates(), from, to, pageable
                    );
            return page.map(alertMapper::toSummaryDto);
        }

        if (vf.hasCodes()) {
            Page<AlertModel> page = alertRepository
                    .findByCompanyIdAndVehicleCodeInAndEventTimeBetweenOrderByEventTimeDesc(
                            companyId, vf.codes(), from, to, pageable
                    );
            return page.map(alertMapper::toSummaryDto);
        }

        return Page.empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLast24hForGroup(Long companyId, Long groupId) {
        NotificationGroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Group does not belong to company: " + companyId);
        }

        VehicleFilter vf = groupVehicleFilter(group);
        if (!vf.hasPlates() && !vf.hasCodes()) return 0L;

        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(24);

        if (vf.hasPlates()) {
            return alertRepository.countByCompanyIdAndLicensePlateInAndEventTimeAfter(companyId, vf.plates(), cutoff);
        }
        return alertRepository.countByCompanyIdAndVehicleCodeInAndEventTimeAfter(companyId, vf.codes(), cutoff);
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

    // ============== Alertas por usuario (placa primero) ==============

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDto> listByUser(Long companyId, Long userId, Pageable pageable) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("User does not belong to company: " + companyId);
        }

        var memberships = groupUserRepository.findByUser_IdAndActiveTrue(userId);
        if (memberships.isEmpty()) {
            return Page.empty(pageable);
        }

        // ✅ juntar placas primero
        Set<String> plates = memberships.stream()
                .map(m -> m.getGroup())
                .filter(Objects::nonNull)
                .map(NotificationGroupModel::getVehiclePlates)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (!plates.isEmpty()) {
            return alertRepository
                    .findByCompanyIdAndLicensePlateInOrderByEventTimeDesc(companyId, plates, pageable)
                    .map(alertMapper::toSummaryDto);
        }

        // fallback: codes
        Set<String> codes = memberships.stream()
                .map(m -> m.getGroup())
                .filter(Objects::nonNull)
                .map(NotificationGroupModel::getVehicleCodes)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (codes.isEmpty()) {
            return Page.empty(pageable);
        }

        return alertRepository
                .findByCompanyIdAndVehicleCodeInOrderByEventTimeDesc(companyId, codes, pageable)
                .map(alertMapper::toSummaryDto);
    }

    // ============== KPI countByDay (placa primero) ==============

    @Override
    public long countByDay(Long companyId, LocalDate day, ZoneId zone, Long fleetId) {
        ZonedDateTime from = day.atStartOfDay(zone);
        ZonedDateTime to = day.plusDays(1).atStartOfDay(zone);

        if (fleetId == null) {
            return alertRepository.countByCompany_IdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                    companyId, from, to
            );
        }

        VehicleFilter fv = fleetVehicleFilter(companyId, fleetId);

        // ✅ principal: placas
        if (fv.hasPlates()) {
            return alertRepository.countByCompanyIdAndLicensePlateInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                    companyId, fv.plates(), from, to
            );
        }

        // fallback: codes
        if (fv.hasCodes()) {
            return alertRepository.countByCompany_IdAndVehicleCodeInAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                    companyId, fv.codes(), from, to
            );
        }

        return 0L;
    }

    // ============== SEARCH (placa primero) ==============

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

        // 1) Resolver filtros (fleet+group) con regla placa primero
        VehicleFilter vf = resolveVehicleFilter(companyId, fleetId, groupId);

        // Si pidieron filtro por fleet/group y quedó vacío => no hay nada que buscar
        if ((fleetId != null || groupId != null) && !vf.hasPlates() && !vf.hasCodes()) {
            return Page.empty(pageable);
        }

        // 2) Normalizar alertTypes
        Set<String> typesNorm = null;
        if (alertTypes != null && !alertTypes.isEmpty()) {
            Set<String> t = alertTypes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
            if (!t.isEmpty()) typesNorm = t;
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

        // ✅ principal: placa
        if (vf.hasPlates()) {
            spec = spec.and(AlertSpecifications.licensePlateIn(vf.plates()));
        } else if (vf.hasCodes()) {
            // fallback: code
            spec = spec.and(AlertSpecifications.vehicleCodeIn(vf.codes()));
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

        return alertRepository.findAll(spec, effectivePageable)
                .map(alertMapper::toSummaryDto);
    }
}
