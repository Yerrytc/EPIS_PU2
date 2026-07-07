package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.HistorialObjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HistorialObjetoRepository extends JpaRepository<HistorialObjeto, Long> {
    List<HistorialObjeto> findAllByOrderByFechaDesc();
    List<HistorialObjeto> findByObjetoIdObjetoOrderByFechaDesc(Long idObjeto);
    @Modifying @Query("DELETE FROM HistorialObjeto h WHERE h.usuario.idUsuario = :id")
    void eliminarPorUsuarioId(@Param("id") Long id);
}
