package com.upt.sistema.modules.admin.service;

import com.upt.sistema.modules.objetos.model.AmbienteGeneral;
import com.upt.sistema.modules.objetos.model.Aula;
import com.upt.sistema.modules.objetos.model.ClaseObjeto;
import com.upt.sistema.modules.objetos.model.Escuela;
import com.upt.sistema.modules.objetos.model.Laboratorio;
import com.upt.sistema.modules.objetos.repository.AmbienteGeneralRepository;
import com.upt.sistema.modules.objetos.repository.AulaRepository;
import com.upt.sistema.modules.objetos.repository.ClaseObjetoRepository;
import com.upt.sistema.modules.objetos.repository.EscuelaRepository;
import com.upt.sistema.modules.objetos.repository.LaboratorioRepository;
import com.upt.sistema.shared.validation.AppValidation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogoService {
    private final EscuelaRepository escuelaRepository;
    private final AulaRepository aulaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final AmbienteGeneralRepository ambienteGeneralRepository;
    private final ClaseObjetoRepository claseObjetoRepository;

    public AdminCatalogoService(EscuelaRepository escuelaRepository,
                                AulaRepository aulaRepository,
                                LaboratorioRepository laboratorioRepository,
                                AmbienteGeneralRepository ambienteGeneralRepository,
                                ClaseObjetoRepository claseObjetoRepository) {
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

    @Transactional
    public ClaseObjeto guardarClase(String nombre) {
        String nombreOk = AppValidation.requiredText("Clase de objeto", nombre, 3, 50);
        if (claseObjetoRepository.existsByNombreClaseIgnoreCase(nombreOk)) {
            throw new IllegalArgumentException("La clase de objeto ya existe.");
        }
        ClaseObjeto clase = new ClaseObjeto();
        clase.setNombreClase(nombreOk);
        return claseObjetoRepository.save(clase);
    }

    @Transactional
    public Aula guardarAula(Long idEscuela, String codigo, String descripcion) {
        Escuela escuela = escuelaRepository.findById(AppValidation.positiveId("escuela", idEscuela))
                .orElseThrow(() -> new IllegalArgumentException("Escuela no encontrada."));
        String codigoOk = AppValidation.requiredCode("Codigo de aula", codigo);
        String descripcionOk = AppValidation.optionalText("Descripcion del aula", descripcion, 150);
        if (aulaRepository.existsByCodigoAulaIgnoreCase(codigoOk)) {
            throw new IllegalArgumentException("El codigo de aula ya existe.");
        }
        Aula aula = new Aula();
        aula.setEscuela(escuela);
        aula.setCodigoAula(codigoOk);
        aula.setDescripcion(descripcionOk);
        return aulaRepository.save(aula);
    }

    @Transactional
    public Laboratorio guardarLaboratorio(Long idEscuela, String codigo, String descripcion) {
        Escuela escuela = escuelaRepository.findById(AppValidation.positiveId("escuela", idEscuela))
                .orElseThrow(() -> new IllegalArgumentException("Escuela no encontrada."));
        String codigoOk = AppValidation.requiredCode("Codigo de laboratorio", codigo);
        String descripcionOk = AppValidation.optionalText("Descripcion del laboratorio", descripcion, 150);
        if (laboratorioRepository.existsByCodigoLaboratorioIgnoreCase(codigoOk)) {
            throw new IllegalArgumentException("El codigo de laboratorio ya existe.");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setEscuela(escuela);
        laboratorio.setCodigoLaboratorio(codigoOk);
        laboratorio.setDescripcion(descripcionOk);
        return laboratorioRepository.save(laboratorio);
    }

    @Transactional
    public AmbienteGeneral guardarAmbiente(String nombre, String tipo) {
        String nombreOk = AppValidation.requiredText("Nombre del ambiente", nombre, 3, 100);
        String tipoOk = AppValidation.oneOf("tipo de ambiente", tipo, "Cancha", "Auditorio", "Biblioteca", "Sala", "Patio", "Administracion", "Otro");
        if (ambienteGeneralRepository.existsByNombreAmbienteIgnoreCaseAndTipoAmbienteIgnoreCase(nombreOk, tipoOk)) {
            throw new IllegalArgumentException("El ambiente ya existe con ese tipo.");
        }
        AmbienteGeneral ambiente = new AmbienteGeneral();
        ambiente.setNombreAmbiente(nombreOk);
        ambiente.setTipoAmbiente(tipoOk);
        return ambienteGeneralRepository.save(ambiente);
    }
}
