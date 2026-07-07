DROP DATABASE IF EXISTS bd_epis;
CREATE DATABASE bd_epis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bd_epis;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE sista_registros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha DATE NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE apoderado (
    id_apoderado INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    direccion VARCHAR(200),
    parentesco VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE estudiante (
    codigo VARCHAR(20) PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    id_apoderado INT,
    CONSTRAINT fk_estudiante_apoderado FOREIGN KEY (id_apoderado) REFERENCES apoderado(id_apoderado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE docente (
    id_docente INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    especialidad VARCHAR(120)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ciclo (
    id_ciclo INT AUTO_INCREMENT PRIMARY KEY,
    numero_ciclo INT NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE curso (
    id_curso INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ciclo_curso (
    id_ciclo_curso INT AUTO_INCREMENT PRIMARY KEY,
    id_ciclo INT NOT NULL,
    id_curso INT NOT NULL,
    CONSTRAINT fk_ciclo_curso_ciclo FOREIGN KEY (id_ciclo) REFERENCES ciclo(id_ciclo),
    CONSTRAINT fk_ciclo_curso_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT uk_ciclo_curso UNIQUE (id_ciclo, id_curso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE curso_docente (
    id_curso_docente INT AUTO_INCREMENT PRIMARY KEY,
    id_curso INT NOT NULL,
    id_docente INT NOT NULL,
    CONSTRAINT fk_curso_docente_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT fk_curso_docente_docente FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
    CONSTRAINT uk_curso_docente UNIQUE (id_curso, id_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE matricula_curso (
    id_matricula_curso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_curso_docente INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'CURSANDO',
    CONSTRAINT fk_matricula_estudiante FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_matricula_curso_docente FOREIGN KEY (id_curso_docente) REFERENCES curso_docente(id_curso_docente),
    CONSTRAINT uk_matricula UNIQUE (codigo_estudiante, id_curso_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE calificacion (
    id_calificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_matricula_curso INT NOT NULL UNIQUE,
    unidad1 DECIMAL(5,2) DEFAULT 0,
    unidad2 DECIMAL(5,2) DEFAULT 0,
    unidad3 DECIMAL(5,2) DEFAULT 0,
    promedio DECIMAL(5,2) DEFAULT 0,
    CONSTRAINT fk_calificacion_matricula FOREIGN KEY (id_matricula_curso) REFERENCES matricula_curso(id_matricula_curso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_calificacion (
    id_historial BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_curso INT NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    unidad1 DECIMAL(5,2) DEFAULT 0,
    unidad2 DECIMAL(5,2) DEFAULT 0,
    unidad3 DECIMAL(5,2) DEFAULT 0,
    promedio DECIMAL(5,2) DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'CURSANDO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_cal_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_hist_cal_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE matricula_curso ADD numero_matricula INT NOT NULL DEFAULT 1;

CREATE TABLE tutoria_estudiante_seguimiento (
    id_seguimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    tipo_seguimiento VARCHAR(60) NOT NULL,
    unidad_academica INT NOT NULL,
    estado VARCHAR(40) NOT NULL DEFAULT 'ABIERTO',
    observacion_positiva TEXT,
    advertencia_academica TEXT,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tut_seg_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_solicitudes (
    id_solicitud BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    descripcion TEXT NOT NULL,
    estado VARCHAR(40) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_nota (
    id_nota BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_curso INT NOT NULL,
    unidad_academica INT NOT NULL,
    nota DECIMAL(5,2) NOT NULL,
    observacion VARCHAR(255),
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tut_nota_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_tut_nota_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_citacion (
    id_citacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    fecha_citacion DATE NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(40) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_tut_cita_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_entrevista (
    id_entrevista BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    problemas_detectados TEXT NOT NULL,
    observaciones TEXT NOT NULL,
    recomendaciones TEXT NOT NULL,
    derivacion_area VARCHAR(80),
    CONSTRAINT fk_tut_ent_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_inasistencia (
    id_inasistencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_curso INT NOT NULL,
    id_docente INT NOT NULL,
    fecha DATE NOT NULL,
    motivo VARCHAR(255),
    CONSTRAINT fk_tut_falta_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_tut_falta_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT fk_tut_falta_doc FOREIGN KEY (id_docente) REFERENCES docente(id_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_justificacion (
    id_justificacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_curso INT NOT NULL,
    id_docente INT NOT NULL,
    fecha_reincorporacion DATE NOT NULL,
    fecha_presentacion DATE NOT NULL,
    dias_faltados INT NOT NULL,
    motivo TEXT NOT NULL,
    evidencia VARCHAR(255),
    estado VARCHAR(30) NOT NULL,
    estado_revision VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    observacion_revision VARCHAR(255),
    CONSTRAINT fk_tut_just_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_tut_just_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT fk_tut_just_doc FOREIGN KEY (id_docente) REFERENCES docente(id_docente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sistema_configuracion (
    clave VARCHAR(80) PRIMARY KEY,
    valor VARCHAR(255) NOT NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_charla (
    id_charla BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    tema VARCHAR(120) NOT NULL,
    ciclos_dirigidos VARCHAR(120) NOT NULL,
    fecha DATE NOT NULL,
    responsable VARCHAR(120) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tutoria_comunicado (
    id_comunicado BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha DATE NOT NULL,
    destinatarios VARCHAR(120) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_proyecto (
    id_proyecto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(180) NOT NULL,
    tipo_proyecto VARCHAR(60) NOT NULL,
    objetivo TEXT NOT NULL,
    problema_social TEXT NOT NULL,
    ods VARCHAR(180) NOT NULL,
    responsable VARCHAR(120) NOT NULL,
    estado_aprobacion VARCHAR(40) NOT NULL DEFAULT 'SOLICITADO',
    escuela_aliada VARCHAR(180),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(40) NOT NULL DEFAULT 'ACTIVO',
    fecha_clausura DATE,
    resultados TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_actividad (
    id_actividad BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora VARCHAR(10) NOT NULL,
    tema VARCHAR(180) NOT NULL,
    descripcion TEXT NOT NULL,
    duracion_horas DECIMAL(5,2) NOT NULL,
    laboratorio_ambiente VARCHAR(120) NOT NULL,
    CONSTRAINT fk_rsu_act_proy FOREIGN KEY (id_proyecto) REFERENCES rsu_proyecto(id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_participante (
    id_participante BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto BIGINT NOT NULL,
    codigo_estudiante VARCHAR(20) NOT NULL,
    responsabilidad VARCHAR(120) NOT NULL,
    horas_cumplidas DECIMAL(6,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_rsu_part_proy FOREIGN KEY (id_proyecto) REFERENCES rsu_proyecto(id_proyecto),
    CONSTRAINT fk_rsu_part_est FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_beneficiario (
    id_beneficiario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto BIGINT NOT NULL,
    nombres VARCHAR(150) NOT NULL,
    tipo_beneficiario VARCHAR(120) NOT NULL,
    documento VARCHAR(30),
    taller_inscrito VARCHAR(180),
    CONSTRAINT fk_rsu_ben_proy FOREIGN KEY (id_proyecto) REFERENCES rsu_proyecto(id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_asistencia (
    id_asistencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_actividad BIGINT NOT NULL,
    tipo_asistente VARCHAR(40) NOT NULL,
    identificador VARCHAR(80) NOT NULL,
    asistio TINYINT(1) NOT NULL DEFAULT 1,
    horas_reconocidas DECIMAL(5,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_rsu_asis_act FOREIGN KEY (id_actividad) REFERENCES rsu_actividad(id_actividad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_evidencia (
    id_evidencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto BIGINT NOT NULL,
    tipo_evidencia VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rsu_evi_proy FOREIGN KEY (id_proyecto) REFERENCES rsu_proyecto(id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rsu_concurso (
    id_concurso BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    criterios TEXT NOT NULL,
    fecha DATE NOT NULL,
    ganadores TEXT NOT NULL,
    CONSTRAINT fk_rsu_con_proy FOREIGN KEY (id_proyecto) REFERENCES rsu_proyecto(id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tipo_intervencion (
    id_tipo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE estado_intervencion (
    codigo VARCHAR(30) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    orden INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE intervencion (
    id_intervencion INT AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL,
    id_tipo INT NOT NULL,
    fecha DATE NOT NULL,
    motivo TEXT NOT NULL,
    responsable VARCHAR(120) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    derivacion VARCHAR(120),
    observaciones TEXT,
    CONSTRAINT fk_intervencion_estudiante FOREIGN KEY (codigo_estudiante) REFERENCES estudiante(codigo),
    CONSTRAINT fk_intervencion_tipo FOREIGN KEY (id_tipo) REFERENCES tipo_intervencion(id_tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE registro_seguimiento (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_intervencion INT NOT NULL,
    fecha_seguimiento DATE NOT NULL,
    descripcion TEXT NOT NULL,
    acuerdos TEXT,
    CONSTRAINT fk_seguimiento_intervencion FOREIGN KEY (id_intervencion) REFERENCES intervencion(id_intervencion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE facultades (
    id_facultad BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_facultad VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE escuelas (
    id_escuela BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_facultad BIGINT NOT NULL,
    nombre_escuela VARCHAR(100) NOT NULL,
    CONSTRAINT fk_escuelas_facultades FOREIGN KEY (id_facultad) REFERENCES facultades(id_facultad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE aulas (
    id_aula BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_escuela BIGINT NOT NULL,
    codigo_aula VARCHAR(20) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    CONSTRAINT fk_aulas_escuelas FOREIGN KEY (id_escuela) REFERENCES escuelas(id_escuela)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE laboratorios (
    id_laboratorio BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_escuela BIGINT NOT NULL,
    codigo_laboratorio VARCHAR(20) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    CONSTRAINT fk_laboratorios_escuelas FOREIGN KEY (id_escuela) REFERENCES escuelas(id_escuela)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ambientes_generales (
    id_ambiente BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_ambiente VARCHAR(100) NOT NULL,
    tipo_ambiente VARCHAR(50) NOT NULL,
    CONSTRAINT chk_tipo_ambiente CHECK (tipo_ambiente IN ('Cancha','Auditorio','Biblioteca','Sala','Patio','Administracion','Otro'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    tipo_usuario VARCHAR(30) NOT NULL,
    codigo_alumno VARCHAR(20) UNIQUE,
    documento VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT chk_tipo_usuario CHECK (tipo_usuario IN ('ADMIN','ALUMNO','DOCENTE','TUTOR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios_login (
    id_login BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_login_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE log_inicio_sesion (
    id_log BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_acceso VARCHAR(50),
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE clase_objeto (
    id_clase_objeto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_clase VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE objetos (
    id_objeto BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre_objeto VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    id_aula BIGINT NULL,
    id_laboratorio BIGINT NULL,
    id_ambiente_general BIGINT NULL,
    fecha_encontrado DATE NOT NULL,
    persona_encontro VARCHAR(120) NOT NULL,
    id_clase_objeto BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_objeto_aula FOREIGN KEY (id_aula) REFERENCES aulas(id_aula),
    CONSTRAINT fk_objeto_laboratorio FOREIGN KEY (id_laboratorio) REFERENCES laboratorios(id_laboratorio),
    CONSTRAINT fk_objeto_ambiente FOREIGN KEY (id_ambiente_general) REFERENCES ambientes_generales(id_ambiente),
    CONSTRAINT fk_objeto_clase FOREIGN KEY (id_clase_objeto) REFERENCES clase_objeto(id_clase_objeto),
    CONSTRAINT chk_estado_objeto CHECK (estado IN ('No entregado','Entregado')),
    CONSTRAINT chk_ubicacion_unica CHECK (
        (id_aula IS NOT NULL AND id_laboratorio IS NULL AND id_ambiente_general IS NULL) OR
        (id_aula IS NULL AND id_laboratorio IS NOT NULL AND id_ambiente_general IS NULL) OR
        (id_aula IS NULL AND id_laboratorio IS NULL AND id_ambiente_general IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_objeto (
    id_historial BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_objeto BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(50) NOT NULL,
    CONSTRAINT fk_historial_objeto FOREIGN KEY (id_objeto) REFERENCES objetos(id_objeto),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT chk_accion_historial CHECK (accion IN ('Registrado','Modificado','Estado actualizado','Entregado','Eliminado'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE entrega_objeto (
    id_entrega BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_objeto BIGINT NOT NULL UNIQUE,
    id_usuario_entrego BIGINT NOT NULL,
    tipo_receptor VARCHAR(30) NOT NULL,
    nombre_receptor VARCHAR(100) NOT NULL,
    apellidos_receptor VARCHAR(100) NOT NULL,
    documento_receptor VARCHAR(20),
    codigo_estudiante VARCHAR(20),
    telefono VARCHAR(15),
    email VARCHAR(100),
    fecha_entrega DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entrega_objeto FOREIGN KEY (id_objeto) REFERENCES objetos(id_objeto),
    CONSTRAINT fk_entrega_usuario FOREIGN KEY (id_usuario_entrego) REFERENCES usuarios(id_usuario),
    CONSTRAINT chk_tipo_receptor CHECK (tipo_receptor IN ('Alumno','Docente','Personal','Externo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- MySQL/MariaDB datos academicos realistas para EPIS.
-- Ejecutar este archivo completo desde HeidiSQL.
-- Todos los passwords demo son "123456" (bcrypt).

-- ============================================================================
-- 1. SISTA
-- ============================================================================
INSERT INTO sista_registros (nombre, descripcion, fecha, estado) VALUES
('Implementacion de modulo de matricula', 'Desarrollo del modulo de matriculas para el ciclo 2025-I', '2026-03-01', 'ACTIVO'),
('Migracion de base de datos academica', 'Migracion de datos desde el sistema legacy hacia el nuevo esquema MySQL local', '2026-03-15', 'EN_REVISION'),
('Modulo de generacion de horarios', 'Implementacion del generador automatico de horarios por escuela profesional', '2026-04-01', 'EN_DESARROLLO'),
('Sistema de reportes academicos', 'Modulo de reportes y dashboard para la direccion de escuela', '2026-02-10', 'ACTIVO'),
('Integracion con servicios web', 'API REST para integracion con el portal del estudiante', '2026-01-20', 'ACTIVO'),
('Registro de notas por competencias', 'Implementacion del nuevo sistema de evaluacion por competencias', '2026-04-10', 'EN_DESARROLLO');

-- ============================================================================
-- 2. TUTORIA SOLICITUDES
-- ============================================================================
INSERT INTO tutoria_solicitudes (titulo, descripcion, estado) VALUES
('Solicitud de tutoria academica', 'Solicitud inicial de tutoria academica para estudiantes de primer ingreso', 'ACTIVO'),
('Programa de acompanamiento preventivo', 'Solicitud para implementar programa de acompanamiento a estudiantes en riesgo', 'EN_EJECUCION'),
('Taller de tecnicas de estudio', 'Solicitud de taller para estudiantes con bajo rendimiento academico', 'ACTIVO'),
('Reforzamiento de matematicas', 'Solicitud de sesiones de reforzamiento para cursos de matematicas basicas', 'ACTIVO');

-- ============================================================================
-- 3. APODERADOS (15 registros)
-- ============================================================================
INSERT INTO apoderado (dni, nombres, apellidos, telefono, email, direccion, parentesco) VALUES
('70000001', 'MARIA', 'QUISPE FLORES', '950111222', 'mquispe@correo.com', 'AV. BOLOGNESI 100', 'MADRE'),
('70000002', 'JUAN', 'VILCA RAMOS', '950333444', 'jvilca@correo.com', 'AV. LEGUIA 200', 'PADRE'),
('70000003', 'ELENA', 'RAMOS PAREDES', '951111222', 'eramos@correo.com', 'AV. INDUSTRIAL 450', 'MADRE'),
('70000004', 'MIGUEL', 'ROJAS HUANCA', '952222333', 'mrojas@correo.com', 'JR. TARAPACA 315', 'PADRE'),
('70000005', 'LUCIA', 'FLORES DIAZ', '953333444', 'lflores@correo.com', 'AV. CUSCO 120', 'MADRE'),
('70000006', 'OSCAR', 'PEREZ MAMANI', '954444555', 'operez@correo.com', 'CALLE LOS PINOS 220', 'PADRE'),
('70000007', 'ROBERTO', 'HUANCA TICONA', '955111222', 'rhuanca@correo.com', 'URB. SANTA ROSA 120', 'PADRE'),
('70000008', 'CARMEN', 'SALAZAR VARGAS', '955333444', 'csalazar@correo.com', 'AV. LOS PROCERES 430', 'MADRE'),
('70000009', 'MARTIN', 'CASTRO MORALES', '956111222', 'mcastro@correo.com', 'CALLE GREGORIO ALBARRACIN 210', 'PADRE'),
('70000010', 'CLAUDIA', 'PAREDES ROJAS', '956333444', 'cparedes@correo.com', 'ASOC. VILLA UNIVERSITARIA MZ B', 'MADRE'),
('70000011', 'PEDRO', 'GUTIERREZ MENDOZA', '957111222', 'pgutierrez@correo.com', 'AV. CIRCUNVALACION 550', 'PADRE'),
('70000012', 'SOFIA', 'CONDORI APAZA', '957333444', 'scondori@correo.com', 'JR. SAN MARTIN 280', 'MADRE'),
('70000013', 'RICARDO', 'HUANCA MAMANI', '958111222', 'rhuancam@correo.com', 'CALLE LOS CLAVELES 150', 'PADRE'),
('70000014', 'VERONICA', 'CARDENAS MOLINA', '958333444', 'vcardenas@correo.com', 'URB. LAS PALMERAS MZ C LT 8', 'MADRE'),
('70000015', 'ALBERTO', 'VARGAS TORRES', '959111222', 'avargas@correo.com', 'AV. TACNA 780', 'PADRE');

-- ============================================================================
-- 4. DOCENTES (6 registros)
-- ============================================================================
INSERT INTO docente (codigo, dni, nombres, apellidos, telefono, email, especialidad) VALUES
('DOC001', '45678890', 'CARLOS', 'MAMANI', '981222333', 'cmamani@upt.pe', 'INGENIERIA DE SOFTWARE'),
('DOC002', '45678891', 'ANA', 'TORRES', '981222334', 'atorres@upt.pe', 'BASE DE DATOS'),
('DOC003', '45678892', 'PATRICIA', 'SALAS', '981222335', 'psalas@upt.pe', 'GESTION DE PROYECTOS'),
('DOC004', '45678893', 'JORGE', 'CONDORI', '981222336', 'jcondori@upt.pe', 'REDES Y SEGURIDAD'),
('DOC005', '45678894', 'ROSA', 'CONDORI APAZA', '981222337', 'rcondori@upt.pe', 'INTELIGENCIA ARTIFICIAL'),
('DOC006', '45678895', 'MIGUEL', 'ROJAS VILCA', '981222338', 'mrojas@upt.pe', 'SISTEMAS OPERATIVOS');

-- ============================================================================
-- 5. CICLOS (5 ciclos)
-- ============================================================================
INSERT INTO ciclo (numero_ciclo, nombre) VALUES
(1, 'Primer ciclo'),
(2, 'Segundo ciclo'),
(3, 'Tercer ciclo'),
(4, 'Cuarto ciclo'),
(5, 'Quinto ciclo');

-- ============================================================================
-- 6. CURSOS (10 cursos)
-- ============================================================================
INSERT INTO curso (codigo, nombre) VALUES
('IS101', 'Programacion I'),
('IS201', 'Base de Datos'),
('IS301', 'Ingenieria de Software'),
('IS401', 'Arquitectura de Software'),
('IS501', 'Redes de Computadoras'),
('IS601', 'Gestion de Proyectos'),
('IS701', 'Inteligencia Artificial'),
('IS801', 'Sistemas Operativos'),
('IS901', 'Desarrollo Web'),
('IS1001', 'Computacion Grafica');

-- ============================================================================
-- 7. CICLO_CURSO (mapping)
-- ============================================================================
INSERT INTO ciclo_curso (id_ciclo, id_curso) VALUES
(1, 1),  -- Ciclo 1: Programacion I
(1, 9),  -- Ciclo 1: Desarrollo Web
(2, 2),  -- Ciclo 2: Base de Datos
(2, 5),  -- Ciclo 2: Redes
(2, 10), -- Ciclo 2: Computacion Grafica
(3, 3),  -- Ciclo 3: Ingenieria de Software
(3, 4),  -- Ciclo 3: Arquitectura de Software
(4, 6),  -- Ciclo 4: Gestion de Proyectos
(4, 8),  -- Ciclo 4: Sistemas Operativos
(5, 7);  -- Ciclo 5: Inteligencia Artificial

-- ============================================================================
-- 8. CURSO_DOCENTE
-- ============================================================================
INSERT INTO curso_docente (id_curso, id_docente) VALUES
(1, 1),  -- Programacion I -> Carlos Mamani
(2, 2),  -- Base de Datos -> Ana Torres
(3, 1),  -- Ingenieria de Software -> Carlos Mamani
(4, 3),  -- Arquitectura de Software -> Patricia Salas
(5, 4),  -- Redes -> Jorge Condori
(6, 3),  -- Gestion de Proyectos -> Patricia Salas
(7, 5),  -- IA -> Rosa Condori
(8, 6),  -- Sistemas Operativos -> Miguel Rojas
(9, 5),  -- Desarrollo Web -> Rosa Condori
(10, 6); -- Computacion Grafica -> Miguel Rojas

-- ============================================================================
-- 9. ESTUDIANTES (15 registros)
-- ============================================================================
INSERT INTO estudiante (codigo, dni, nombres, apellidos, fecha_nacimiento, telefono, email, direccion, id_apoderado) VALUES
('2023078888', '75677777', 'SEBASTIAN', 'CORTEZ', '2004-05-10', '999999999', 'scortez@upt.pe', 'TACNA', 1),
('2023078434', '75676149', 'LUIS', 'VILCA', '2004-08-15', '901344647', 'lvilca@upt.pe', 'TACNA', 2),
('2023078001', '75670001', 'VALERIA', 'RAMOS', '2004-02-03', '901111111', 'vramos@upt.pe', 'TACNA', 3),
('2023078002', '75670002', 'DIEGO', 'ROJAS', '2003-11-21', '902222222', 'drojas@upt.pe', 'TACNA', 4),
('2023078003', '75670003', 'CAMILA', 'FLORES', '2005-01-18', '903333333', 'cflores@upt.pe', 'TACNA', 5),
('2023078004', '75670004', 'MATEO', 'PEREZ', '2004-09-08', '904444444', 'mperez@upt.pe', 'TACNA', 6),
('2023078005', '75670005', 'SOFIA', 'MAMANI', '2005-03-27', '905555555', 'smamani@upt.pe', 'TACNA', 1),
('2023078006', '75670006', 'GABRIEL', 'HUANCA', '2004-06-14', '906666666', 'ghuanca@upt.pe', 'TACNA', 7),
('2023078007', '75670007', 'HELENA', 'SALAZAR', '2005-07-03', '907777777', 'hsalazar@upt.pe', 'TACNA', 8),
('2023078008', '75670008', 'IVAN', 'CASTRO', '2004-12-19', '908888888', 'icastro@upt.pe', 'TACNA', 9),
('2023078009', '75670009', 'JULIA', 'PAREDES', '2003-10-04', '909999999', 'jparedes@upt.pe', 'TACNA', 10),
('2023078010', '75670010', 'ALEXANDER', 'GUTIERREZ', '2005-05-15', '910101010', 'agutierrez@upt.pe', 'TACNA', 11),
('2023078011', '75670011', 'NICOLE', 'CONDORI', '2004-11-08', '911111111', 'ncondori@upt.pe', 'TACNA', 12),
('2023078012', '75670012', 'FERNANDO', 'HUANCA', '2005-01-30', '912121212', 'fhuanca@upt.pe', 'TACNA', 13),
('2023078013', '75670013', 'ALEJANDRA', 'CARDENAS', '2004-03-22', '913131313', 'acardenas@upt.pe', 'TACNA', 14);

-- ============================================================================
-- 10. MATRICULA_CURSO (30+ registros)
-- ============================================================================
INSERT INTO matricula_curso (codigo_estudiante, id_curso_docente, estado, numero_matricula) VALUES
-- Sebastian Cortez
('2023078888', 1, 'APROBADO', 1),
('2023078888', 2, 'CURSANDO', 1),
('2023078888', 5, 'CURSANDO', 1),
('2023078888', 9, 'APROBADO', 1),
('2023078888', 10, 'APROBADO', 1),
-- Luis Vilca
('2023078434', 1, 'CURSANDO', 3),
('2023078434', 3, 'DESAPROBADO', 4),
('2023078434', 7, 'CURSANDO', 2),
('2023078434', 10, 'CURSANDO', 2),
-- Valeria Ramos
('2023078001', 2, 'CURSANDO', 1),
('2023078001', 4, 'CURSANDO', 1),
('2023078001', 8, 'CURSANDO', 1),
-- Diego Rojas
('2023078002', 3, 'DESAPROBADO', 3),
('2023078002', 5, 'CURSANDO', 2),
('2023078002', 6, 'CURSANDO', 2),
-- Camila Flores
('2023078003', 1, 'APROBADO', 1),
('2023078003', 6, 'CURSANDO', 1),
('2023078003', 9, 'CURSANDO', 1),
-- Mateo Perez
('2023078004', 4, 'CURSANDO', 4),
('2023078004', 7, 'CURSANDO', 1),
-- Sofia Mamani
('2023078005', 5, 'CURSANDO', 1),
-- Gabriel Huanca
('2023078006', 2, 'CURSANDO', 1),
('2023078006', 5, 'CURSANDO', 1),
('2023078006', 10, 'CURSANDO', 1),
-- Helena Salazar
('2023078007', 1, 'APROBADO', 1),
('2023078007', 3, 'CURSANDO', 1),
-- Ivan Castro
('2023078008', 4, 'DESAPROBADO', 2),
('2023078008', 6, 'CURSANDO', 1),
-- Julia Paredes
('2023078009', 2, 'CURSANDO', 3),
('2023078009', 5, 'CURSANDO', 3),
('2023078009', 8, 'CURSANDO', 1),
-- Alexander Gutierrez
('2023078010', 1, 'CURSANDO', 1),
('2023078010', 9, 'CURSANDO', 1),
-- Nicole Condori
('2023078011', 2, 'APROBADO', 1),
('2023078011', 10, 'CURSANDO', 1),
-- Fernando Huanca
('2023078012', 4, 'CURSANDO', 2),
('2023078012', 7, 'CURSANDO', 1),
-- Alejandra Cardenas
('2023078013', 3, 'CURSANDO', 1),
('2023078013', 6, 'CURSANDO', 1);

-- ============================================================================
-- 11. CALIFICACION (30+ registros)
-- ============================================================================
INSERT INTO calificacion (id_matricula_curso, unidad1, unidad2, unidad3, promedio) VALUES
(1,  15, 14, 16, 15.00),   -- Sebastian - Prog I
(2,  12, 13, 0,  12.50),   -- Sebastian - BD
(3,  14, 15, 0,  14.50),   -- Sebastian - Redes
(4,  16, 17, 18, 17.00),   -- Sebastian - DW
(5,  14, 15, 16, 15.00),   -- Sebastian - CG
(6,  11, 12, 0,  11.50),   -- Luis - Prog I
(7,  8,  9,  10, 9.00),    -- Luis - IS
(8,  10, 12, 0,  11.00),   -- Luis - IA
(9,  13, 14, 0,  13.50),   -- Luis - CG
(10, 13, 14, 0,  13.50),   -- Valeria - BD
(11, 16, 15, 0,  15.50),   -- Valeria - Arquitectura
(12, 14, 13, 0,  13.50),   -- Valeria - SO
(13, 9,  10, 8,  9.00),    -- Diego - IS
(14, 12, 11, 0,  11.50),   -- Diego - Redes
(15, 10, 11, 0,  10.50),   -- Diego - GP
(16, 17, 16, 18, 17.00),   -- Camila - Prog I
(17, 14, 15, 0,  14.50),   -- Camila - GP
(18, 15, 16, 0,  15.50),   -- Camila - DW
(19, 10, 9,  0,  9.50),    -- Mateo - Arquitectura
(20, 11, 13, 0,  12.00),   -- Mateo - IA
(21, 13, 13, 0,  13.00),   -- Sofia - Redes
(22, 14, 13, 0,  13.50),   -- Gabriel - BD
(23, 12, 12, 0,  12.00),   -- Gabriel - Redes
(24, 15, 16, 0,  15.50),   -- Gabriel - CG
(25, 17, 16, 18, 17.00),   -- Helena - Prog I
(26, 13, 14, 0,  13.50),   -- Helena - IS
(27, 8,  9,  10, 9.00),    -- Ivan - Arquitectura
(28, 11, 12, 0,  11.50),   -- Ivan - GP
(29, 10, 11, 0,  10.50),   -- Julia - BD
(30, 12, 10, 0,  11.00),   -- Julia - Redes
(31, 14, 13, 0,  13.50),   -- Julia - SO
(32, 15, 16, 0,  15.50),   -- Alexander - Prog I
(33, 14, 13, 0,  13.50),   -- Alexander - DW
(34, 16, 17, 18, 17.00),   -- Nicole - BD
(35, 15, 14, 0,  14.50),   -- Nicole - CG
(36, 12, 13, 0,  12.50),   -- Fernando - Arquitectura
(37, 11, 12, 0,  11.50),   -- Fernando - IA
(38, 13, 14, 0,  13.50),   -- Alejandra - IS
(39, 12, 13, 0,  12.50);   -- Alejandra - GP

-- ============================================================================
-- 12. HISTORIAL_CALIFICACION (periodos anteriores)
-- ============================================================================
INSERT INTO historial_calificacion (codigo_estudiante, id_curso, periodo, unidad1, unidad2, unidad3, promedio, estado) VALUES
-- Sebastian Cortez - periodos anteriores
('2023078888', 1, '2024-II', 14, 15, 15, 14.67, 'APROBADO'),
('2023078888', 9, '2024-II', 16, 14, 15, 15.00, 'APROBADO'),
('2023078888', 2, '2024-II', 13, 12, 14, 13.00, 'APROBADO'),
('2023078888', 10, '2024-I', 15, 16, 15, 15.33, 'APROBADO'),
-- Luis Vilca - periodos anteriores
('2023078434', 1, '2024-II', 10, 11, 12, 11.00, 'APROBADO'),
('2023078434', 3, '2024-II', 8,  9,  9,  8.67,  'DESAPROBADO'),
('2023078434', 2, '2024-I', 11, 10, 12, 11.00, 'APROBADO'),
('2023078434', 5, '2024-I', 12, 11, 13, 12.00, 'APROBADO'),
-- Valeria Ramos
('2023078001', 1, '2024-I', 8,  9,  10, 9.00,  'DESAPROBADO'),
('2023078001', 9, '2024-II', 12, 13, 14, 13.00, 'APROBADO'),
-- Diego Rojas
('2023078002', 2, '2024-II', 9,  10, 11, 10.00, 'APROBADO'),
('2023078002', 5, '2024-I', 13, 12, 14, 13.00, 'APROBADO'),
('2023078002', 3, '2024-I', 7,  8,  9,  8.00,  'DESAPROBADO'),
-- Camila Flores
('2023078003', 1, '2024-II', 15, 16, 17, 16.00, 'APROBADO'),
('2023078003', 2, '2024-II', 14, 15, 14, 14.33, 'APROBADO'),
('2023078003', 4, '2024-II', 13, 14, 13, 13.33, 'APROBADO'),
('2023078003', 5, '2024-II', 15, 16, 15, 15.33, 'APROBADO'),
-- Mateo Perez
('2023078004', 1, '2024-I', 8,  7,  9,  8.00,  'DESAPROBADO'),
('2023078004', 2, '2024-I', 9,  8,  10, 9.00,  'DESAPROBADO'),
('2023078004', 5, '2024-II', 10, 11, 12, 11.00, 'APROBADO'),
('2023078004', 9, '2024-II', 11, 12, 13, 12.00, 'APROBADO'),
-- Helena Salazar
('2023078007', 1, '2024-II', 16, 17, 18, 17.00, 'APROBADO'),
('2023078007', 2, '2024-II', 15, 16, 15, 15.33, 'APROBADO'),
-- Ivan Castro
('2023078008', 1, '2024-I', 10, 11, 12, 11.00, 'APROBADO'),
('2023078008', 2, '2024-I', 9,  10, 11, 10.00, 'APROBADO'),
('2023078008', 9, '2024-II', 8,  9,  10, 9.00,  'DESAPROBADO'),
-- Alexander Gutierrez
('2023078010', 1, '2024-II', 14, 15, 16, 15.00, 'APROBADO'),
('2023078010', 9, '2024-II', 13, 14, 13, 13.33, 'APROBADO'),
-- Nicole Condori
('2023078011', 1, '2024-II', 16, 15, 17, 16.00, 'APROBADO'),
('2023078011', 9, '2024-II', 14, 15, 14, 14.33, 'APROBADO'),
-- Alejandra Cardenas
('2023078013', 1, '2024-I', 12, 13, 14, 13.00, 'APROBADO'),
('2023078013', 2, '2024-II', 13, 14, 15, 14.00, 'APROBADO'),
('2023078013', 10, '2024-II', 12, 13, 12, 12.33, 'APROBADO');

-- ============================================================================
-- 13. TIPO_INTERVENCION + ESTADO_INTERVENCION
-- ============================================================================
INSERT INTO tipo_intervencion (nombre) VALUES
('Riesgo academico'),
('Asistencia'),
('Tutoria psicopedagogica'),
('Orientacion vocacional'),
('Acompanamiento preventivo');

INSERT INTO estado_intervencion (codigo, nombre, orden) VALUES
('PENDIENTE', 'Pendiente', 1),
('EN_PROCESO', 'En proceso', 2),
('COMPLETO', 'Completo', 3);

-- ============================================================================
-- 14. INTERVENCIONES (12 registros)
-- ============================================================================
INSERT INTO intervencion (codigo_estudiante, id_tipo, fecha, motivo, responsable, estado) VALUES
('2023078888', 1, '2025-03-20', 'PROMEDIO EN OBSERVACION EN BASE DE DATOS', 'CARLOS MAMANI', 'EN_PROCESO'),
('2023078434', 2, '2025-03-22', 'INASISTENCIAS FRECUENTES A CLASES DE PROGRAMACION', 'ANA TORRES', 'PENDIENTE'),
('2023078002', 1, '2025-03-24', 'CURSOS DESAPROBADOS EN SEGUNDO Y TERCER CICLO', 'PATRICIA SALAS', 'PENDIENTE'),
('2023078004', 1, '2025-03-27', 'CUARTA MATRICULA EN ARQUITECTURA DE SOFTWARE', 'PATRICIA SALAS', 'EN_PROCESO'),
('2023078001', 3, '2025-03-29', 'ACOMPANAMIENTO PREVENTIVO POR CAMBIO DE CICLO', 'JORGE CONDORI', 'COMPLETO'),
('2023078008', 1, '2025-04-03', 'BAJO PROMEDIO EN ARQUITECTURA DE SOFTWARE', 'ANA TORRES', 'PENDIENTE'),
('2023078009', 2, '2025-04-05', 'ACUMULA INASISTENCIAS EN BASE DE DATOS', 'CARLOS MAMANI', 'EN_PROCESO'),
('2023078006', 3, '2025-04-08', 'ORIENTACION POR CAMBIO DE HABITOS DE ESTUDIO', 'JORGE CONDORI', 'COMPLETO'),
('2023078012', 4, '2025-04-12', 'DUDA SOBRE ESPECIALIDAD Y MERCADO LABORAL', 'ROSA CONDORI', 'PENDIENTE'),
('2023078010', 5, '2025-04-15', 'PREVENCION TEMPRANA POR RENDIMIENTO IRREGULAR', 'CARLOS MAMANI', 'EN_PROCESO'),
('2023078005', 2, '2025-04-18', 'FALTAS CONSTANTES EN CLASE DE REDES', 'JORGE CONDORI', 'PENDIENTE'),
('2023078011', 3, '2025-04-20', 'ADAPTACION AL RITMO ACADEMICO UNIVERSITARIO', 'ANA TORRES', 'COMPLETO');

-- ============================================================================
-- 15. REGISTRO_SEGUIMIENTO
-- ============================================================================
INSERT INTO registro_seguimiento (id_intervencion, fecha_seguimiento, descripcion, acuerdos) VALUES
(1, '2025-03-25', 'SE REALIZO ENTREVISTA CON EL ESTUDIANTE PARA ANALIZAR RENDIMIENTO', 'REFORZAMIENTO SEMANAL EN BASE DE DATOS'),
(2, '2025-03-26', 'SE CONTACTO AL APODERADO PARA INFORMAR SOBRE INASISTENCIAS', 'JUSTIFICAR INASISTENCIAS EN 48 HORAS'),
(3, '2025-03-28', 'REVISION DE HISTORIAL ACADEMICO COMPLETO', 'PLAN DE ESTUDIO ASISTIDO CON TUTOR'),
(4, '2025-03-30', 'REUNION CON COORDINACION DE ESCUELA', 'MONITOREO QUINCENAL DEL RENDIMIENTO'),
(5, '2025-04-01', 'CASO CERRADO SIN OBSERVACIONES PENDIENTES', 'SEGUIMIENTO PREVENTIVO CONTINUO'),
(6, '2025-04-09', 'SE PROGRAMO REFORZAMIENTO CON DOCENTE DEL CURSO', 'ASISTENCIA OBLIGATORIA CADA VIERNES'),
(7, '2025-04-10', 'SE NOTIFICO AL TUTOR SOBRE LAS INASISTENCIAS REGISTRADAS', 'PRESENTAR JUSTIFICACIONES EN 48 HORAS'),
(8, '2025-04-11', 'SE REALIZO CHARLA DE ORIENTACION ACADEMICA INDIVIDUAL', 'MANTENER PLAN SEMANAL DE ESTUDIO'),
(9, '2025-04-14', 'ENTREVISTA DE ORIENTACION VOCACIONAL REALIZADA', 'ASISTIR A FERIA DE ESPECIALIDADES'),
(10, '2025-04-17', 'PRIMER SEGUIMIENTO DE RENDIMIENTO EN PROGRAMACION', 'REVISAR AVANCE CADA 15 DIAS'),
(11, '2025-04-21', 'REGISTRO DE FALTA Y CONTACTO CON APODERADO', 'JUSTIFICACION MEDICA SI CORRESPONDE'),
(12, '2025-04-22', 'ENTREVISTA DE ADAPTACION REALIZADA CON RESULTADOS POSITIVOS', 'CONTINUAR CON MONITOREO MENSUAL');

-- ============================================================================
-- 16. SISTEMA CONFIGURACION
-- ============================================================================
INSERT INTO sistema_configuracion (clave, valor) VALUES
('correo_alertas_inasistencia', 'tutoria@upt.edu.pe');

-- ============================================================================
-- 17. TUTORIA SEGUIMIENTO ESTUDIANTE
-- ============================================================================
INSERT INTO tutoria_estudiante_seguimiento
(codigo_estudiante, motivo, tipo_seguimiento, unidad_academica, estado, observacion_positiva, advertencia_academica) VALUES
('2023078434', 'Curso desaprobado y cuarta matricula en Ingenieria de Software', 'CUARTA_MATRICULA', 1, 'EN_SEGUIMIENTO', NULL, 'Debe asistir a reforzamiento obligatorio'),
('2023078888', 'Buen rendimiento academico sostenido en todos los cursos', 'BECADO', 1, 'ABIERTO', 'Felicitacion por rendimiento sostenido en el semestre', NULL),
('2023078002', 'Riesgo por curso desaprobado en Ingenieria de Software', 'RIESGO_ACADEMICO', 2, 'ABIERTO', NULL, 'Debe presentar plan de recuperacion antes del examen final'),
('2023078004', 'Cuarta matricula detectada en Arquitectura de Software', 'CUARTA_MATRICULA', 2, 'EN_SEGUIMIENTO', NULL, 'Asistencia obligatoria a sesiones de tutoria semanal'),
('2023078001', 'Seguimiento preventivo por historial de bajo rendimiento', 'SITUACION_ESPECIAL', 1, 'CERRADO', 'Participacion regular en clases y mejoras notables', NULL),
('2023078008', 'Segunda matricula desaprobada en Arquitectura de Software', 'CUARTA_MATRICULA', 2, 'EN_SEGUIMIENTO', NULL, 'Evaluacion psicopedagogica requerida'),
('2023078012', 'Orientacion vocacional para eleccion de mencion', 'SITUACION_ESPECIAL', 1, 'ABIERTO', 'Interes demostrado en multiples areas', 'Definir mencion antes del siguiente ciclo'),
('2023078010', 'Rendimiento irregular detectado en primeros ciclos', 'RIESGO_ACADEMICO', 1, 'EN_SEGUIMIENTO', 'Buena actitud en clases practicas', 'Mejorar promedio en teoria'),
('2023078011', 'Seguimiento por adaptacion academica', 'SITUACION_ESPECIAL', 1, 'CERRADO', 'Adaptacion satisfactoria al entorno universitario', NULL);

-- ============================================================================
-- 18. TUTORIA NOTA
-- ============================================================================
INSERT INTO tutoria_nota (codigo_estudiante, id_curso, unidad_academica, nota, observacion) VALUES
('2023078434', 3, 1, 9.00, 'Requiere seguimiento inmediato en Ingenieria de Software'),
('2023078888', 1, 1, 15.00, 'Rendimiento destacado en Programacion I'),
('2023078002', 6, 1, 8.50, 'Rendimiento por debajo del minimo en Gestion de Proyectos'),
('2023078004', 4, 1, 7.00, 'Riesgo academico alto en Arquitectura de Software'),
('2023078012', 7, 1, 12.00, 'Rendimiento regular en IA con potencial de mejora');

-- ============================================================================
-- 19. TUTORIA CITACION
-- ============================================================================
INSERT INTO tutoria_citacion (codigo_estudiante, fecha_citacion, motivo, estado) VALUES
('2023078434', '2025-03-28', 'Citacion por riesgo academico en Ingenieria de Software', 'PENDIENTE'),
('2023078004', '2025-04-10', 'Citacion por cuarta matricula en Arquitectura de Software', 'PENDIENTE'),
('2023078002', '2025-04-15', 'Citacion para revisar plan de recuperacion', 'REALIZADA'),
('2023078008', '2025-04-20', 'Citacion por bajo rendimiento en cursos del ciclo', 'PENDIENTE'),
('2023078012', '2025-04-25', 'Citacion para orientacion vocacional', 'REALIZADA');

-- ============================================================================
-- 20. TUTORIA ENTREVISTA
-- ============================================================================
INSERT INTO tutoria_entrevista (codigo_estudiante, fecha, problemas_detectados, observaciones, recomendaciones, derivacion_area) VALUES
('2023078434', '2025-03-29', 'Dificultades de asistencia y bajo promedio en dos cursos', 'Se recomienda seguimiento por unidad academica y psicologia', 'Asistir a reforzamiento y tutoria semanal obligatoria', 'PSICOLOGIA'),
('2023078004', '2025-04-11', 'Frustracion academica por repetir curso por cuarta vez', 'Posible cambio de estrategia de estudio y apoyo psicologico', 'Evaluacion psicopedagogica completa', 'BIENESTAR_UNIVERSITARIO'),
('2023078002', '2025-04-16', 'Desmotivacion general y ausentismo en clases teoricas', 'Presenta mejora en clases practicas de laboratorio', 'Plan de estudio personalizado con horario fijo', NULL),
('2023078012', '2025-04-26', 'Indecision sobre mencion de especialidad', 'Perfil compatible con varias areas de la carrera', 'Asistir a charlas informativas de cada mencion', 'ORIENTACION_VOCACIONAL');

-- ============================================================================
-- 21. TUTORIA INASISTENCIA
-- ============================================================================
INSERT INTO tutoria_inasistencia (codigo_estudiante, id_curso, id_docente, fecha, motivo) VALUES
('2023078434', 1, 1, '2025-03-10', 'No asistio a clase'),
('2023078434', 1, 1, '2025-03-11', 'No asistio a clase'),
('2023078434', 1, 1, '2025-03-12', 'No asistio a clase por problemas de salud'),
('2023078002', 3, 1, '2025-03-18', 'No asistio a clase'),
('2023078002', 3, 1, '2025-03-19', 'No asistio a clase'),
('2023078002', 3, 1, '2025-03-20', 'No asistio a clase'),
('2023078004', 4, 3, '2025-03-21', 'Tardanza acumulada de 30 minutos'),
('2023078004', 4, 3, '2025-03-22', 'No asistio a clase'),
('2023078004', 4, 3, '2025-03-23', 'No asistio a clase'),
('2023078004', 4, 3, '2025-03-24', 'No asistio a clase'),
('2023078005', 5, 4, '2025-04-01', 'No asistio a clase'),
('2023078005', 5, 4, '2025-04-03', 'No asistio a clase'),
('2023078005', 5, 4, '2025-04-08', 'No asistio a clase'),
('2023078009', 2, 2, '2025-04-02', 'No asistio a clase'),
('2023078009', 2, 2, '2025-04-04', 'No asistio a clase'),
('2023078009', 2, 2, '2025-04-09', 'No asistio a clase');

-- ============================================================================
-- 22. TUTORIA JUSTIFICACION
-- ============================================================================
INSERT INTO tutoria_justificacion
(codigo_estudiante, id_curso, id_docente, fecha_reincorporacion, fecha_presentacion, dias_faltados, motivo, evidencia, estado) VALUES
('2023078434', 1, 1, '2025-03-13', '2025-03-14', 3, 'Problemas de salud', 'Sustento medico presentado en texto', 'EN_PLAZO'),
('2023078004', 4, 3, '2025-03-25', '2025-03-26', 4, 'Problemas familiares', 'Carta de apoderado presentada', 'EN_PLAZO'),
('2023078005', 5, 4, '2025-04-10', '2025-04-11', 3, 'Infeccion estomacal', 'Certificado medico digital', 'FUERA_PLAZO'),
('2023078009', 2, 2, '2025-04-11', '2025-04-14', 3, 'Viaje imprevisto por emergencia familiar', 'Sustento escrito con firma del apoderado', 'EN_REVISION');

-- ============================================================================
-- 23. TUTORIA CHARLA
-- ============================================================================
INSERT INTO tutoria_charla (titulo, tema, ciclos_dirigidos, fecha, responsable) VALUES
('Induccion a servicios universitarios', 'Servicios universitarios y bienestar estudiantil', 'Primer y segundo ciclo', '2025-04-01', 'Coordinacion de tutoria'),
('Tecnicas de estudio y manejo del tiempo', 'Estrategias de aprendizaje activo', 'Primer ciclo', '2025-04-15', 'Oficina de Psicopedagogia'),
('Prevencion de estres academico', 'Salud mental y equilibrio emocional', 'Todos los ciclos', '2025-04-28', 'Bienestar Universitario'),
('Oportunidades de movilidad academica', 'Programas de intercambio estudiantil', 'Tercer y cuarto ciclo', '2025-05-10', 'Oficina de Relaciones Internacionales'),
('Preparacion para el mercado laboral', 'Habilidades blandas y CV profesional', 'Cuarto y quinto ciclo', '2025-05-20', 'Oficina de Empleabilidad');

-- ============================================================================
-- 24. TUTORIA COMUNICADO
-- ============================================================================
INSERT INTO tutoria_comunicado (titulo, mensaje, fecha, destinatarios) VALUES
('Participacion obligatoria en tutoria', 'Se recuerda a todos los estudiantes que las sesiones de tutoria son de caracter obligatorio. Comunicarse con su tutor asignado para reprogramar en caso de inasistencia justificada.', '2025-04-02', 'Todos los estudiantes'),
('Taller de tecnicas de estudio', 'Se invita a los estudiantes con bajo rendimiento academico a inscribirse en el taller de tecnicas de estudio que se realizara los sabados de 10:00 a 12:00 en el pabellon P.', '2025-04-14', 'Estudiantes en riesgo academico'),
('Cronograma de entrevistas de tutoria', 'Se publico el cronograma de entrevistas obligatorias para estudiantes con seguimiento activo. Revisar el panel de tutoria para conocer la fecha asignada.', '2025-04-22', 'Estudiantes con seguimiento activo');

-- ============================================================================
-- 25. RSU PROYECTOS (6 proyectos)
-- ============================================================================
INSERT INTO rsu_proyecto
(nombre, tipo_proyecto, objetivo, problema_social, ods, responsable, estado_aprobacion, escuela_aliada, fecha_inicio, fecha_fin, estado) VALUES
('Taller de desarrollo web con IA', 'SERVICIO_SOCIAL_UNIVERSITARIO', 'Capacitar a estudiantes de secundaria en herramientas web e IA', 'Brecha digital en escolares de Tacna', 'ODS 4, ODS 10', 'Responsable RSU EPIS', 'APROBADO', 'EPIS', '2025-04-10', '2025-05-10', 'ACTIVO'),
('Alfabetizacion digital comunitaria', 'RESPONSABILIDAD_SOCIAL', 'Fortalecer competencias digitales basicas en adultos mayores', 'Acceso limitado a tecnologia en adultos mayores', 'ODS 4, ODS 9', 'Coordinacion RSU EPIS', 'PENDIENTE', 'EPIS', '2025-05-12', '2025-06-20', 'ACTIVO'),
('Voluntariado de soporte academico', 'VOLUNTARIADO', 'Apoyar a escolares de zonas rurales en pensamiento computacional', 'Bajo acceso a reforzamiento academico en zonas rurales', 'ODS 4', 'Docentes EPIS', 'APROBADO', 'EPIS', '2025-06-01', '2025-07-15', 'ACTIVO'),
('Reciclaje tecnologico con impacto social', 'RESPONSABILIDAD_SOCIAL', 'Recolectar y reacondicionar equipos informaticos para asociaciones sin fines de lucro', 'Brecha digital y contaminacion electronica', 'ODS 9, ODS 12', 'Comite de Sostenibilidad EPIS', 'APROBADO', 'EPIS', '2025-04-20', '2025-06-30', 'ACTIVO'),
('Club de robotica para ninos', 'VOLUNTARIADO', 'Ensenar fundamentos de robotica educativa a ninos de primaria', 'Falta de acceso a educacion STEM en colegios publicos', 'ODS 4, ODS 10', 'Docentes y estudiantes EPIS', 'APROBADO', 'EPIS', '2025-05-05', '2025-07-05', 'ACTIVO'),
('Capacitacion en ciberseguridad', 'SERVICIO_SOCIAL_UNIVERSITARIO', 'Capacitar a microempresarios en buenas practicas de seguridad digital', 'Vulnerabilidad digital en pequenos negocios', 'ODS 8, ODS 9', 'Grupo de Investigacion en Seguridad EPIS', 'PENDIENTE', 'EPIS', '2025-06-15', '2025-07-30', 'ACTIVO');

-- ============================================================================
-- 26. RSU ACTIVIDADES
-- ============================================================================
INSERT INTO rsu_actividad (id_proyecto, fecha, hora, tema, descripcion, duracion_horas, laboratorio_ambiente) VALUES
(1, '2025-04-12', '09:00', 'Introduccion a paginas web con HTML y CSS', 'Sesion practica en laboratorio con estudiantes de secundaria', 4, 'LAB-A'),
(1, '2025-04-19', '09:00', 'Prototipos interactivos con herramientas de IA', 'Taller guiado con equipos de trabajo colaborativo', 4, 'LAB-B'),
(1, '2025-04-26', '09:00', 'Publicacion de proyectos web', 'Sesion final de presentacion de proyectos desarrollados', 4, 'LAB-A'),
(2, '2025-05-20', '10:00', 'Herramientas digitales basicas para adultos mayores', 'Capacitacion a beneficiarios externos en uso de dispositivos moviles', 3, 'LAB-C'),
(2, '2025-05-27', '10:00', 'Navegacion segura en internet', 'Taller de ciberseguridad basica para usuarios no tecnicos', 3, 'LAB-C'),
(3, '2025-06-08', '08:30', 'Pensamiento computacional con Scratch', 'Sesion de reforzamiento escolar para ninos de primaria', 5, 'P-301'),
(3, '2025-06-15', '08:30', 'Introduccion a la programacion con bloques', 'Taller practico con actividades ludicas de programacion', 5, 'P-301'),
(4, '2025-04-25', '14:00', 'Jornada de recoleccion de equipos electronicos', 'Campana de recoleccion de equipos en desuso en EPIS', 6, 'Patio EPIS'),
(4, '2025-05-09', '14:00', 'Taller de reacondicionamiento de equipos', 'Sesion de reparacion y puesta a punto de equipos recolectados', 4, 'LAB-B'),
(5, '2025-05-10', '09:00', 'Introduccion a robotica educativa', 'Taller de armado de robots simples con kits Lego', 4, 'LAB-A'),
(5, '2025-05-24', '09:00', 'Programacion de robots con sensores', 'Sesion de programacion basica de sensores y actuadores', 4, 'LAB-A'),
(6, '2025-06-22', '10:00', 'Conceptos basicos de ciberseguridad para negocios', 'Charla-taller sobre riesgos digitales en pequenas empresas', 3, 'P-307');

-- ============================================================================
-- 27. RSU PARTICIPANTES
-- ============================================================================
INSERT INTO rsu_participante (id_proyecto, codigo_estudiante, responsabilidad, horas_cumplidas) VALUES
(1, '2023078888', 'Apoyo tecnico en laboratorio', 32),
(1, '2023078434', 'Encargado de publicidad y difusion', 12),
(2, '2023078001', 'Facilitadora de talleres para adultos mayores', 18),
(2, '2023078003', 'Monitoreo y registro de asistencia', 22),
(2, '2023078006', 'Apoyo logistico en actividades', 16),
(3, '2023078004', 'Asistencia en reforzamiento escolar', 30),
(3, '2023078005', 'Apoyo tecnico en preparacion de materiales', 28),
(4, '2023078007', 'Coordinadora de campana de recoleccion', 24),
(4, '2023078008', 'Registro y clasificacion de equipos', 20),
(4, '2023078011', 'Apoyo en taller de reacondicionamiento', 18),
(5, '2023078009', 'Instructora de robotica para ninos', 26),
(5, '2023078013', 'Asistente de logistica en talleres', 22),
(6, '2023078010', 'Difusion y convocatoria a microempresarios', 10),
(6, '2023078012', 'Apoyo en preparacion de materiales', 8);

-- ============================================================================
-- 28. RSU BENEFICIARIOS
-- ============================================================================
INSERT INTO rsu_beneficiario (id_proyecto, nombres, tipo_beneficiario, documento, taller_inscrito) VALUES
(1, 'Colegio Nacional Coronel Bolognesi - Grupo A', 'Estudiantes de secundaria', NULL, 'Desarrollo web con IA'),
(1, 'Colegio Nacional Coronel Bolognesi - Grupo B', 'Estudiantes de secundaria', NULL, 'Desarrollo web con IA'),
(2, 'Asociacion de Adultos Mayores "Nueva Esperanza"', 'Adultos mayores', NULL, 'Alfabetizacion digital'),
(3, 'I.E. NÂ° 42001 "Santa Rosa" - Zona rural', 'Escolares de primaria', NULL, 'Pensamiento computacional'),
(4, 'Asociacion "Tecnologia para Todos"', 'Organizacion sin fines de lucro', 'RUC 20123456781', 'Reciclaje tecnologico'),
(5, 'I.E. NÂ° 42005 "Manuel Belgrano" - Nivel primaria', 'Escolares de primaria', NULL, 'Robotica educativa'),
(6, 'Camara de Comercio de Tacna - Pequenos empresarios', 'Microempresarios', 'RUC 20123456782', 'Ciberseguridad para negocios');

-- ============================================================================
-- 29. RSU ASISTENCIAS
-- ============================================================================
INSERT INTO rsu_asistencia (id_actividad, tipo_asistente, identificador, asistio, horas_reconocidas) VALUES
(1, 'ESTUDIANTE', '2023078888', 1, 4),
(1, 'BENEFICIARIO', 'Colegio Coronel Bolognesi - Grupo A', 1, 4),
(2, 'ESTUDIANTE', '2023078888', 1, 4),
(2, 'ESTUDIANTE', '2023078434', 1, 4),
(2, 'BENEFICIARIO', 'Colegio Coronel Bolognesi - Grupo A', 1, 4),
(3, 'ESTUDIANTE', '2023078888', 1, 4),
(3, 'BENEFICIARIO', 'Colegio Coronel Bolognesi - Grupo A', 0, 0),
(4, 'ESTUDIANTE', '2023078001', 1, 3),
(4, 'BENEFICIARIO', 'Adultos Mayores "Nueva Esperanza"', 1, 3),
(6, 'ESTUDIANTE', '2023078004', 1, 5),
(6, 'BENEFICIARIO', 'I.E. Santa Rosa', 1, 5),
(8, 'ESTUDIANTE', '2023078007', 1, 6),
(8, 'BENEFICIARIO', 'Asociacion Tecnologia para Todos', 1, 6),
(10, 'ESTUDIANTE', '2023078009', 1, 4),
(10, 'BENEFICIARIO', 'I.E. Manuel Belgrano', 1, 4);

-- ============================================================================
-- 30. RSU EVIDENCIAS
-- ============================================================================
INSERT INTO rsu_evidencia (id_proyecto, tipo_evidencia, descripcion) VALUES
(1, 'Informe', 'Informe de la primera sesion del taller de desarrollo web con IA'),
(1, 'Fotografia', 'Registro fotografico de los estudiantes trabajando en laboratorio'),
(2, 'Informe', 'Plan de trabajo del proyecto de alfabetizacion digital'),
(3, 'Lista de asistencia', 'Registro de asistencia de escolares al taller de pensamiento computacional'),
(4, 'Acta de donacion', 'Acta de donacion de equipos reacondicionados a la asociacion beneficiaria'),
(5, 'Video', 'Video resumen del taller de robotica educativa'),
(6, 'Material didactico', 'Presentacion y guia del taller de ciberseguridad');

-- ============================================================================
-- 31. RSU CONCURSOS
-- ============================================================================
INSERT INTO rsu_concurso (id_proyecto, nombre, criterios, fecha, ganadores) VALUES
(1, 'Concurso de paginas web con impacto social', 'Diseno, funcionalidad, uso de IA y relevancia social', '2025-05-08', 'Equipo Alfa - "Web de apoyo escolar"'),
(4, 'Concurso de ideas de reciclaje tecnologico', 'Innovacion, viabilidad e impacto ambiental', '2025-05-30', 'Equipo Beta - "Centro de acopio digital"');

-- ============================================================================
-- 32. FACULTADES, ESCUELAS, AULAS, LABORATORIOS, AMBIENTES
-- ============================================================================
INSERT INTO facultades (nombre_facultad) VALUES
('EPIS - Escuela Profesional de Ingenieria de Sistemas');

INSERT INTO escuelas (id_facultad, nombre_escuela) VALUES
(1, 'Escuela Profesional de Ingenieria de Sistemas');

INSERT INTO aulas (id_escuela, codigo_aula, descripcion) VALUES
(1, 'P-301', 'Aula tercer piso pabellon P - 40 carpetas'),
(1, 'P-307', 'Aula tercer piso pabellon P - 35 carpetas'),
(1, 'P-204', 'Aula segundo piso pabellon P - 40 carpetas'),
(1, 'P-101', 'Aula primer piso pabellon P - 50 carpetas'),
(1, 'P-105', 'Aula primer piso pabellon P - 45 carpetas');

INSERT INTO laboratorios (id_escuela, codigo_laboratorio, descripcion) VALUES
(1, 'LAB-A', 'Laboratorio de Computo A - 25 equipos'),
(1, 'LAB-B', 'Laboratorio de Computo B - 30 equipos'),
(1, 'LAB-C', 'Laboratorio de Computo C - 20 equipos'),
(1, 'LAB-D', 'Laboratorio de Hardware - 15 estaciones de trabajo'),
(1, 'LAB-REDES', 'Laboratorio de Redes y Comunicaciones');

INSERT INTO ambientes_generales (nombre_ambiente, tipo_ambiente) VALUES
('Cancha EPIS', 'Cancha'),
('Sala de reuniones EPIS', 'Sala'),
('Sala de Lectura EPIS', 'Sala'),
('Biblioteca Central UPT', 'Biblioteca'),
('Auditorio EPIS', 'Auditorio'),
('Patio principal', 'Patio'),
('Comedor universitario', 'Otro'),
('Oficina de Bienestar Universitario', 'Administracion');

-- ============================================================================
-- 33. USUARIOS (1 por rol: ADMIN, TUTOR, DOCENTE, ALUMNO)
-- ============================================================================
INSERT INTO usuarios (nombre, apellidos, tipo_usuario, codigo_alumno, documento, telefono, email) VALUES
('Ivan',         'Quispe Huallpa',  'ADMIN',  NULL,         '2023078707', '900000001', 'iq2023078707@virtual.upt.pe'),
('Patricia',     'Salas Gutierrez', 'TUTOR',  NULL,         '45678892',   '981222335', 'tutor@virtual.upt.pe'),
('Ana',          'Torres Mamani',   'DOCENTE', NULL,        '45678891',   '981222334', 'docente@virtual.upt.pe'),
('Sebastian',    'Cortez Mendoza',  'ALUMNO', '2023078888', '75677777',   '999999999', 'estudiante@virtual.upt.pe');

INSERT INTO usuarios_login (id_usuario, username, password_hash, activo) VALUES
(1, 'iq2023078707@virtual.upt.pe', '$2y$10$EDb0b9AI3WW6TsU/RYNUpetdtA1O77B8DkpEogvd3FaFvqTAXXc6y', 1),
(2, 'tutor@virtual.upt.pe',        '$2y$10$EDb0b9AI3WW6TsU/RYNUpetdtA1O77B8DkpEogvd3FaFvqTAXXc6y', 1),
(3, 'docente@virtual.upt.pe',      '$2y$10$EDb0b9AI3WW6TsU/RYNUpetdtA1O77B8DkpEogvd3FaFvqTAXXc6y', 1),
(4, 'estudiante@virtual.upt.pe',   '$2y$10$EDb0b9AI3WW6TsU/RYNUpetdtA1O77B8DkpEogvd3FaFvqTAXXc6y', 1);

-- ============================================================================
-- 34. CLASE_OBJETO
-- ============================================================================
INSERT INTO clase_objeto (nombre_clase) VALUES
('Electronico'), ('Ropa'), ('Documento'), ('Utiles'), ('Accesorios'), ('Otros');

-- ============================================================================
-- 35. OBJETOS PERDIDOS (25 objetos)
-- ============================================================================
INSERT INTO objetos
(codigo, nombre_objeto, descripcion, id_aula, id_laboratorio, id_ambiente_general,
 fecha_encontrado, persona_encontro, id_clase_objeto, estado)
VALUES
('0001', 'Celular Samsung Galaxy A54', 'Color negro, pantalla ligeramente trizada, funda transparente', NULL, 1, NULL, '2025-03-10', 'Luis Vilca', 1, 'No entregado'),
('0002', 'Casaca azul UPT', 'Casaca deportiva con logo UPT bordado en el pecho', 1, NULL, NULL, '2025-03-12', 'Carlos Mamani', 2, 'Entregado'),
('0003', 'DNI', 'DNI encontrado en la Sala de Lectura EPIS', NULL, NULL, 3, '2025-03-15', 'Luis Vilca', 3, 'No entregado'),
('0004', 'Cuaderno universitario', 'Cuaderno anillado con apuntes de cursos en la materia de Base de Datos', 2, NULL, NULL, '2025-03-18', 'Rosa Condori', 4, 'No entregado'),
('0005', 'Laptop Lenovo ThinkPad', 'Equipo gris con cargador incluido, stickers de Linux en la tapa', NULL, 2, NULL, '2025-03-20', 'Patricia Salas', 1, 'No entregado'),
('0006', 'Mochila negra Totto', 'Mochila con compartimiento para laptop, contiene utiles y botella de agua', 3, NULL, NULL, '2025-03-22', 'Jorge Condori', 5, 'Entregado'),
('0007', 'Carnet universitario', 'Carnet de estudiante de EPIS a nombre de Maria Fernandez', NULL, NULL, 4, '2025-03-25', 'Rosa Condori', 3, 'No entregado'),
('0008', 'Audifonos inalambricos', 'Audifonos Bluetooth blancos marca Huawei en estuche de carga', NULL, 3, NULL, '2025-03-28', 'Camila Flores', 1, 'Entregado'),
('0009', 'Libro de Algebra Lineal', 'Libro "Algebra Lineal y sus Aplicaciones" con anotaciones internas', 1, NULL, NULL, '2025-04-02', 'Mateo Perez', 4, 'No entregado'),
('0010', 'USB Kingston 32GB', 'Memoria USB color azul con llavero metalico', NULL, 1, NULL, '2025-04-05', 'Valeria Ramos', 1, 'No entregado'),
('0011', 'Calculadora Casio FX-991', 'Calculadora cientifica encontrada en el aula P-204 despues de examen parcial', 2, NULL, NULL, '2025-04-08', 'Ana Torres', 4, 'No entregado'),
('0012', 'Tomatodo deportivo UPT', 'Tomatodo azul con sticker del logo de Sistemas', NULL, NULL, 1, '2025-04-09', 'Jorge Condori', 6, 'No entregado'),
('0013', 'Tablet Samsung Galaxy Tab', 'Tablet gris con funda negra protectora, encontrada en laboratorio LAB-B', NULL, 2, NULL, '2025-04-11', 'Patricia Salas', 1, 'Entregado'),
('0014', 'Billetera marron', 'Billetera de cuero marron con carnets y tarjetas personales', NULL, NULL, 4, '2025-04-12', 'Rosa Condori', 5, 'No entregado'),
('0015', 'Audifonos Sony WH-CH510', 'Audifonos over-ear negros con cable de carga USB-C', NULL, 1, NULL, '2025-04-14', 'Carlos Mamani', 1, 'No entregado'),
('0016', 'Cargador de laptop HP', 'Cargador original HP de 65W con cable de poder', NULL, 1, NULL, '2025-04-16', 'Miguel Rojas', 1, 'No entregado'),
('0017', 'Lentes de sol', 'Lentes de sol marca Ray-Ban, color oscuro con marco dorado', NULL, NULL, 6, '2025-04-18', 'Valeria Ramos', 5, 'No entregado'),
('0018', 'Libro de Programacion Java', 'Libro "Java: Como programar" de Deitel, 10ma edicion', 2, NULL, NULL, '2025-04-20', 'Jorge Condori', 4, 'No entregado'),
('0019', 'Chaleco institucional UPT', 'Chaleco azul marino con logo UPT bordado', NULL, NULL, 3, '2025-04-22', 'Rosa Condori', 2, 'No entregado'),
('0020', 'Mouse inalambrico Logitech', 'Mouse inalambrico color gris con receptor USB incluido', NULL, 3, NULL, '2025-04-24', 'Carlos Mamani', 1, 'No entregado'),
('0021', 'Llavero con llaves', 'Llavero metalico con 3 llaves y un llavero de la UPT', 5, NULL, NULL, '2025-04-26', 'Patricia Salas', 5, 'No entregado'),
('0022', 'Tableta grafica Wacom', 'Tableta grafica marca Wacom Intuos Small, color negra con cable USB', NULL, 2, NULL, '2025-04-28', 'Ana Torres', 1, 'No entregado'),
('0023', 'Sombrilla plegable', 'Sombrilla automatica color negra con mango curvo', NULL, NULL, 6, '2025-04-30', 'Sebastian Cortez', 6, 'No entregado'),
('0024', 'Cable USB-C', 'Cable USB-C a USB-A de 1 metro, color blanco', NULL, 1, NULL, '2025-05-02', 'Luis Vilca', 1, 'No entregado'),
('0025', 'Buzo universitario', 'Buzo gris con capucha, talla M, con logo de EPIS', 4, NULL, NULL, '2025-05-04', 'Camila Flores', 2, 'No entregado');

-- ============================================================================
-- 36. HISTORIAL_OBJETO (registro de acciones)
-- ============================================================================
INSERT INTO historial_objeto (id_objeto, id_usuario, accion) VALUES
(1, 1, 'Registrado'),
(2, 3, 'Registrado'),
(2, 1, 'Entregado'),
(3, 4, 'Registrado'),
(4, 1, 'Registrado'),
(5, 3, 'Registrado'),
(6, 3, 'Registrado'),
(6, 1, 'Entregado'),
(7, 1, 'Registrado'),
(8, 4, 'Registrado'),
(8, 1, 'Entregado'),
(9, 4, 'Registrado'),
(10, 1, 'Registrado'),
(11, 3, 'Registrado'),
(12, 1, 'Registrado'),
(13, 3, 'Registrado'),
(13, 1, 'Entregado'),
(14, 1, 'Registrado'),
(15, 3, 'Registrado'),
(16, 1, 'Registrado'),
(17, 4, 'Registrado'),
(18, 1, 'Registrado'),
(19, 3, 'Registrado'),
(20, 1, 'Registrado'),
(21, 3, 'Registrado'),
(22, 1, 'Registrado'),
(23, 4, 'Registrado'),
(24, 1, 'Registrado'),
(25, 3, 'Registrado');

-- ============================================================================
-- 37. ENTREGA_OBJETO (entregas a duenos)
-- ============================================================================
INSERT INTO entrega_objeto
(id_objeto, id_usuario_entrego, tipo_receptor, nombre_receptor, apellidos_receptor,
 documento_receptor, codigo_estudiante, telefono, email)
VALUES
(2,  1, 'Alumno',   'Sebastian', 'Cortez',      '75677777', '2023078888', '999999999', 'scortez@upt.pe'),
(6,  1, 'Alumno',   'Camila',    'Flores',      '75670003', '2023078003', '903333333', 'cflores@upt.pe'),
(8,  1, 'Docente',  'Ana',       'Torres',      '45678891', NULL,         '981222334', 'atorres@upt.pe'),
(13, 1, 'Alumno',   'Gabriel',   'Huanca',      '75670006', '2023078006', '906666666', 'ghuanca@upt.pe');

-- Datos insertados correctamente.
