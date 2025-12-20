package com.test.docmanager.controller;

import com.test.docmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;

    // --- CORREÇÃO DEFINITIVA DO REGEX ---
    // Agora ele exige minúscula, maiúscula, número, e
    // "qualquer caractere que não seja letra ou número".
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
    // --- FIM DA CORREÇÃO ---


    // --- Fluxo "Esqueci a Senha" ---

    @GetMapping("/esqueci-senha")
    public String esqueciSenhaForm() {
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String handleEsqueciSenha(@RequestParam("email") String email, RedirectAttributes redirect) {
        try {
            userService.iniciarProcessoResetSenha(email);
            redirect.addFlashAttribute("sucesso", "Se um usuário com este e-mail existir, um link de redefinição foi enviado.");
        } catch (Exception e) {
            redirect.addFlashAttribute("sucesso", "Se um usuário com este e-mail existir, um link de redefinição foi enviado.");
        }
        return "redirect:/login";
    }

    // --- Fluxo Comum (Resetar ou Definir) ---

    @GetMapping("/definir-senha")
    public String definirSenhaForm(@RequestParam("token") String token, Model model) {
        try {
            userService.validarToken(token);
            model.addAttribute("token", token);
            model.addAttribute("actionUrl", "/definir-senha");
            model.addAttribute("pageTitle", "Defina sua Senha");
            return "resetar-senha";
        } catch (Exception e) {
            model.addAttribute("erro", "Token inválido ou expirado.");
            return "login";
        }
    }

    @GetMapping("/resetar-senha")
    public String resetarSenhaForm(@RequestParam("token") String token, Model model) {
        try {
            userService.validarToken(token);
            model.addAttribute("token", token);
            model.addAttribute("actionUrl", "/resetar-senha");
            model.addAttribute("pageTitle", "Redefina sua Senha");
            return "resetar-senha";
        } catch (Exception e) {
            model.addAttribute("erro", "Token inválido ou expirado.");
            return "login";
        }
    }

    @PostMapping("/definir-senha")
    public String handleDefinirSenha(@RequestParam String token,
                                     @RequestParam String senha,
                                     @RequestParam String confirmarSenha,
                                     RedirectAttributes redirect) {

        String erroValidacao = validarSenha(senha, confirmarSenha);
        if (erroValidacao != null) {
            redirect.addFlashAttribute("erro", erroValidacao);
            return "redirect:/definir-senha?token=" + token;
        }

        try {
            userService.definirSenha(token, senha);
            redirect.addFlashAttribute("sucesso", "Senha definida com sucesso! Faça seu login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Token inválido ou expirado.");
            return "redirect:/login";
        }
    }

    @PostMapping("/resetar-senha")
    public String handleResetarSenha(@RequestParam String token,
                                     @RequestParam String senha,
                                     @RequestParam String confirmarSenha,
                                     RedirectAttributes redirect) {

        String erroValidacao = validarSenha(senha, confirmarSenha);
        if (erroValidacao != null) {
            redirect.addFlashAttribute("erro", erroValidacao);
            return "redirect:/resetar-senha?token=" + token;
        }

        try {
            userService.definirSenha(token, senha);
            redirect.addAttribute("sucesso", true);
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Token inválido ou expirado.");
            return "redirect:/login";
        }
    }

    // Método utilitário de validação
    private String validarSenha(String senha, String confirmarSenha) {
        if (!senha.equals(confirmarSenha)) {
            return "As senhas não conferem.";
        }

        // --- MENSAGEM DE ERRO ATUALIZADA ---
        if (!senha.matches(PASSWORD_REGEX)) {
            return "A senha deve ter no mínimo 8 caracteres, incluindo uma letra maiúscula, " +
                    "uma minúscula, um número e um caractere especial (ex: @, #, _, -).";
        }
        // --- FIM DA MUDANÇA ---

        return null; // Sem erros
    }
}