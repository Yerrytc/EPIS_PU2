package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByApellidosAscNombreAsc();
    long countByTipoUsuario(String tipoUsuario);

    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdUsuarioNot(String documento, Long idUsuario);

    boolean existsByTelefono(String telefono);
    boolean existsByTelefonoAndIdUsuarioNot(String telefono, Long idUsuario);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Long idUsuario);

    boolean existsByCodigoAlumno(String codigoAlumno);
    boolean existsByCodigoAlumnoAndIdUsuarioNot(String codigoAlumno, Long idUsuario);
}
