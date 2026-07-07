package com.upt.sistema.modules.objetos.repository;

import com.upt.sistema.modules.objetos.model.Objeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ObjetoRepository extends JpaRepository<Objeto, Long> {
    boolean existsByCodigo(String codigo);
    Optional<Objeto> findByCodigo(String codigo);
    List<Objeto> findAllByOrderByIdObjetoDesc();
    List<Objeto> findByEstadoOrderByIdObjetoDesc(String estado);
    long countByEstado(String estado);

    @Query(value = """
        SELECT * FROM objetos o
        WHERE (:estado IS NULL OR o.estado = :estado)
          AND (:buscar IS NULL OR
               LOWER(o.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(o.nombre_objeto) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(o.descripcion) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(o.persona_encontro) LIKE LOWER(CONCAT('%', :buscar, '%')))
        ORDER BY o.id_objeto DESC
    """, nativeQuery = true)
    List<Objeto> filtrar(@Param("buscar") String buscar, @Param("estado") String estado);
}
