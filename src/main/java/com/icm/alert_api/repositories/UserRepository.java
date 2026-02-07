package com.icm.alert_api.repositories;

import com.icm.alert_api.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByUsername(String username);
    //Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByDni(String dni);
    List<UserModel> findByCompany_IdAndDniIn(Long companyId, Collection<String> dnis);
    @Query("""
select u from UserModel u
where u.company.id = :companyId
and u.dni in :dnis
""")
    List<UserModel> findUsersByCompanyAndDnis(@Param("companyId") Long companyId, @Param("dnis") Collection<String> dnis);


    Optional<UserModel> findByCompanyIdAndUsernameIgnoreCase(Long companyId, String username);

    Optional<UserModel> findByCompanyIdAndDni(Long companyId, String dni);

    // Por seguridad, siempre verifica que pertenezca a la empresa
    Optional<UserModel> findByIdAndCompanyId(Long id, Long companyId);

    // Listar usuarios de una empresa
    Page<UserModel> findByCompanyId(Long companyId, Pageable pageable);

    // Búsqueda por texto dentro de una empresa (fullName / username / dni)
    @Query("""
           SELECT u
           FROM UserModel u
           WHERE u.company.id = :companyId
             AND (
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.dni)      LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<UserModel> searchInCompany(@Param("companyId") Long companyId,
                                    @Param("q") String q,
                                    Pageable pageable);

    long countByCompanyId(Long companyId);
}
