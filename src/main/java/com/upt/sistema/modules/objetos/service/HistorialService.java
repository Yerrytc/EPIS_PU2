package com.upt.sistema.modules.objetos.service;

import com.upt.sistema.modules.objetos.model.HistorialObjeto;
import com.upt.sistema.modules.objetos.model.Objeto;
import com.upt.sistema.modules.objetos.model.Usuario;
import com.upt.sistema.modules.objetos.repository.HistorialObjetoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HistorialService {
    private final HistorialObjetoRepository historialObjetoRepository;

    public HistorialService(HistorialObjetoRepository historialObjetoRepository) {
        this.historialObjetoRepository = historialObjetoRepository;
    }

    public List<HistorialObjeto> listarTodo() {
        return historialObjetoRepository.findAllByOrderByFechaDesc();
    }

    public List<HistorialObjeto> listarPorObjeto(Long idObjeto) {
        return historialObjetoRepository.findByObjetoIdObjetoOrderByFechaDesc(idObjeto);
    }

    public HistorialObjeto registrar(Objeto objeto, Usuario usuario, String accion) {
        HistorialObjeto historial = new HistorialObjeto();
        historial.setObjeto(objeto);
        historial.setUsuario(usuario);
        historial.setAccion(accion);
        return historialObjetoRepository.save(historial);
    }
}
