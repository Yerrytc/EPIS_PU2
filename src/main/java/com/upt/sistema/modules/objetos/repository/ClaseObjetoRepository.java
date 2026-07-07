package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.ClaseObjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaseObjetoRepository extends JpaRepository<ClaseObjeto, Long> {
    List<ClaseObjeto> findAllByOrderByNombreClaseAsc();
    boolean existsByNombreClaseIgnoreCase(String nombreClase);
}
