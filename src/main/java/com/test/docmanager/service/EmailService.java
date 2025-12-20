package com.test.docmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Envia o e-mail de convite para criar a senha
    public void enviarEmailConvite(String paraEmail, String token) {
        String assunto = "Bem-vindo ao Sistema de Documentos!";
        String url = "http://localhost:8080/definir-senha?token=" + token;
        String mensagem = "Olá,\n\nVocê foi convidado para acessar o Sistema de Documentos da Prefeitura.\n\n"
                + "Clique no link abaixo para definir sua senha e ativar sua conta:\n" + url
                + "\n\nEste link expira em 24 horas.";

        enviarEmail(paraEmail, assunto, mensagem);
    }

    // Envia o e-mail de redefinição de senha
    public void enviarEmailResetSenha(String paraEmail, String token) {
        String assunto = "Redefinição de Senha - Sistema de Documentos";
        String url = "http://localhost:8080/resetar-senha?token=" + token;
        String mensagem = "Olá,\n\nRecebemos uma solicitação para redefinir sua senha.\n\n"
                + "Clique no link abaixo para criar uma nova senha:\n" + url
                + "\n\nSe você não solicitou isso, por favor, ignore este e-mail."
                + "\nEste link expira em 1 hora.";

        enviarEmail(paraEmail, assunto, mensagem);
    }

    private void enviarEmail(String para, String assunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(para);
            message.setSubject(assunto);
            message.setText(texto);
            // Defina o e-mail "de" (opcional, pode ser pego do properties)
            message.setFrom("nao-responda@prefeitura.com");

            mailSender.send(message);
        } catch (Exception e) {
            // Em um app real, aqui deveria ter um log mais robusto
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            // Lançar uma exceção customizada seria o ideal
            throw new RuntimeException("Erro ao enviar e-mail.", e);
        }
    }
}