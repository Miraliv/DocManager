package com.test.docmanager.service;

import com.test.docmanager.model.User;
import com.test.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    // 5 tentativas falhas bloqueiam a conta
    private static final int MAX_FAILED_ATTEMPTS = 5;
    // Por 15 minutos
    private static final long LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    /**
     * Chamado quando o login é bem-sucedido.
     * Zera o contador de falhas e limpa o tempo de bloqueio.
     */
    public void loginSucceeded(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockExpiryTime(null);
            userRepository.save(user);
        });
    }

    /**
     * Chamado quando o login falha.
     * Incrementa o contador e, se atingir o limite, bloqueia a conta.
     */
    public void loginFailed(String email) {
        // Encontra o usuário PELO E-MAIL que foi tentado
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockExpiryTime(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            }
            userRepository.save(user);
        });
    }

    /**
     * Chamado no momento em que o usuário tenta logar (antes da verificação de senha).
     * Se a conta estava bloqueada mas o tempo já passou, desbloqueia.
     */
    public void checkAndUnlockUser(User user) {
        if (user.getLockExpiryTime() != null && user.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            user.setFailedLoginAttempts(0);
            user.setLockExpiryTime(null);
            userRepository.save(user);
        }
    }
}