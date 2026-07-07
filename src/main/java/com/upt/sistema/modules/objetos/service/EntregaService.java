package com.upt.sistema.modules.objetos.service;

import com.upt.sistema.modules.objetos.model.EntregaObjeto;
import com.upt.sistema.modules.objetos.repository.EntregaObjetoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EntregaService {
    private final EntregaObjetoRepository entregaObjetoRepository;

    public EntregaService(EntregaObjetoRepository entregaObjetoRepository) {
        this.entregaObjetoRepository = entregaObjetoRepository;
    }

    public List<EntregaObjeto> listar() {
        return entregaObjetoRepository.findAllByOrderByFechaEntregaDesc();
    }
}
