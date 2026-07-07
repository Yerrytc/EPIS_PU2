package com.upt.sistema.modules.objetos.service;

import com.upt.sistema.modules.objetos.model.Usuario;
import com.upt.sistema.modules.objetos.model.UsuarioLogin;
import com.upt.sistema.modules.objetos.repository.EntregaObjetoRepository;
import com.upt.sistema.modules.objetos.repository.HistorialObjetoRepository;
import com.upt.sistema.modules.objetos.repository.LogInicioSesionRepository;
import com.upt.sistema.modules.objetos.repository.UsuarioLoginRepository;
import com.upt.sistema.modules.objetos.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {
    private static final Pattern SOLO_TEXTO = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,80}$");
    private static final Pattern DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern TELEFONO = Pattern.compile("^9\\d{8}$");
    private static final Pattern CODIGO_ALUMNO = Pattern.compile("^\\d{10}$");
    private static final Pattern CORREO_INSTITUCIONAL = Pattern.compile("^[A-Za-z0-9._%+-]+@virtual\\.upt\\.pe$");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioLoginRepository usuarioLoginRepository;
    private final LogInicioSesionRepository logInicioSesionRepository;
    private final HistorialObjetoRepository historialObjetoRepository;
    private final EntregaObjetoRepository entregaObjetoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioLoginRepository usuarioLoginRepository,
                          LogInicioSesionRepository logInicioSesionRepository,
                          HistorialObjetoRepository historialObjetoRepository,
                          EntregaObjetoRepository entregaObjetoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioLoginRepository = usuarioLoginRepository;
        this.logInicioSesionRepository = logInicioSesionRepository;
        this.historialObjetoRepository = historialObjetoRepository;
        this.entregaObjetoRepository = entregaObjetoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllByOrderByApellidosAscNombreAsc();
    }

    public Optional<Usuario> buscarUsuario(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioLogin> buscarLoginPorUsername(String username) {
        return usuarioLoginRepository.findByUsernameIgnoreCase(username == null ? "" : username.trim().toLowerCase());
    }

    public Usuario usuarioActual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return usuarioLoginRepository.findByUsernameIgnoreCase(authentication.getName().trim().toLowerCase())
                .map(UsuarioLogin::getUsuario)
                .orElse(null);
    }

    @Transactional
    public Usuario guardarUsuarioConLogin(Usuario usuario, String username, String password) {
        prepararUsuario(usuario);
        String usernameNormalizado = prepararUsername(username, usuario.getEmail());
        validarUsuario(usuario, usernameNormalizado, password, null, null, true);

        Usuario guardado = usuarioRepository.save(usuario);
        UsuarioLogin login = new UsuarioLogin();
        login.setUsuario(guardado);
        login.setUsername(usernameNormalizado);
        login.setPasswordHash(passwordEncoder.encode(password.trim()));
        login.setActivo(true);
        usuarioLoginRepository.save(login);
        return guardado;
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario datos, String username, String password) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setNombre(datos.getNombre());
        usuario.setApellidos(datos.getApellidos());
        usuario.setTipoUsuario(datos.getTipoUsuario());
        usuario.setCodigoAlumno(datos.getCodigoAlumno());
        usuario.setDocumento(datos.getDocumento());
        usuario.setTelefono(datos.getTelefono());
        usuario.setEmail(datos.getEmail());

        prepararUsuario(usuario);
        UsuarioLogin login = usuario.getLogin();
        Long idLogin = login == null ? null : login.getIdLogin();
        String usernameNormalizado = prepararUsername(username, usuario.getEmail());
        validarUsuario(usuario, usernameNormalizado, password, id, idLogin, login == null || (password != null && !password.isBlank()));

        Usuario guardado = usuarioRepository.save(usuario);
        if (login != null) {
            login.setUsername(usernameNormalizado);
            if (password != null && !password.isBlank()) {
                login.setPasswordHash(passwordEncoder.encode(password.trim()));
            }
            usuarioLoginRepository.save(login);
        } else {
            UsuarioLogin nuevoLogin = new UsuarioLogin();
            nuevoLogin.setUsuario(guardado);
            nuevoLogin.setUsername(usernameNormalizado);
            nuevoLogin.setPasswordHash(passwordEncoder.encode((password == null || password.isBlank()) ? "123456" : password.trim()));
            nuevoLogin.setActivo(true);
            usuarioLoginRepository.save(nuevoLogin);
        }
        return guardado;
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        logInicioSesionRepository.eliminarPorUsuarioId(id);
        historialObjetoRepository.eliminarPorUsuarioId(id);
        entregaObjetoRepository.eliminarPorUsuarioId(id);
        if (usuario.getLogin() != null) {
            usuarioLoginRepository.delete(usuario.getLogin());
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void cambiarEstadoLogin(Long idLogin) {
        UsuarioLogin login = usuarioLoginRepository.findById(idLogin)
                .orElseThrow(() -> new IllegalArgumentException("Login no encontrado"));
        login.setActivo(!Boolean.TRUE.equals(login.getActivo()));
        usuarioLoginRepository.save(login);
    }

    public boolean existeUsername(String username) {
        return usuarioLoginRepository.existsByUsernameIgnoreCase(username == null ? "" : username.trim().toLowerCase());
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    private void prepararUsuario(Usuario usuario) {
        usuario.setNombre(normalizarTexto(usuario.getNombre()));
        usuario.setApellidos(normalizarTexto(usuario.getApellidos()));
        usuario.setTipoUsuario(normalizarRol(usuario.getTipoUsuario()));
        usuario.setDocumento(normalizarNullable(usuario.getDocumento()));
        usuario.setTelefono(normalizarNullable(usuario.getTelefono()));
        usuario.setEmail(normalizarCorreo(usuario.getEmail()));
        usuario.setCodigoAlumno(normalizarNullable(usuario.getCodigoAlumno()));
        if (!"ALUMNO".equals(usuario.getTipoUsuario())) {
            usuario.setCodigoAlumno(null);
        }
    }

    private void validarUsuario(Usuario usuario, String username, String password, Long idUsuarioActual, Long idLoginActual, boolean passwordObligatoria) {
        if (usuario.getNombre() == null || !SOLO_TEXTO.matcher(usuario.getNombre()).matches()) {
            throw new IllegalArgumentException("Los nombres solo deben contener letras y espacios, minimo 2 caracteres.");
        }
        if (usuario.getApellidos() == null || !SOLO_TEXTO.matcher(usuario.getApellidos()).matches()) {
            throw new IllegalArgumentException("Los apellidos solo deben contener letras y espacios, minimo 2 caracteres.");
        }
        if (!List.of("ADMIN", "TUTOR", "DOCENTE", "ALUMNO").contains(usuario.getTipoUsuario())) {
            throw new IllegalArgumentException("Debe seleccionar un rol valido.");
        }
        if (usuario.getDocumento() == null || !DNI.matcher(usuario.getDocumento()).matches()) {
            throw new IllegalArgumentException("El DNI/documento debe tener exactamente 8 digitos numericos.");
        }
        if (existeDocumento(usuario.getDocumento(), idUsuarioActual)) {
            throw new IllegalArgumentException("El DNI/documento ya esta registrado en otro usuario.");
        }
        if (usuario.getTelefono() == null || !TELEFONO.matcher(usuario.getTelefono()).matches()) {
            throw new IllegalArgumentException("El telefono debe tener exactamente 9 digitos y empezar con 9.");
        }
        if (existeTelefono(usuario.getTelefono(), idUsuarioActual)) {
            throw new IllegalArgumentException("El telefono ya esta registrado en otro usuario.");
        }
        if (usuario.getEmail() == null || !CORREO_INSTITUCIONAL.matcher(usuario.getEmail()).matches()) {
            throw new IllegalArgumentException("El email institucional debe terminar en @virtual.upt.pe.");
        }
        if (existeEmail(usuario.getEmail(), idUsuarioActual)) {
            throw new IllegalArgumentException("El email institucional ya esta registrado en otro usuario.");
        }
        if (username == null || !CORREO_INSTITUCIONAL.matcher(username).matches()) {
            throw new IllegalArgumentException("El correo de acceso debe terminar en @virtual.upt.pe.");
        }
        if (existeUsername(username, idLoginActual)) {
            throw new IllegalArgumentException("El correo de acceso ya esta registrado en otro usuario.");
        }
        if ("ALUMNO".equals(usuario.getTipoUsuario())) {
            if (usuario.getCodigoAlumno() == null || !CODIGO_ALUMNO.matcher(usuario.getCodigoAlumno()).matches()) {
                throw new IllegalArgumentException("El codigo de alumno debe tener exactamente 10 digitos numericos.");
            }
            if (existeCodigoAlumno(usuario.getCodigoAlumno(), idUsuarioActual)) {
                throw new IllegalArgumentException("El codigo de alumno ya esta registrado en otro usuario.");
            }
        }
        if (passwordObligatoria && (password == null || password.isBlank())) {
            throw new IllegalArgumentException("Debe ingresar una contrasena.");
        }
        if (password != null && !password.isBlank() && password.trim().length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres.");
        }
    }

    private boolean existeDocumento(String documento, Long idUsuarioActual) {
        return idUsuarioActual == null ? usuarioRepository.existsByDocumento(documento) : usuarioRepository.existsByDocumentoAndIdUsuarioNot(documento, idUsuarioActual);
    }

    private boolean existeTelefono(String telefono, Long idUsuarioActual) {
        return idUsuarioActual == null ? usuarioRepository.existsByTelefono(telefono) : usuarioRepository.existsByTelefonoAndIdUsuarioNot(telefono, idUsuarioActual);
    }

    private boolean existeEmail(String email, Long idUsuarioActual) {
        return idUsuarioActual == null ? usuarioRepository.existsByEmailIgnoreCase(email) : usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(email, idUsuarioActual);
    }

    private boolean existeCodigoAlumno(String codigoAlumno, Long idUsuarioActual) {
        return idUsuarioActual == null ? usuarioRepository.existsByCodigoAlumno(codigoAlumno) : usuarioRepository.existsByCodigoAlumnoAndIdUsuarioNot(codigoAlumno, idUsuarioActual);
    }

    private boolean existeUsername(String username, Long idLoginActual) {
        return idLoginActual == null ? usuarioLoginRepository.existsByUsernameIgnoreCase(username) : usuarioLoginRepository.existsByUsernameIgnoreCaseAndIdLoginNot(username, idLoginActual);
    }

    private String prepararUsername(String username, String email) {
        String value = (username == null || username.isBlank()) ? email : username;
        return normalizarCorreo(value);
    }

    private String normalizarRol(String rol) {
        if (rol == null) return "ALUMNO";
        return rol.trim().toUpperCase();
    }

    private String normalizarTexto(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizarNullable(String value) {
        if (value == null) return null;
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    private String normalizarCorreo(String value) {
        if (value == null) return null;
        String clean = value.trim().toLowerCase();
        return clean.isEmpty() ? null : clean;
    }
}
