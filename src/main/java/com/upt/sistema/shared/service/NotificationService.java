package com.upt.sistema.shared.service;

import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private final JavaMailSender emailSender;
    private final JdbcTemplate jdbc;
    private final Environment environment;
    private final String from;

    public NotificationService(JavaMailSender emailSender,
                               JdbcTemplate jdbc,
                               Environment environment,
                               @Value("${app.mail.from:noreply@upt.pe}") String from) {
        this.emailSender = emailSender;
        this.jdbc = jdbc;
        this.environment = environment;
        this.from = from;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        validateRecipient(to);
        JavaMailSender sender = resolveSender();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(resolveFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String html) {
        try {
            validateRecipient(to);
            JavaMailSender sender = resolveSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(resolveFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        }
    }

    private JavaMailSender resolveSender() {
        String envUsername = environment.getProperty("spring.mail.username", "");
        String envPassword = environment.getProperty("spring.mail.password", "");
        if (StringUtils.hasText(envUsername) && StringUtils.hasText(envPassword)) {
            return emailSender;
        }

        String dbUsername = getConfig("smtp_username", "");
        String dbPassword = getConfig("smtp_password", "");
        if (!StringUtils.hasText(dbUsername) || !StringUtils.hasText(dbPassword)) {
            return emailSender;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(getConfig("smtp_host", environment.getProperty("spring.mail.host", "smtp.gmail.com")));
        sender.setPort(Integer.parseInt(getConfig("smtp_port", environment.getProperty("spring.mail.port", "587"))));
        sender.setUsername(dbUsername);
        sender.setPassword(dbPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", getConfig("smtp_tls", "true"));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }

    private String resolveFrom() {
        String dbFrom = getConfig("smtp_from", "");
        return StringUtils.hasText(dbFrom) ? dbFrom : from;
    }

    private String getConfig(String clave, String defecto) {
        var rows = jdbc.queryForList("SELECT valor FROM sistema_configuracion WHERE clave = ?", String.class, clave);
        return rows.isEmpty() || !StringUtils.hasText(rows.get(0)) ? defecto : rows.get(0);
    }

    private void validateRecipient(String to) {
        if (!StringUtils.hasText(to)) {
            throw new IllegalArgumentException("Debe indicar un correo destino.");
        }
    }
}
