package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.AmbienteGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AmbienteGeneralRepository extends JpaRepository<AmbienteGeneral, Long> {
    List<AmbienteGeneral> findAllByOrderByTipoAmbienteAscNombreAmbienteAsc();
    boolean existsByNombreAmbienteIgnoreCaseAndTipoAmbienteIgnoreCase(String nombreAmbiente, String tipoAmbiente);
}
