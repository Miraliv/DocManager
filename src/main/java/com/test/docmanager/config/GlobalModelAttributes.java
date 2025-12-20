package com.test.docmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.time.Duration;

/**
 * Esta classe injeta atributos globais em todos os models
 * antes que as páginas sejam renderizadas.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final long sessionTimeoutInSeconds;

    /**
     * O Spring Boot é inteligente o suficiente para injetar a string "15m"
     * (ou "1m") do properties diretamente em um objeto Duration.
     */
    public GlobalModelAttributes(@Value("${server.servlet.session.timeout}") Duration sessionTimeout) {
        // Nós então convertemos para segundos e armazenamos.
        this.sessionTimeoutInSeconds = sessionTimeout.toSeconds();
    }

    /**
     * Este método anotação @ModelAttribute garante que a variável
     * "sessionTimeoutInSeconds" esteja disponível em TODOS os templates Thymeleaf.
     */
    @ModelAttribute("sessionTimeoutInSeconds")
    public long getSessionTimeoutInSeconds() {
        return this.sessionTimeoutInSeconds;
    }
}