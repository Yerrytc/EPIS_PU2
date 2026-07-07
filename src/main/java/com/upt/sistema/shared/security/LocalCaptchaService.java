package com.upt.sistema.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LocalCaptchaService {

    public static final String CAPTCHA_CHALLENGES_SESSION_KEY = "LOGIN_CAPTCHA_CHALLENGES";
    private static final int MAX_ACTIVE_CAPTCHAS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    public CaptchaChallenge generate(HttpSession session) {
        int left = secureRandom.nextInt(9) + 1;
        int right = secureRandom.nextInt(9) + 1;
        String id = UUID.randomUUID().toString();
        String question = left + " + " + right + " = ?";
        activeCaptchas(session).put(id, String.valueOf(left + right));
        pruneOldCaptchas(session);
        return new CaptchaChallenge(id, question);
    }

    public boolean validate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String captchaId = request.getParameter("captchaId");
        if (!StringUtils.hasText(captchaId)) {
            return false;
        }

        Map<String, String> captchas = activeCaptchas(session);
        String expected = captchas.remove(captchaId);

        String submitted = request.getParameter("captcha");
        return expected != null
                && StringUtils.hasText(submitted)
                && expected.equals(submitted.replaceAll("\\s+", ""));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> activeCaptchas(HttpSession session) {
        Object value = session.getAttribute(CAPTCHA_CHALLENGES_SESSION_KEY);
        if (value instanceof Map<?, ?>) {
            return (Map<String, String>) value;
        }

        Map<String, String> captchas = new LinkedHashMap<>();
        session.setAttribute(CAPTCHA_CHALLENGES_SESSION_KEY, captchas);
        return captchas;
    }

    private void pruneOldCaptchas(HttpSession session) {
        Map<String, String> captchas = activeCaptchas(session);
        while (captchas.size() > MAX_ACTIVE_CAPTCHAS) {
            String firstKey = captchas.keySet().iterator().next();
            captchas.remove(firstKey);
        }
    }

    public record CaptchaChallenge(String id, String question) {
    }
}
