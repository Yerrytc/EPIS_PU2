package com.upt.sistema.modules.sista.service;

import com.upt.sistema.core.base.BaseRepository;
import com.upt.sistema.core.base.BaseService;
import com.upt.sistema.modules.sista.entity.SistaRegistro;
import com.upt.sistema.modules.sista.repository.SistaRegistroRepository;
import com.upt.sistema.shared.validation.AppValidation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SistaRegistroService extends BaseService<SistaRegistro> {

    private final SistaRegistroRepository repository;

    public SistaRegistroService(SistaRegistroRepository repository) {
        this.repository = repository;
    }

    @Override
    protected BaseRepository<SistaRegistro> getRepository() {
        return repository;
    }

    @Override
    public SistaRegistro guardar(SistaRegistro registro) {
        registro.setNombre(AppValidation.requiredText("Nombre del registro", registro.getNombre(), 5, 255));
        registro.setDescripcion(AppValidation.optionalText("Descripcion", registro.getDescripcion(), 1000));
        if (registro.getFecha() != null && registro.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del registro no puede ser futura.");
        }
        registro.setEstado(AppValidation.oneOf("estado", registro.getEstado(), "ACTIVO", "EN_REVISION", "EN_DESARROLLO", "FINALIZADO", "INACTIVO"));
        return repository.save(registro);
    }

    @Override
    public List<SistaRegistro> listarTodos() {
        return repository.findAll();
    }
}
