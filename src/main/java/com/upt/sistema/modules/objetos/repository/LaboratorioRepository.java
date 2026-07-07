package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {
    List<Laboratorio> findAllByOrderByCodigoLaboratorioAsc();
    boolean existsByCodigoLaboratorioIgnoreCase(String codigoLaboratorio);
}
