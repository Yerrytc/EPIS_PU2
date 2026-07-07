package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {
    List<Aula> findAllByOrderByCodigoAulaAsc();
    boolean existsByCodigoAulaIgnoreCase(String codigoAula);
}
