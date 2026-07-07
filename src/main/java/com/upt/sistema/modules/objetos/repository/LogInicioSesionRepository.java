package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.LogInicioSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogInicioSesionRepository extends JpaRepository<LogInicioSesion, Long> {
    List<LogInicioSesion> findAllByOrderByFechaIngresoDesc();
    @Modifying @Query("DELETE FROM LogInicioSesion l WHERE l.usuario.idUsuario = :id")
    void eliminarPorUsuarioId(@Param("id") Long id);
}
