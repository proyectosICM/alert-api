package com.icm.alert_api.services;

import com.icm.alert_api.dto.user.CreateUserRequest;
import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import com.icm.alert_api.dto.user.UpdateGroupUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    // Crear usuario nuevo y asociarlo a un grupo
    GroupUserDetailDto create(Long groupId, CreateUserRequest request);

    // Actualizar datos del usuario (en el contexto del grupo)
    GroupUserDetailDto update(Long groupId, Long userId, UpdateGroupUserRequest request);

    // Eliminar la membresía del usuario en un grupo (y opcionalmente borrar el usuario si quieres)
    void deleteById(Long groupId, Long userId);

    // Buscar usuario globalmente por id (independiente del grupo)
    Optional<GroupUserDetailDto> findById(Long userId);

    // Buscar usuario asegurando que pertenece al grupo
    Optional<GroupUserDetailDto> findById(Long groupId, Long userId);

    // Para UI / admins (no es la usada por Spring Security, que puede ir directo a UserRepository)
    Optional<GroupUserDetailDto> findByUsername(String username);

    // Listar / buscar miembros de un grupo
    Page<GroupUserSummaryDto> search(Long groupId, String q, Pageable pageable);
}
