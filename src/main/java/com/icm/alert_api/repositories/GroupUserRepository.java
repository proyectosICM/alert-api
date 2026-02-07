package com.icm.alert_api.repositories;

import com.icm.alert_api.models.GroupUserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUserModel, Long> {

    // Para KPIs
    long countByGroup_IdAndActiveTrue(Long groupId);

    // Para push notifications: miembros activos de varios grupos
    List<GroupUserModel> findByGroup_IdInAndActiveTrue(Collection<Long> groupIds);
    Page<GroupUserModel> findByGroup_IdAndActiveTrue(Long groupId, Pageable pageable);

    // Para ver los grupos de un usuario
    List<GroupUserModel> findByUser_IdAndActiveTrue(Long userId);

    // Para asegurar pertenencia user-grupo
    Optional<GroupUserModel> findByGroup_IdAndUser_Id(Long groupId, Long userId);

    // Listado simple de miembros de un grupo
    Page<GroupUserModel> findByGroup_Id(Long groupId, Pageable pageable);

    // Búsqueda por texto dentro de un grupo (fullName / username / dni)
    @Query("""
           SELECT gu
           FROM GroupUserModel gu
           JOIN gu.user u
           WHERE gu.group.id = :groupId
             AND gu.active = true
             AND (
                 LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(u.dni)      LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<GroupUserModel> searchMembersInGroup(@Param("groupId") Long groupId,
                                              @Param("q") String q,
                                              Pageable pageable);

    long deleteByGroup_Id(Long groupId);

    long deleteByGroup_IdIn(Collection<Long> groupIds);

}
