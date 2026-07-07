package com.upt.sistema.modules.sista.repository;

import com.upt.sistema.core.base.BaseRepository;
import com.upt.sistema.modules.sista.entity.SistaRegistro;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SistaRegistroRepository extends BaseRepository<SistaRegistro> {
    List<SistaRegistro> findByEstado(String estado);
}
