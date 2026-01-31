package com.icm.alert_api.repositories;

import com.icm.alert_api.models.AlertRevisionPhotoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertRevisionPhotoRepository extends JpaRepository<AlertRevisionPhotoModel, Long> {

    // 1) Listar fotos (solo metadatos si tu DTO es summary; igual la entidad trae data)
    //    Si quieres evitar traer data, usa proyección o query que no seleccione data.
    List<AlertRevisionPhotoModel> findByRevision_IdOrderByCreatedAtAsc(Long revisionId);

    // 2) Obtener una foto y validar que pertenece a una revisión
    Optional<AlertRevisionPhotoModel> findByIdAndRevision_Id(Long id, Long revisionId);

    // 3) Contar fotos por revisión
    long countByRevision_Id(Long revisionId);

    // 4) Borrar todas las fotos de una revisión (útil si haces "replace all")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AlertRevisionPhotoModel p where p.revision.id = :revisionId")
    int deleteByRevisionId(@Param("revisionId") Long revisionId);

    // 5) Borrar una foto asegurando pertenencia
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AlertRevisionPhotoModel p where p.id = :id and p.revision.id = :revisionId")
    int deleteByIdAndRevisionId(@Param("id") Long id, @Param("revisionId") Long revisionId);
}
