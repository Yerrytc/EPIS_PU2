package com.upt.sistema.shared.validation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

public final class AppValidation {
    private static final Pattern LETRAS = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$");
    private static final Pattern TEXTO_SEGURO = Pattern.compile("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñüÜ.,;:()\\-_/°#&+%\"'¿?¡!\\s]+$");
    private static final Pattern DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern RUC = Pattern.compile("^\\d{11}$");
    private static final Pattern TELEFONO = Pattern.compile("^9\\d{8}$");
    private static final Pattern CODIGO_ESTUDIANTE = Pattern.compile("^\\d{10}$");
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CORREO_INSTITUCIONAL = Pattern.compile("^[A-Za-z0-9._%+-]+@virtual\\.upt\\.pe$");
    private static final Pattern HORA = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final Pattern CODIGO_SIMPLE = Pattern.compile("^[A-Za-z0-9\\-]{2,20}$");

    private AppValidation() {}

    public static String clean(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String upper(String value) {
        String clean = clean(value);
        return clean == null ? null : clean.toUpperCase(Locale.ROOT);
    }

    public static String requiredText(String field, String value, int min, int max) {
        String clean = clean(value);
        if (clean == null) throw new IllegalArgumentException(field + " es obligatorio.");
        if (clean.length() < min || clean.length() > max) {
            throw new IllegalArgumentException(field + " debe tener entre " + min + " y " + max + " caracteres.");
        }
        if (!TEXTO_SEGURO.matcher(clean).matches()) {
            throw new IllegalArgumentException(field + " contiene caracteres no permitidos.");
        }
        return clean;
    }

    public static String optionalText(String field, String value, int max) {
        String clean = clean(value);
        if (clean == null) return null;
        if (clean.length() > max) throw new IllegalArgumentException(field + " no debe superar " + max + " caracteres.");
        if (!TEXTO_SEGURO.matcher(clean).matches()) throw new IllegalArgumentException(field + " contiene caracteres no permitidos.");
        return clean;
    }

    public static String requiredLetters(String field, String value, int min, int max) {
        String clean = clean(value);
        if (clean == null) throw new IllegalArgumentException(field + " es obligatorio.");
        if (clean.length() < min || clean.length() > max) {
            throw new IllegalArgumentException(field + " debe tener entre " + min + " y " + max + " caracteres.");
        }
        if (!LETRAS.matcher(clean).matches()) throw new IllegalArgumentException(field + " solo debe contener letras y espacios.");
        return clean;
    }

    public static String optionalLetters(String field, String value, int max) {
        String clean = clean(value);
        if (clean == null) return null;
        if (clean.length() > max) throw new IllegalArgumentException(field + " no debe superar " + max + " caracteres.");
        if (!LETRAS.matcher(clean).matches()) throw new IllegalArgumentException(field + " solo debe contener letras y espacios.");
        return clean;
    }

    public static String requiredDni(String value) {
        String clean = clean(value);
        if (clean == null || !DNI.matcher(clean).matches()) {
            throw new IllegalArgumentException("El DNI/documento debe tener exactamente 8 digitos numericos.");
        }
        return clean;
    }

    public static String optionalDniOrRuc(String field, String value) {
        String clean = clean(value);
        if (clean == null) return null;
        if (!DNI.matcher(clean).matches() && !RUC.matcher(clean).matches()) {
            throw new IllegalArgumentException(field + " debe tener 8 digitos si es DNI o 11 digitos si es RUC.");
        }
        return clean;
    }

    public static String requiredPhone(String value) {
        String clean = clean(value);
        if (clean == null || !TELEFONO.matcher(clean).matches()) {
            throw new IllegalArgumentException("El telefono debe tener exactamente 9 digitos y empezar con 9.");
        }
        return clean;
    }

    public static String optionalPhone(String value) {
        String clean = clean(value);
        if (clean == null) return null;
        if (!TELEFONO.matcher(clean).matches()) {
            throw new IllegalArgumentException("El telefono debe tener exactamente 9 digitos y empezar con 9.");
        }
        return clean;
    }

    public static String requiredStudentCode(String value) {
        String clean = clean(value);
        if (clean == null || !CODIGO_ESTUDIANTE.matcher(clean).matches()) {
            throw new IllegalArgumentException("El codigo de estudiante debe tener exactamente 10 digitos numericos.");
        }
        return clean;
    }

    public static String requiredEmail(String field, String value) {
        String clean = clean(value);
        if (clean == null || !CORREO.matcher(clean).matches()) throw new IllegalArgumentException(field + " debe tener formato de correo valido.");
        return clean.toLowerCase(Locale.ROOT);
    }

    public static String optionalEmail(String field, String value) {
        String clean = clean(value);
        if (clean == null) return null;
        if (!CORREO.matcher(clean).matches()) throw new IllegalArgumentException(field + " debe tener formato de correo valido.");
        return clean.toLowerCase(Locale.ROOT);
    }

    public static String requiredInstitutionalEmail(String field, String value) {
        String clean = clean(value);
        if (clean == null || !CORREO_INSTITUCIONAL.matcher(clean).matches()) {
            throw new IllegalArgumentException(field + " debe terminar en @virtual.upt.pe.");
        }
        return clean.toLowerCase(Locale.ROOT);
    }

    public static LocalDate requiredDate(String field, String value) {
        String clean = clean(value);
        if (clean == null) throw new IllegalArgumentException(field + " es obligatorio.");
        try {
            return LocalDate.parse(clean);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " debe tener una fecha valida.");
        }
    }

    public static LocalDate dateNotFuture(String field, String value) {
        LocalDate date = requiredDate(field, value);
        if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException(field + " no puede ser una fecha futura.");
        return date;
    }

    public static LocalDate dateNotPast(String field, String value) {
        LocalDate date = requiredDate(field, value);
        if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException(field + " no puede ser una fecha pasada.");
        return date;
    }

    public static String requiredHour(String field, String value) {
        String clean = clean(value);
        if (clean == null || !HORA.matcher(clean).matches()) throw new IllegalArgumentException(field + " debe tener formato HH:mm.");
        try {
            LocalTime.parse(clean);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " debe tener una hora valida.");
        }
        return clean;
    }

    public static Long positiveId(String field, Long value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("Debe seleccionar " + field + ".");
        return value;
    }

    public static Integer positiveId(String field, Integer value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("Debe seleccionar " + field + ".");
        return value;
    }

    public static Double range(String field, Double value, double min, double max) {
        if (value == null || value < min || value > max) {
            throw new IllegalArgumentException(field + " debe estar entre " + min + " y " + max + ".");
        }
        return value;
    }

    public static Integer range(String field, Integer value, int min, int max) {
        if (value == null || value < min || value > max) {
            throw new IllegalArgumentException(field + " debe estar entre " + min + " y " + max + ".");
        }
        return value;
    }

    public static String oneOf(String field, String value, String... allowed) {
        String clean = clean(value);
        if (clean == null) throw new IllegalArgumentException("Debe seleccionar " + field + ".");
        for (String option : allowed) {
            if (option.equalsIgnoreCase(clean)) return option;
        }
        throw new IllegalArgumentException(field + " invalido. Valores permitidos: " + String.join(", ", allowed) + ".");
    }

    public static String requiredCode(String field, String value) {
        String clean = clean(value);
        if (clean == null || !CODIGO_SIMPLE.matcher(clean).matches()) {
            throw new IllegalArgumentException(field + " debe tener de 2 a 20 caracteres alfanumericos, sin espacios.");
        }
        return clean.toUpperCase(Locale.ROOT);
    }

    public static void ensureExists(JdbcTemplate jdbc, String table, String column, Object value, String name) {
        if (value == null) throw new IllegalArgumentException("Debe seleccionar " + name + ".");
        String tableSafe = safeTable(table);
        String columnSafe = safeColumn(column);
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + tableSafe + " WHERE " + columnSafe + " = ?", Integer.class, value);
        if (count == null || count == 0) throw new IllegalArgumentException(name + " no existe o no esta registrado.");
    }

    private static String safeTable(String table) {
        String[] allowed = {
            "estudiante", "curso", "docente", "tipo_intervencion", "estado_intervencion", "intervencion",
            "rsu_proyecto", "rsu_actividad", "tutoria_justificacion", "escuelas", "aulas", "laboratorios",
            "ambientes_generales", "clase_objeto"
        };
        if (Arrays.asList(allowed).contains(table)) return table;
        throw new IllegalArgumentException("Tabla no permitida para validacion.");
    }

    private static String safeColumn(String column) {
        String[] allowed = {
            "codigo", "id_curso", "id_docente", "id_tipo", "codigo", "id_intervencion", "id_proyecto",
            "id_actividad", "id_justificacion", "id_escuela", "id_aula", "id_laboratorio", "id_ambiente", "id_clase_objeto"
        };
        if (Arrays.asList(allowed).contains(column)) return column;
        throw new IllegalArgumentException("Columna no permitida para validacion.");
    }
}
