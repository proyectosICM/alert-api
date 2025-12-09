package com.icm.alert_api.services;

import com.icm.alert_api.dto.user.GroupUserDetailDto;
import com.icm.alert_api.dto.user.GroupUserSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupUserService {

    // Asignar un usuario existente a un grupo
    GroupUserDetailDto addUserToGroup(Long groupId, Long userId);

    // Sacar a un usuario de un grupo (desactivar membresía)
    void removeUserFromGroup(Long groupId, Long userId);

    // Listar / buscar miembros de un grupo
    Page<GroupUserSummaryDto> listMembers(Long groupId, String q, Pageable pageable);
}
