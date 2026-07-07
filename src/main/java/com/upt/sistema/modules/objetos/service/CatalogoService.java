package com.upt.sistema.modules.objetos.service;

import com.upt.sistema.modules.objetos.model.*;
import com.upt.sistema.modules.objetos.repository.*;
import com.upt.sistema.shared.validation.AppValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoService {
    private final EscuelaRepository escuelaRepository;
    private final AulaRepository aulaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final AmbienteGeneralRepository ambienteGeneralRepository;
    private final ClaseObjetoRepository claseObjetoRepository;

    public CatalogoService(EscuelaRepository escuelaRepository,
                           AulaRepository aulaRepository, LaboratorioRepository laboratorioRepository,
                           AmbienteGeneralRepository ambienteGeneralRepository, ClaseObjetoRepository claseObjetoRepository) {
        this.escuelaRepository = escuelaRepository;
        this.aulaRepository = aulaRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.ambienteGeneralRepository = ambienteGeneralRepository;
        this.claseObjetoRepository = claseObjetoRepository;
    }

    public List<Escuela> listarEscuelas() {
        return escuelaRepository.findAllByOrderByNombreEscuelaAsc().stream()
                .filter(e -> e.getNombreEscuela() != null && e.getNombreEscuela().contains("Ingenieria de Sistemas"))
                .collect(Collectors.toList());
    }
    public List<Aula> listarAulas() { return aulaRepository.findAllByOrderByCodigoAulaAsc(); }
    public List<Laboratorio> listarLaboratorios() { return laboratorioRepository.findAllByOrderByCodigoLaboratorioAsc(); }
    public List<AmbienteGeneral> listarAmbientes() { return ambienteGeneralRepository.findAllByOrderByTipoAmbienteAscNombreAmbienteAsc(); }
    public List<ClaseObjeto> listarClases() { return claseObjetoRepository.findAllByOrderByNombreClaseAsc(); }

    public ClaseObjeto buscarClase(Long id) { return claseObjetoRepository.findById(AppValidation.positiveId("clase de objeto", id)).orElseThrow(() -> new IllegalArgumentException("Clase de objeto no encontrada.")); }
    public Aula buscarAula(Long id) { return aulaRepository.findById(AppValidation.positiveId("aula", id)).orElseThrow(() -> new IllegalArgumentException("Aula no encontrada.")); }
    public Laboratorio buscarLaboratorio(Long id) { return laboratorioRepository.findById(AppValidation.positiveId("laboratorio", id)).orElseThrow(() -> new IllegalArgumentException("Laboratorio no encontrado.")); }
    public AmbienteGeneral buscarAmbiente(Long id) { return ambienteGeneralRepository.findById(AppValidation.positiveId("ambiente", id)).orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado.")); }
    public Escuela buscarEscuela(Long id) { return escuelaRepository.findById(AppValidation.positiveId("escuela", id)).orElseThrow(() -> new IllegalArgumentException("Escuela no encontrada.")); }

    @Transactional
    public ClaseObjeto guardarClase(String nombre) {
        String nombreOk = AppValidation.requiredText("Clase de objeto", nombre, 3, 50);
        if (claseObjetoRepository.existsByNombreClaseIgnoreCase(nombreOk)) throw new IllegalArgumentException("La clase de objeto ya existe.");
        ClaseObjeto clase = new ClaseObjeto();
        clase.setNombreClase(nombreOk);
        return claseObjetoRepository.save(clase);
    }

    @Transactional
    public Aula guardarAula(Long idEscuela, String codigo, String descripcion) {
        Aula aula = new Aula();
        aula.setEscuela(buscarEscuela(idEscuela));
        aula.setCodigoAula(AppValidation.requiredCode("Codigo de aula", codigo));
        aula.setDescripcion(AppValidation.optionalText("Descripcion de aula", descripcion, 150));
        return aulaRepository.save(aula);
    }

    @Transactional
    public Laboratorio guardarLaboratorio(Long idEscuela, String codigo, String descripcion) {
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setEscuela(buscarEscuela(idEscuela));
        laboratorio.setCodigoLaboratorio(AppValidation.requiredCode("Codigo de laboratorio", codigo));
        laboratorio.setDescripcion(AppValidation.optionalText("Descripcion de laboratorio", descripcion, 150));
        return laboratorioRepository.save(laboratorio);
    }

    @Transactional
    public AmbienteGeneral guardarAmbiente(String nombre, String tipo) {
        AmbienteGeneral ambiente = new AmbienteGeneral();
        ambiente.setNombreAmbiente(AppValidation.requiredText("Nombre de ambiente", nombre, 3, 100));
        ambiente.setTipoAmbiente(AppValidation.oneOf("tipo de ambiente", tipo, "Cancha", "Auditorio", "Biblioteca", "Sala", "Patio", "Administracion", "Otro"));
        return ambienteGeneralRepository.save(ambiente);
    }
}
