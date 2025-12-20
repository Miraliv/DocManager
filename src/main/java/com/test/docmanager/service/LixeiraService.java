package com.test.docmanager.service;

import com.test.docmanager.model.Documento;
import com.test.docmanager.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Para logs
public class LixeiraService {

    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;
    private static final int DIAS_PARA_EXPURGO = 30;

    /**
     * Roda todo dia, às 02:00 da manhã.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expurgarLixeiraAntiga() {
        log.info("Iniciando tarefa agendada: Expurgar Lixeira...");

        LocalDateTime dataLimite = LocalDateTime.now().minusDays(DIAS_PARA_EXPURGO);

        List<Documento> paraExpurgar = documentoRepository.findAllDeletedBefore(dataLimite);

        if (paraExpurgar.isEmpty()) {
            log.info("Nenhum documento antigo encontrado na lixeira. Tarefa concluída.");
            return;
        }

        log.warn("Encontrados {} documentos para exclusão permanente.", paraExpurgar.size());

        for (Documento doc : paraExpurgar) {
            try {
                fileStorageService.deletarArquivo(doc.getCaminhoArquivo());
                documentoRepository.delete(doc);
                log.info("Documento expurgado com sucesso: ID {}", doc.getId());

            } catch (Exception e) {
                log.error("Falha ao expurgar documento ID {}: {}", doc.getId(), e.getMessage());
            }
        }
        log.info("Tarefa de expurgo da lixeira concluída.");
    }
}