package com.test.docmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN ou MANAGER

    // --- MUDANÇA: Agora aponta para Department ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    // --- Campos de Fluxo de Senha ---
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    private boolean enabled = false;

    // --- Campos de Proteção contra Força Bruta ---
    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "lock_expiry_time")
    private LocalDateTime lockExpiryTime;


    // --- Implementação UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna ROLE_ADMIN ou ROLE_MANAGER
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    // Lógica de bloqueio por força bruta
    @Override
    public boolean isAccountNonLocked() {
        if (this.lockExpiryTime != null) {
            if (this.lockExpiryTime.isBefore(LocalDateTime.now())) {
                return true; // Tempo passou, desbloqueia
            }
            return false; // Ainda bloqueado
        }
        return true; // Nunca foi bloqueado
    }
}