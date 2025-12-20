package com.test.docmanager.config;

import com.test.docmanager.model.Department;
import com.test.docmanager.model.Role;
import com.test.docmanager.model.User;
import com.test.docmanager.repository.DepartmentRepository;
import com.test.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Cria apenas a unidade raiz
            Department diretoria = departmentRepository.findDiretoria()
                    .orElseGet(() -> departmentRepository.save(new Department("Diretoria")));

            // Cria o Administrador Mestre
            User admin = new User();
            admin.setNome("Administrador Mestre");
            admin.setEmail("admin@docmanager.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setDepartment(diretoria);
            admin.setEnabled(true);
            userRepository.save(admin);

            System.out.println(">>> Sistema Inicializado. Crie novos departamentos via Painel Admin.");
        }
    }
}