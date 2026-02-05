package com.icm.alert_api.services;

import com.icm.alert_api.dto.shift.CreateShiftRequest;
import com.icm.alert_api.dto.shift.ShiftDetailDto;
import com.icm.alert_api.dto.shift.ShiftSummaryDto;
import com.icm.alert_api.dto.shift.UpdateShiftRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftService {

    // CRUD
    ShiftDetailDto create(CreateShiftRequest request);

    ShiftDetailDto update(Long companyId, Long shiftId, UpdateShiftRequest request);

    Optional<ShiftDetailDto> findById(Long companyId, Long shiftId);

    void deleteById(Long companyId, Long shiftId);

    // Listados
    Page<ShiftSummaryDto> listAll(Long companyId, Pageable pageable);

    List<ShiftSummaryDto> listCurrent(Long companyId);

    List<ShiftSummaryDto> listByDate(Long companyId, LocalDate rosterDate);

    Page<ShiftSummaryDto> listByDate(Long companyId, LocalDate rosterDate, Pageable pageable);

    Page<ShiftSummaryDto> listByDateRange(Long companyId, LocalDate from, LocalDate to, Pageable pageable);

    List<ShiftSummaryDto> listByBatch(Long companyId, String batchId);

    // Search (q + filtros)
    Page<ShiftSummaryDto> search(
            Long companyId,
            String q,
            Boolean active,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    /**
     * Caso Excel:
     * - Desactiva el "current" anterior (active=true) de la empresa
     * - Crea un nuevo batch (UUID) y guarda todos los turnos como active=true
     * - Retorna el detalle de lo importado (ya con ids)
     */
    List<ShiftDetailDto> replaceCurrentBatch(Long companyId, LocalDate rosterDate, List<CreateShiftRequest> shifts);
}
