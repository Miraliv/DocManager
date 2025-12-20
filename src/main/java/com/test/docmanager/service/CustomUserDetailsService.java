package com.test.docmanager.service;

import com.test.docmanager.model.User;
import com.test.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    // Injetando o novo serviço
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));

        // --- NOVA LÓGICA DE DESBLOQUEIO ---
        // Antes de retornar o usuário para o Spring Security,
        // verificamos se ele estava bloqueado e se o tempo já passou.
        loginAttemptService.checkAndUnlockUser(user);
        // --- FIM DA NOVA LÓGICA ---

        // O Spring Security irá então verificar user.isAccountNonLocked()
        return user;
    }
}