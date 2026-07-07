package com.upt.sistema.modules.objetos.service;

import com.upt.sistema.shared.validation.AppValidation;
import com.upt.sistema.modules.objetos.model.*;
import com.upt.sistema.modules.objetos.repository.EntregaObjetoRepository;
import com.upt.sistema.modules.objetos.repository.ObjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ObjetoService {
    private static final Set<String> ESTADOS_VALIDOS = Set.of("No entregado", "Entregado");

    private final ObjetoRepository objetoRepository;
    private final EntregaObjetoRepository entregaObjetoRepository;
    private final CatalogoService catalogoService;
    private final HistorialService historialService;

    public ObjetoService(ObjetoRepository objetoRepository, EntregaObjetoRepository entregaObjetoRepository,
                         CatalogoService catalogoService, HistorialService historialService) {
        this.objetoRepository = objetoRepository;
        this.entregaObjetoRepository = entregaObjetoRepository;
        this.catalogoService = catalogoService;
        this.historialService = historialService;
    }

    public List<Objeto> listar(String buscar, String estado) {
        String filtro = (buscar == null || buscar.isBlank()) ? null : buscar.trim();
        String est = (estado == null || estado.isBlank()) ? null : estado;
        return objetoRepository.filtrar(filtro, est);
    }

    public List<Objeto> listarTodos() { return objetoRepository.findAllByOrderByIdObjetoDesc(); }
    public List<Objeto> listarNoEntregados() { return objetoRepository.findByEstadoOrderByIdObjetoDesc("No entregado"); }

    public Objeto buscar(Long id) {
        return objetoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado"));
    }

    public Objeto nuevoObjeto() {
        Objeto objeto = new Objeto();
        objeto.setCodigo(generarCodigo());
        objeto.setFechaEncontrado(LocalDate.now());
        objeto.setEstado("No entregado");
        return objeto;
    }

    @Transactional
    public Objeto registrar(Objeto objeto, String tipoUbicacion, Long idUbicacion, Long idClaseObjeto, Usuario usuario) {
        if (objeto.getCodigo() == null || objeto.getCodigo().isBlank()) objeto.setCodigo(generarCodigo());
        objeto.setCodigo(normalizarCodigo(objeto.getCodigo()));
        validarDatosObjeto(objeto, null);
        objeto.setEstado("No entregado");
        objeto.setClaseObjeto(catalogoService.buscarClase(idClaseObjeto));
        asignarUbicacion(objeto, tipoUbicacion, idUbicacion);
        Objeto guardado = objetoRepository.save(objeto);
        historialService.registrar(guardado, usuario, "Registrado");
        return guardado;
    }

    @Transactional
    public Objeto actualizar(Long id, Objeto datos, String tipoUbicacion, Long idUbicacion, Long idClaseObjeto, Usuario usuario, boolean puedeCambiarEstado) {
        Objeto objeto = buscar(id);
        String estadoAnterior = objeto.getEstado();

        objeto.setCodigo(normalizarCodigo(datos.getCodigo()));
        objeto.setNombreObjeto(normalizarTexto(datos.getNombreObjeto()));
        objeto.setDescripcion(normalizarTexto(datos.getDescripcion()));
        objeto.setFechaEncontrado(datos.getFechaEncontrado());
        objeto.setPersonaEncontro(normalizarTexto(datos.getPersonaEncontro()));
        objeto.setClaseObjeto(catalogoService.buscarClase(idClaseObjeto));
        asignarUbicacion(objeto, tipoUbicacion, idUbicacion);

        if (puedeCambiarEstado && datos.getEstado() != null && !datos.getEstado().isBlank()) {
            objeto.setEstado(normalizarEstado(datos.getEstado()));
        } else {
            objeto.setEstado(estadoAnterior);
        }

        validarDatosObjeto(objeto, id);
        Objeto guardado = objetoRepository.save(objeto);
        historialService.registrar(guardado, usuario, "Modificado");
        if (!estadoAnterior.equalsIgnoreCase(guardado.getEstado())) {
            historialService.registrar(guardado, usuario, "Estado actualizado");
        }
        return guardado;
    }

    @Transactional
    public EntregaObjeto entregar(Long idObjeto, EntregaObjeto entrega, Usuario usuarioEntrego) {
        Objeto objeto = buscar(AppValidation.positiveId("objeto", idObjeto));
        if ("Entregado".equalsIgnoreCase(objeto.getEstado()) || entregaObjetoRepository.existsByObjetoIdObjeto(idObjeto)) {
            throw new IllegalStateException("El objeto ya fue entregado anteriormente.");
        }
        if (usuarioEntrego == null) {
            throw new IllegalArgumentException("No se pudo identificar al usuario que realiza la entrega.");
        }
        entrega.setTipoReceptor(AppValidation.oneOf("tipo de receptor", entrega.getTipoReceptor(), "Alumno", "Docente", "Personal", "Externo"));
        entrega.setNombreReceptor(AppValidation.requiredLetters("Nombre del receptor", entrega.getNombreReceptor(), 2, 100));
        entrega.setApellidosReceptor(AppValidation.requiredLetters("Apellidos del receptor", entrega.getApellidosReceptor(), 2, 100));
        entrega.setDocumentoReceptor(AppValidation.optionalDniOrRuc("Documento del receptor", entrega.getDocumentoReceptor()));
        if ("Alumno".equals(entrega.getTipoReceptor())) {
            entrega.setCodigoEstudiante(AppValidation.requiredStudentCode(entrega.getCodigoEstudiante()));
        } else {
            entrega.setCodigoEstudiante(null);
        }
        entrega.setTelefono(AppValidation.optionalPhone(entrega.getTelefono()));
        entrega.setEmail(AppValidation.optionalEmail("Email del receptor", entrega.getEmail()));
        entrega.setObjeto(objeto);
        entrega.setUsuarioEntrego(usuarioEntrego);
        EntregaObjeto guardada = entregaObjetoRepository.save(entrega);
        objeto.setEstado("Entregado");
        objetoRepository.save(objeto);
        historialService.registrar(objeto, usuarioEntrego, "Entregado");
        return guardada;
    }

    public long total() { return objetoRepository.count(); }
    public long totalEntregados() { return objetoRepository.countByEstado("Entregado"); }
    public long totalNoEntregados() { return objetoRepository.countByEstado("No entregado"); }

    public String generarCodigo() {
        long numero = objetoRepository.count() + 1;
        String codigo;
        do {
            codigo = String.format("%04d", numero++);
        } while (objetoRepository.existsByCodigo(codigo));
        return codigo;
    }

    private void validarDatosObjeto(Objeto objeto, Long idActual) {
        if (objeto.getCodigo() == null || !objeto.getCodigo().matches("^\\d{4,20}$")) {
            throw new IllegalArgumentException("El codigo del objeto debe ser numerico y tener minimo 4 digitos.");
        }
        objetoRepository.findByCodigo(objeto.getCodigo()).ifPresent(existente -> {
            if (idActual == null || !existente.getIdObjeto().equals(idActual)) {
                throw new IllegalArgumentException("El codigo del objeto ya existe.");
            }
        });
        objeto.setNombreObjeto(AppValidation.requiredText("Nombre del objeto", objeto.getNombreObjeto(), 3, 100));
        objeto.setDescripcion(AppValidation.requiredText("Descripcion", objeto.getDescripcion(), 5, 300));
        objeto.setPersonaEncontro(AppValidation.requiredLetters("Persona que encontro", objeto.getPersonaEncontro(), 3, 120));
        if (objeto.getFechaEncontrado() == null) {
            throw new IllegalArgumentException("Debe ingresar la fecha en que se encontro el objeto.");
        }
        if (objeto.getFechaEncontrado().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha encontrada no puede ser futura.");
        }
        if (!ESTADOS_VALIDOS.contains(objeto.getEstado())) {
            throw new IllegalArgumentException("Estado de objeto invalido.");
        }
    }

    private void asignarUbicacion(Objeto objeto, String tipoUbicacion, Long idUbicacion) {
        objeto.setAula(null);
        objeto.setLaboratorio(null);
        objeto.setAmbienteGeneral(null);
        if (idUbicacion == null) throw new IllegalArgumentException("Debe seleccionar una ubicacion.");
        if ("AULA".equalsIgnoreCase(tipoUbicacion)) objeto.setAula(catalogoService.buscarAula(idUbicacion));
        else if ("LABORATORIO".equalsIgnoreCase(tipoUbicacion)) objeto.setLaboratorio(catalogoService.buscarLaboratorio(idUbicacion));
        else if ("AMBIENTE".equalsIgnoreCase(tipoUbicacion)) objeto.setAmbienteGeneral(catalogoService.buscarAmbiente(idUbicacion));
        else throw new IllegalArgumentException("Tipo de ubicacion invalido.");
    }

    private String normalizarCodigo(String value) {
        if (value == null) return null;
        return value.trim();
    }

    private String normalizarTexto(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizarEstado(String value) {
        if ("Entregado".equalsIgnoreCase(value == null ? "" : value.trim())) return "Entregado";
        if ("No entregado".equalsIgnoreCase(value == null ? "" : value.trim())) return "No entregado";
        throw new IllegalArgumentException("Estado de objeto invalido.");
    }
}
