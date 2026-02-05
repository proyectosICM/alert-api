package com.icm.alert_api.repositories;

import com.icm.alert_api.models.ShiftModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<ShiftModel, Long>, JpaSpecificationExecutor<ShiftModel> {

    // =========================
    // "CURRENT" (último Excel)
    // =========================

    List<ShiftModel> findByCompany_IdAndActiveTrueOrderByShiftNameAsc(Long companyId);

    Page<ShiftModel> findByCompany_IdAndActiveTrueOrderByRosterDateDescShiftNameAsc(
            Long companyId,
            Pageable pageable
    );

    Optional<ShiftModel> findByCompany_IdAndActiveTrueAndShiftNameIgnoreCase(
            Long companyId,
            String shiftName
    );

    // =========================
    // FILTRO por DÍA
    // =========================

    List<ShiftModel> findByCompany_IdAndRosterDateOrderByShiftNameAsc(Long companyId, LocalDate rosterDate);

    Page<ShiftModel> findByCompany_IdAndRosterDateOrderByShiftNameAsc(
            Long companyId,
            LocalDate rosterDate,
            Pageable pageable
    );

    Optional<ShiftModel> findByCompany_IdAndRosterDateAndShiftNameIgnoreCase(
            Long companyId,
            LocalDate rosterDate,
            String shiftName
    );

    // =========================
    // FILTRO por RANGO de FECHAS (historial)
    // =========================

    Page<ShiftModel> findByCompany_IdAndRosterDateBetweenOrderByRosterDateDescShiftNameAsc(
            Long companyId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    // =========================
    // FILTRO por BATCH (una importación)
    // =========================

    List<ShiftModel> findByCompany_IdAndBatchIdOrderByShiftNameAsc(Long companyId, String batchId);

    Page<ShiftModel> findByCompany_IdAndBatchIdOrderByShiftNameAsc(
            Long companyId,
            String batchId,
            Pageable pageable
    );

    // =========================
    // ACCIÓN: desactivar el "current" (para nueva importación)
    // =========================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ShiftModel s set s.active=false where s.company.id=:companyId and s.active=true")
    int deactivateCurrent(@Param("companyId") Long companyId);

    // =========================
    // ACCIÓN: desactivar por batch (por si quieres rollback lógico)
    // =========================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ShiftModel s set s.active=false where s.company.id=:companyId and s.batchId=:batchId")
    int deactivateBatch(@Param("companyId") Long companyId, @Param("batchId") String batchId);

    // =========================
    // Conteos útiles
    // =========================

    long countByCompany_IdAndActiveTrue(Long companyId);

    long countByCompany_IdAndRosterDate(Long companyId, LocalDate rosterDate);

    long countByCompany_IdAndRosterDateBetween(Long companyId, LocalDate from, LocalDate to);

    // =========================
    // Búsqueda simple por texto (shiftName) (tipo "q")
    // =========================

    @Query("""
        select s from ShiftModel s
        where s.company.id = :companyId
          and (:active is null or s.active = :active)
          and (:from is null or s.rosterDate >= :from)
          and (:to is null or s.rosterDate <= :to)
          and (:q is null or :q = '' or lower(s.shiftName) like lower(concat('%', :q, '%')))
        order by s.rosterDate desc, s.shiftName asc
    """)
    Page<ShiftModel> search(
            @Param("companyId") Long companyId,
            @Param("q") String q,
            @Param("active") Boolean active,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    // =========================
    // Nota sobre filtrar por DNI/placa:
    // - Como están guardados en JSON (LONGTEXT), Spring Data no puede filtrar bien aquí.
    // - Si quieres filtrar por "contiene DNI", lo ideal es hacerlo en servicio (post-filter),
    //   o migrar a tablas normales, o a MySQL JSON con JSON_CONTAINS y query nativa.
    // =========================
}
