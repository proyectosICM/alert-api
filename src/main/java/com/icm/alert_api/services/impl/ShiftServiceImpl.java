package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.shift.CreateShiftRequest;
import com.icm.alert_api.dto.shift.ShiftDetailDto;
import com.icm.alert_api.dto.shift.ShiftSummaryDto;
import com.icm.alert_api.dto.shift.UpdateShiftRequest;
import com.icm.alert_api.enums.GroupSource;
import com.icm.alert_api.mappers.ShiftMapper;
import com.icm.alert_api.models.*;
import com.icm.alert_api.repositories.CompanyRepository;
import com.icm.alert_api.repositories.NotificationGroupRepository;
import com.icm.alert_api.repositories.ShiftRepository;
import com.icm.alert_api.repositories.UserRepository;
import com.icm.alert_api.services.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final CompanyRepository companyRepository;
    private final ShiftMapper shiftMapper;

    private final NotificationGroupRepository groupRepository;
    private final UserRepository userRepository;

    // =========================
    // Helpers
    // =========================

    private CompanyModel requireCompany(Long companyId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
    }

    private void requireBelongsToCompany(ShiftModel model, Long companyId) {
        if (model.getCompany() == null || model.getCompany().getId() == null) {
            throw new IllegalStateException("Shift has no company assigned");
        }
        if (!model.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Shift does not belong to company: " + companyId);
        }
    }

    private List<String> normalizeDnis(List<String> dnis) {
        if (dnis == null) return new ArrayList<>();
        return dnis.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                // opcional: solo dígitos
                // .map(s -> s.replaceAll("\\D+", ""))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String normalizePlate(String s) {
        if (s == null) return "";
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        x = x.replace("-", "");
        return x;
    }

    private List<String> normalizePlates(List<String> plates) {
        if (plates == null) return new ArrayList<>();
        // LinkedHashSet para dedup manteniendo orden
        Set<String> out = new LinkedHashSet<>();
        for (String p : plates) {
            if (p == null) continue;
            String n = normalizePlate(p);
            if (!n.isBlank()) out.add(n);
        }
        return new ArrayList<>(out);
    }

    private String ensureBatchId(String batchId) {
        if (batchId != null && batchId.isBlank()) {
            throw new IllegalArgumentException("batchId cannot be blank");
        }
        return (batchId == null) ? UUID.randomUUID().toString() : batchId;
    }

    private void validateShiftName(String shiftName) {
        if (shiftName == null || shiftName.isBlank()) {
            throw new IllegalArgumentException("shiftName is required");
        }
        if (shiftName.length() > 120) {
            throw new IllegalArgumentException("shiftName max length is 120");
        }
    }

    // =========================
    // CRUD
    // =========================

    @Override
    public ShiftDetailDto create(CreateShiftRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");

        Long companyId = request.getCompanyId();
        CompanyModel company = requireCompany(companyId);

        if (request.getRosterDate() == null) throw new IllegalArgumentException("rosterDate is required");
        validateShiftName(request.getShiftName());

        ShiftModel model = shiftMapper.toEntity(request);
        model.setCompany(company);

        // batchId: si no vino -> generar
        model.setBatchId(ensureBatchId(model.getBatchId()));

        // normalizar listas
        model.setResponsibleDnis(normalizeDnis(request.getResponsibleDnis()));
        model.setVehiclePlates(normalizePlates(request.getVehiclePlates()));

        ShiftModel saved = shiftRepository.save(model);
        return shiftMapper.toDetailDto(saved);
    }

    @Override
    public ShiftDetailDto update(Long companyId, Long shiftId, UpdateShiftRequest request) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (shiftId == null) throw new IllegalArgumentException("shiftId is required");
        if (request == null) throw new IllegalArgumentException("request is required");

        ShiftModel model = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        requireBelongsToCompany(model, companyId);

        if (request.getShiftName() != null) {
            validateShiftName(request.getShiftName());
        }
        if (request.getBatchId() != null && request.getBatchId().isBlank()) {
            throw new IllegalArgumentException("batchId cannot be blank");
        }

        shiftMapper.updateEntityFromDto(request, model);

        // Column batch_id es NOT NULL: si llega null no pasa (mapper ignora null),
        // pero si alguien manda blank ya lo bloqueamos arriba.

        // normalizar listas si vienen
        if (request.getResponsibleDnis() != null) {
            model.setResponsibleDnis(normalizeDnis(request.getResponsibleDnis()));
        }
        if (request.getVehiclePlates() != null) {
            model.setVehiclePlates(normalizePlates(request.getVehiclePlates()));
        }

        ShiftModel saved = shiftRepository.save(model);
        return shiftMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftDetailDto> findById(Long companyId, Long shiftId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (shiftId == null) throw new IllegalArgumentException("shiftId is required");

        return shiftRepository.findById(shiftId)
                .filter(s -> s.getCompany() != null && companyId.equals(s.getCompany().getId()))
                .map(shiftMapper::toDetailDto);
    }

    @Override
    public void deleteById(Long companyId, Long shiftId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (shiftId == null) throw new IllegalArgumentException("shiftId is required");

        ShiftModel model = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        requireBelongsToCompany(model, companyId);

        shiftRepository.delete(model);
    }

    // =========================
    // Listados
    // =========================

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSummaryDto> listAll(Long companyId, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        return shiftRepository.search(companyId, null, null, null, null, pageable)
                .map(shiftMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSummaryDto> listCurrent(Long companyId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        return shiftRepository.findByCompany_IdAndActiveTrueOrderByShiftNameAsc(companyId)
                .stream()
                .map(shiftMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSummaryDto> listByDate(Long companyId, LocalDate rosterDate) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (rosterDate == null) throw new IllegalArgumentException("rosterDate is required");

        return shiftRepository.findByCompany_IdAndRosterDateOrderByShiftNameAsc(companyId, rosterDate)
                .stream()
                .map(shiftMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSummaryDto> listByDate(Long companyId, LocalDate rosterDate, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (rosterDate == null) throw new IllegalArgumentException("rosterDate is required");

        return shiftRepository.findByCompany_IdAndRosterDateOrderByShiftNameAsc(companyId, rosterDate, pageable)
                .map(shiftMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSummaryDto> listByDateRange(Long companyId, LocalDate from, LocalDate to, Pageable pageable) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (from == null || to == null) throw new IllegalArgumentException("from and to are required");
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be >= from");

        return shiftRepository.findByCompany_IdAndRosterDateBetweenOrderByRosterDateDescShiftNameAsc(
                companyId, from, to, pageable
        ).map(shiftMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftSummaryDto> listByBatch(Long companyId, String batchId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (batchId == null || batchId.isBlank()) throw new IllegalArgumentException("batchId is required");

        return shiftRepository.findByCompany_IdAndBatchIdOrderByShiftNameAsc(companyId, batchId)
                .stream()
                .map(shiftMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDetailDto> listByDateDetail(Long companyId, LocalDate rosterDate) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (rosterDate == null) throw new IllegalArgumentException("rosterDate is required");

        return shiftRepository.findByCompany_IdAndRosterDateOrderByShiftNameAsc(companyId, rosterDate)
                .stream()
                .map(shiftMapper::toDetailDto)
                .toList();
    }

    // =========================
    // Search
    // =========================

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftSummaryDto> search(
            Long companyId,
            String q,
            Boolean active,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("to must be >= from");
        }

        String query = (q == null) ? null : q.trim();

        return shiftRepository.search(companyId, query, active, from, to, pageable)
                .map(shiftMapper::toSummaryDto);
    }

    // =========================
    // Excel: reemplazar current por batch
    // =========================

    @Override
    public List<ShiftDetailDto> replaceCurrentBatch(Long companyId, LocalDate rosterDate, List<CreateShiftRequest> shifts) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (rosterDate == null) throw new IllegalArgumentException("rosterDate is required");
        if (shifts == null || shifts.isEmpty()) throw new IllegalArgumentException("shifts list is required");

        CompanyModel company = requireCompany(companyId);

        String batchId = UUID.randomUUID().toString();

        // 1) desactivar current anterior
        shiftRepository.deactivateCurrent(companyId);

        // 2) construir entidades
        List<ShiftModel> entities = new ArrayList<>(shifts.size());

        for (CreateShiftRequest req : shifts) {
            if (req == null) continue;

            validateShiftName(req.getShiftName());

            ShiftModel model = shiftMapper.toEntity(req);
            model.setCompany(company);

            // fuerza rosterDate del batch (viene del Excel)
            model.setRosterDate(rosterDate);

            // batch único por import
            model.setBatchId(batchId);

            // current = true
            model.setActive(true);

            // normalizar listas
            model.setResponsibleDnis(normalizeDnis(req.getResponsibleDnis()));
            model.setVehiclePlates(normalizePlates(req.getVehiclePlates()));

            entities.add(model);
        }

        if (entities.isEmpty()) {
            throw new IllegalArgumentException("No valid shifts to import");
        }

        // 3) guardar todo
        List<ShiftModel> saved = shiftRepository.saveAll(entities);

        // (si quieres asegurar timestamps/ids antes de mapear en algunos providers)
        // shiftRepository.flush();

        return saved.stream().map(shiftMapper::toDetailDto).toList();
    }

    public void rebuildShiftExcelGroups(Long companyId, List<ShiftDetailDto> importedShifts) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");

        CompanyModel company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // 1) borrar grupos temporales anteriores
        groupRepository.deleteByCompany_IdAndSource(companyId, GroupSource.SHIFT_EXCEL);

        if (importedShifts == null || importedShifts.isEmpty()) return;

        // 2) juntar DNIs de todos los turnos (bulk)
        Set<String> allDnis = importedShifts.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> s.getResponsibleDnis() == null ? Stream.empty() : s.getResponsibleDnis().stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(d -> !d.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, UserModel> userByDni = userRepository.findUsersByCompanyAndDnis(companyId, allDnis).stream()
                .filter(u -> u.getDni() != null)
                .collect(Collectors.toMap(u -> u.getDni().trim(), u -> u, (a,b) -> a));

        // 3) crear grupos por turno + memberships
        Map<String, Integer> nameCounter = new HashMap<>();

        for (ShiftDetailDto shift : importedShifts) {
            if (shift == null) continue;

            String rawName = (shift.getShiftName() == null) ? "" : shift.getShiftName().trim();
            if (rawName.isBlank()) continue;

            // evitar colisión por nombres repetidos en el mismo Excel
            String name = uniqueName(rawName, nameCounter);

            NotificationGroupModel group = NotificationGroupModel.builder()
                    .company(company)
                    .name(name)
                    .description("Generado desde Excel de turnos")
                    .active(true)
                    .source(GroupSource.SHIFT_EXCEL)
                    .build();

            // memberships
            if (shift.getResponsibleDnis() != null) {
                for (String dniRaw : shift.getResponsibleDnis()) {
                    if (dniRaw == null) continue;
                    String dni = dniRaw.trim();
                    if (dni.isBlank()) continue;

                    UserModel user = userByDni.get(dni);
                    if (user == null) continue; // DNI no existe en usuarios -> lo ignoras o registras warning

                    group.getMemberships().add(GroupUserModel.builder()
                            .group(group)
                            .user(user)
                            .active(true)
                            .build());
                }
            }

            // si no quieres grupos vacíos:
            if (group.getMemberships().isEmpty()) continue;

            groupRepository.save(group);
        }
    }

    private String uniqueName(String base, Map<String, Integer> counter) {
        int n = counter.getOrDefault(base, 0) + 1;
        counter.put(base, n);
        return (n == 1) ? base : base + " (" + n + ")";
    }
}
