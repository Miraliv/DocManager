package com.test.docmanager.controller;

import com.test.docmanager.model.Department;
import com.test.docmanager.model.User;
import com.test.docmanager.repository.DepartmentRepository;
import com.test.docmanager.repository.UserRepository;
import com.test.docmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Role Corporativa
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        // Lista todos exceto o próprio Admin
        List<User> usuarios = userRepository.findAll().stream()
                .filter(user -> user.getRole() != com.test.docmanager.model.Role.ADMIN)
                .toList();
        model.addAttribute("usuarios", usuarios);
        return "admin-usuarios";
    }

    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("departamentos", departmentRepository.findAll());
        return "admin-usuarios-form";
    }

    @PostMapping("/usuarios/novo")
    public String salvarNovoUsuario(@RequestParam String nome,
                                    @RequestParam String email,
                                    @RequestParam Long departmentId,
                                    RedirectAttributes redirect) {
        try {
            userService.criarNovoGestor(nome, email, departmentId);
            redirect.addFlashAttribute("sucesso", "Convite enviado com sucesso para " + email);
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Falha ao criar usuário: " + e.getMessage());
            return "redirect:/admin/usuarios/novo";
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/revogar/{id}")
    public String revogarConvite(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            userService.revogarConvite(id);
            redirect.addFlashAttribute("sucesso", "Convite revogado com sucesso.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Falha ao revogar convite: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/status/{id}")
    public String alternarStatus(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            userService.alternarStatusUsuario(id);
            redirect.addFlashAttribute("sucesso", "Status do usuário alterado com sucesso.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Falha ao alterar status: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/reenviar/{id}")
    public String reenviarConvite(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            userService.reenviarConvite(id);
            redirect.addFlashAttribute("sucesso", "Novo convite enviado com sucesso.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Falha ao reenviar convite: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
    @GetMapping("/departamentos")
    public String listarDepartamentos(Model model) {
        model.addAttribute("departamentos", departmentRepository.findAll());
        return "admin-departamentos";
    }

    @GetMapping("/departamentos/novo")
    public String novoDepartamentoForm(Model model) {
        model.addAttribute("department", new Department());
        return "admin-departamento-form";
    }

    @PostMapping("/departamentos/salvar")
    public String salvarDepartamento(@ModelAttribute Department department, RedirectAttributes redirect) {
        try {
            departmentRepository.save(department);
            redirect.addFlashAttribute("sucesso", "Departamento salvo com sucesso!");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Erro ao salvar: Nome já existe.");
        }
        return "redirect:/admin/departamentos";
    }

    @GetMapping("/departamentos/editar/{id}")
    public String editarDepartamento(@PathVariable Long id, Model model) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Não encontrado"));
        model.addAttribute("department", dept);
        return "admin-departamento-form";
    }

    @PostMapping("/departamentos/excluir/{id}")
    public String excluirDepartamento(@PathVariable Long id, RedirectAttributes redirect) {
        // Verificação de segurança: não deixar excluir a Diretoria
        Department dept = departmentRepository.findById(id).orElseThrow();
        if ("Diretoria".equals(dept.getNome())) {
            redirect.addFlashAttribute("erro", "A Diretoria é uma unidade protegida e não pode ser excluída.");
        } else {
            departmentRepository.deleteById(id);
            redirect.addFlashAttribute("sucesso", "Departamento removido.");
        }
        return "redirect:/admin/departamentos";
    }
}