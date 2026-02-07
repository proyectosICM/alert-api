package com.icm.alert_api.repositories;

import com.icm.alert_api.enums.GroupSource;
import com.icm.alert_api.models.NotificationGroupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroupModel, Long> {

    /**
     * Listar grupos de una empresa sin filtro.
     */
    Page<NotificationGroupModel> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Búsqueda por nombre/description dentro de una empresa.
     */
    @Query("""
           SELECT g
           FROM NotificationGroupModel g
           WHERE g.company.id = :companyId
             AND (
                 LOWER(g.name)        LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(g.description) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<NotificationGroupModel> searchByCompanyAndText(
            @Param("companyId") Long companyId,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * Grupos de una empresa que tienen asignado cierto código de montacargas.
     * Usado para saber qué grupos deben recibir las alertas de un vehículo.
     */
    @Query("""
           SELECT g
           FROM NotificationGroupModel g
           WHERE g.company.id = :companyId
             AND :vehicleCode MEMBER OF g.vehicleCodes
           """)
    List<NotificationGroupModel> findByCompanyAndVehicleCodeAssigned(
            @Param("companyId") Long companyId,
            @Param("vehicleCode") String vehicleCode
    );

    long countByCompany_Id(Long companyId);

    @Query("""
        select g
        from NotificationGroupModel g
        join g.vehicleCodes v
        where v = :vehicleCode
    """)
    java.util.List<NotificationGroupModel> findByVehicleCode(String vehicleCode);

    List<NotificationGroupModel> findByCompany_IdAndSource(Long companyId, GroupSource source);

    long deleteByCompany_IdAndSource(Long companyId, GroupSource source);

    Optional<NotificationGroupModel> findByCompany_IdAndName(Long companyId, String name);

    long deleteByCompany_IdAndName(Long companyId, String name);
}
