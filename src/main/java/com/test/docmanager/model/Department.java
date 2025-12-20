package com.test.docmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set; // Importar

@Entity
@Data
@NoArgsConstructor
public class Department { // Era Secretaria

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    // Construtor para facilitar
    public Department(String nome) {
        this.nome = nome;
    }

    // Relação inversa (opcional, mas boa prática manter)
    // Mapeado pelo atributo "department" na classe User
    @OneToMany(mappedBy = "department")
    private Set<User> usuarios;
}