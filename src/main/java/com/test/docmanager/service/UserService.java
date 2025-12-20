package com.test.docmanager.service;

import com.test.docmanager.model.Department;
import com.test.docmanager.model.Role;
import com.test.docmanager.model.User;
import com.test.docmanager.repository.DepartmentRepository;
import com.test.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Cria um novo Gerente de Departamento (antigo Secretário)
     */
    public void criarNovoGestor(String nome, String email, Long departmentId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado."));

        User novoUsuario = new User();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setDepartment(department);
        novoUsuario.setRole(Role.MANAGER);
        novoUsuario.setEnabled(false);

        String token = gerarToken();
        novoUsuario.setResetToken(token);
        novoUsuario.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        userRepository.save(novoUsuario);

        emailService.enviarEmailConvite(email, token);
    }

    public void iniciarProcessoResetSenha(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            System.err.println("Tentativa de reset bloqueada para usuário inativo: " + email);
            return;
        }

        String token = gerarToken();
        user.setResetToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        emailService.enviarEmailResetSenha(email, token);
    }

    public User validarToken(String token) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado."));

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            user.setResetToken(null);
            user.setTokenExpiryDate(null);
            userRepository.save(user);
            throw new RuntimeException("Token inválido ou expirado.");
        }

        return user;
    }

    public void definirSenha(String token, String novaSenha) {
        User user = validarToken(token);

        user.setSenha(passwordEncoder.encode(novaSenha));
        user.setEnabled(true);
        user.setResetToken(null);
        user.setTokenExpiryDate(null);

        userRepository.save(user);
    }

    public void revogarConvite(Long usuarioId) {
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.isEnabled()) {
            throw new RuntimeException("Este usuário já está ativo.");
        }

        user.setResetToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);
    }

    public void alternarStatusUsuario(Long usuarioId) {
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.getSenha() == null) {
            throw new RuntimeException("Este usuário ainda não definiu uma senha.");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public void reenviarConvite(Long usuarioId) {
        User user = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (user.isEnabled() || user.getSenha() != null) {
            throw new RuntimeException("Ação não permitida. O usuário já está ativo.");
        }

        String token = gerarToken();
        user.setResetToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.enviarEmailConvite(user.getEmail(), token);
    }

    private String gerarToken() {
        return UUID.randomUUID().toString();
    }
}