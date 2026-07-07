package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.UsuarioLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioLoginRepository extends JpaRepository<UsuarioLogin, Long> {
    Optional<UsuarioLogin> findByUsername(String username);
    Optional<UsuarioLogin> findByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdLoginNot(String username, Long idLogin);
}
