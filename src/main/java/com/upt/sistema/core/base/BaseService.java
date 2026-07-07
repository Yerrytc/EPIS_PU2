package com.upt.sistema.core.base;

import com.upt.sistema.core.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<T extends BaseEntity> {

    protected abstract BaseRepository<T> getRepository();

    public List<T> listarTodos() {
        return getRepository().findAll();
    }

    public Optional<T> buscarPorId(Long id) {
        return getRepository().findById(id);
    }

    public T obtenerPorId(Long id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + id));
    }

    public T guardar(T entity) {
        return getRepository().save(entity);
    }

    public void eliminar(Long id) {
        T entity = obtenerPorId(id);
        entity.setActivo(false);
        getRepository().save(entity);
    }
}
