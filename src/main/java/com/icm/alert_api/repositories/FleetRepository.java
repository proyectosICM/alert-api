package com.icm.alert_api.repositories;

import com.icm.alert_api.models.FleetModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FleetRepository extends JpaRepository<FleetModel, Long> {

    // Listado por empresa
    Page<FleetModel> findByCompany_Id(Long companyId, Pageable pageable);

    // Búsqueda por texto (name/description) dentro de una empresa
    @Query("""
           select f
           from FleetModel f
           where f.company.id = :companyId
             and (
                  lower(f.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(f.description, '')) like lower(concat('%', :q, '%'))
             )
           """)
    Page<FleetModel> searchByCompanyAndText(Long companyId, String q, Pageable pageable);

    // Para validar ownership empresa+flota
    Optional<FleetModel> findByIdAndCompany_Id(Long fleetId, Long companyId);

    // Contar flotas por empresa (si lo quieres tipo CompanyService)
    long countByCompany_Id(Long companyId);

    /**
     * Traer SOLO vehicleCodes de una flota (evita cargar toda la entidad).
     * Útil para filtros de alertas por flota.
     *
     * Nota: Esto usa la tabla del ElementCollection: fleet_vehicle_codes
     */
    @Query("""
           select vc
           from FleetModel f
           join f.vehicleCodes vc
           where f.id = :fleetId
             and f.company.id = :companyId
           """)
    List<String> findVehicleCodes(Long companyId, Long fleetId);

    /**
     * Traer vehicleCodes de múltiples flotas (por si luego permites filtros multi-flota).
     */
    @Query("""
           select distinct vc
           from FleetModel f
           join f.vehicleCodes vc
           where f.company.id = :companyId
             and f.id in :fleetIds
           """)
    List<String> findVehicleCodesByFleetIds(Long companyId, Collection<Long> fleetIds);
}
