package com.test.docmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    private LocalDateTime dataUpload;

    @Column(nullable = false, unique = true)
    private String caminhoArquivo;

    private String nomeArquivoOriginal;

    private String contentType;

    // --- MUDANÇA: Agora aponta para Department ---
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_upload_id")
    private User usuarioUpload;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    private LocalDateTime deletionDate;
}