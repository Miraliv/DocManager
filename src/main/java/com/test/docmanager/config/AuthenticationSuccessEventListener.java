package com.test.docmanager.config;

import com.test.docmanager.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // Pega o e-mail (username) do usuário que acabou de logar
        String email = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        // Informa ao nosso serviço que ele logou com sucesso
        loginAttemptService.loginSucceeded(email);
    }
}