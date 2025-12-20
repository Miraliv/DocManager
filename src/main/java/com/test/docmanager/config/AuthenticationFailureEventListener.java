package com.test.docmanager.config;

import com.test.docmanager.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureEventListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        // Pega o e-mail (principal) que foi usado na tentativa de login
        String email = (String) event.getAuthentication().getPrincipal();
        // Informa ao nosso serviço que houve uma falha
        loginAttemptService.loginFailed(email);
    }
}