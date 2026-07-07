package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.Escuela;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EscuelaRepository extends JpaRepository<Escuela, Long> {
    List<Escuela> findAllByOrderByNombreEscuelaAsc();
}
