package com.test.docmanager.model;

public enum TipoDocumento {
    DOCUMENTO_TEXTO("Documento de Texto"),
    PLANILHA("Planilha"),
    APRESENTACAO("Apresentação"),
    IMAGEM("Imagem"),
    PDF("Arquivo PDF"),
    RELATORIO("Relatório"),
    CONTRATO("Contrato"),
    OUTRO("Outro");

    private final String displayName;

    TipoDocumento(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}