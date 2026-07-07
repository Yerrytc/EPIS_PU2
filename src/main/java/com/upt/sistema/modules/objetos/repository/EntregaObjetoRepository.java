package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.EntregaObjeto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EntregaObjetoRepository extends JpaRepository<EntregaObjeto, Long> {
    @EntityGraph(attributePaths = {"objeto", "usuarioEntrego"})
    List<EntregaObjeto> findAllByOrderByFechaEntregaDesc();
    boolean existsByObjetoIdObjeto(Long idObjeto);
    Optional<EntregaObjeto> findByObjetoIdObjeto(Long idObjeto);
    @Modifying @Query("DELETE FROM EntregaObjeto e WHERE e.usuarioEntrego.idUsuario = :id")
    void eliminarPorUsuarioId(@Param("id") Long id);
}
