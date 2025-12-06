package com.icm.alert_api.repositories;

import com.icm.alert_api.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    // Buscar un usuario asegurando que pertenece a un grupo concreto
    Optional<UserModel> findByIdAndNotificationGroup_Id(Long id, Long groupId);

    // Listar usuarios de un grupo (sin filtro de texto)
    Page<UserModel> findByNotificationGroup_Id(Long groupId, Pageable pageable);

    // Búsqueda por texto dentro de un grupo (fullName / username / dni)
    @Query("""
           SELECT u
           FROM UserModel u
           WHERE u.notificationGroup.id = :groupId
             AND (
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.dni)      LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<UserModel> searchInGroup(@Param("groupId") Long groupId,
                                  @Param("q") String q,
                                  Pageable pageable);

    // Para KPIs de grupos si luego lo quieres usar
    long countByNotificationGroup_Id(Long groupId);
}
