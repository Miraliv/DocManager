package com.test.docmanager.controller;

import com.test.docmanager.model.Documento;
import com.test.docmanager.model.Role;
import com.test.docmanager.model.Department;
import com.test.docmanager.model.TipoDocumento;
import com.test.docmanager.model.User;
import com.test.docmanager.repository.DocumentoRepository;
import com.test.docmanager.repository.DepartmentRepository;
import com.test.docmanager.repository.UserRepository;
import com.test.docmanager.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoRepository documentoRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Filtro base para documentos não eliminados
    private Specification<Documento> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    // Helper para procurar documentos ativos
    private Documento findDocumentoAtivoById(Long id) {
        return documentoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado ou está na lixeira."));
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (user.getRole() == Role.ADMIN) {
            Department diretoria = departmentRepository.findDiretoria().orElse(null);
            List<Department> outros = departmentRepository.findOutrosDepartamentos();

            Map<Long, Long> contagemDocs = outros.stream()
                    .collect(Collectors.toMap(Department::getId, documentoRepository::countByDepartment));

            long contagemDiretoria = (diretoria != null) ? documentoRepository.countByDepartment(diretoria) : 0;

            model.addAttribute("diretoria", diretoria);
            model.addAttribute("outrosDepartamentos", outros);
            model.addAttribute("contagemDocs", contagemDocs);
            model.addAttribute("contagemDiretoria", contagemDiretoria);
            model.addAttribute("usuario", user);
            return "admin-dashboard";
        } else {
            Department dept = user.getDepartment();
            Specification<Documento> spec = isNotDeleted()
                    .and((root, query, cb) -> cb.equal(root.get("department"), dept));

            List<Documento> documentos = documentoRepository.findAll(spec);

            Map<TipoDocumento, Long> contagemPorTipo = documentos.stream()
                    .collect(Collectors.groupingBy(Documento::getTipoDocumento, Collectors.counting()));

            List<Documento> recentes = documentoRepository.findTop5ByDepartmentOrderByDataUploadDesc(dept);

            model.addAttribute("contagemPorTipo", contagemPorTipo);
            model.addAttribute("recentes", recentes);
            model.addAttribute("totalDocumentos", documentos.size());
            model.addAttribute("nomeDepartamento", dept.getNome());
            model.addAttribute("usuario", user);
            return "manager-dashboard";
        }
    }

    @GetMapping("/documentos/categoria/{tipo}")
    public String verDocumentosPorCategoria(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable TipoDocumento tipo,
            @RequestParam(required = false) Long departmentId
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Department departmentAlvo;

        if (user.getRole() == Role.ADMIN && departmentId != null) {
            departmentAlvo = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new AccessDeniedException("Departamento não encontrado."));
        } else {
            departmentAlvo = user.getDepartment();
        }

        List<Documento> documentos = documentoRepository.findByDepartmentAndTipoDocumento(departmentAlvo, tipo);
        model.addAttribute("documentos", documentos);
        model.addAttribute("tituloPagina", "Categoria: " + tipo.getDisplayName());
        model.addAttribute("department", departmentAlvo);
        model.addAttribute("isAdminView", (user.getRole() == Role.ADMIN));
        return "documentos-lista";
    }

    @GetMapping("/admin/department/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String verDepartamentoComoAdmin(Model model, @PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

        Specification<Documento> spec = isNotDeleted()
                .and((root, query, cb) -> cb.equal(root.get("department"), dept));

        List<Documento> documentos = documentoRepository.findAll(spec);
        Map<TipoDocumento, Long> contagemPorTipo = documentos.stream()
                .collect(Collectors.groupingBy(Documento::getTipoDocumento, Collectors.counting()));

        List<Documento> recentes = documentoRepository.findTop5ByDepartmentOrderByDataUploadDesc(dept);

        model.addAttribute("contagemPorTipo", contagemPorTipo);
        model.addAttribute("recentes", recentes);
        model.addAttribute("department", dept);
        return "admin-ver-departamento";
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        // Mudado de "tipos" para "tiposDocumento" para bater com o upload.html
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "upload";
    }

    @PostMapping("/upload")
    public String realizarUpload(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("titulo") String titulo,
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            Department deptDestino = user.getDepartment();

            if (deptDestino == null) {
                throw new RuntimeException("Utilizador não associado a um departamento.");
            }

            // Correção: Usar o nome correto do método no FileStorageService
            String nomeArquivoUnico = fileStorageService.salvarArquivo(arquivo);

            Documento doc = new Documento();
            doc.setTitulo(titulo);
            doc.setTipoDocumento(tipoDocumento);
            doc.setDataUpload(LocalDateTime.now());
            doc.setCaminhoArquivo(nomeArquivoUnico);
            doc.setNomeArquivoOriginal(arquivo.getOriginalFilename());
            doc.setContentType(arquivo.getContentType());
            doc.setDepartment(deptDestino);
            doc.setUsuarioUpload(user);

            documentoRepository.save(doc);
            redirectAttributes.addFlashAttribute("sucesso", "Upload realizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro no upload: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/documentos/view/{id}")
    public ResponseEntity<Resource> viewDocumento(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = findDocumentoAtivoById(id);

        if (user.getRole() != Role.ADMIN && !doc.getDepartment().getId().equals(user.getDepartment().getId())) {
            throw new AccessDeniedException("Sem permissão para visualizar este documento.");
        }

        Resource resource = fileStorageService.carregarArquivo(doc.getCaminhoArquivo());
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";

        String headerValue = ("application/pdf".equals(contentType))
                ? "inline; filename=\"" + doc.getNomeArquivoOriginal() + "\""
                : "attachment; filename=\"" + doc.getNomeArquivoOriginal() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @GetMapping("/documentos/download/{id}")
    public ResponseEntity<Resource> downloadDocumento(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = findDocumentoAtivoById(id);

        if (user.getRole() != Role.ADMIN && !doc.getDepartment().getId().equals(user.getDepartment().getId())) {
            throw new AccessDeniedException("Sem permissão para descarregar este documento.");
        }

        Resource resource = fileStorageService.carregarArquivo(doc.getCaminhoArquivo());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNomeArquivoOriginal() + "\"")
                .body(resource);
    }

    @GetMapping("/documentos/editar/{id}")
    public String editarDocumentoForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = findDocumentoAtivoById(id);

        boolean isOwner = doc.getUsuarioUpload().getId().equals(user.getId());
        boolean isAdminInDiretoria = user.getRole() == Role.ADMIN && doc.getDepartment().getNome().equals("Diretoria");

        if (!isOwner && !isAdminInDiretoria) {
            throw new AccessDeniedException("Sem permissão para editar este documento.");
        }

        model.addAttribute("documento", doc);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        return "editar-documento";
    }

    @PostMapping("/documentos/editar")
    public String salvarEdicaoDocumento(
            @RequestParam("id") Long id,
            @RequestParam("titulo") String titulo,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = findDocumentoAtivoById(id);

        boolean isOwner = doc.getUsuarioUpload().getId().equals(user.getId());
        boolean isAdminInDiretoria = user.getRole() == Role.ADMIN && doc.getDepartment().getNome().equals("Diretoria");

        if (!isOwner && !isAdminInDiretoria) {
            throw new AccessDeniedException("Sem permissão para editar este documento.");
        }

        doc.setTitulo(titulo);
        documentoRepository.save(doc);

        redirectAttributes.addFlashAttribute("sucesso", "Título atualizado!");
        return user.getRole() == Role.ADMIN ? "redirect:/admin/department/" + doc.getDepartment().getId() : "redirect:/dashboard";
    }

    @GetMapping("/lixeira")
    public String verLixeira(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Documento> deletados = (user.getRole() == Role.ADMIN)
                ? documentoRepository.findAllDeletedForAdmin()
                : documentoRepository.findAllDeletedForManager(user.getDepartment());

        model.addAttribute("documentos", deletados);
        return "lixeira";
    }

    @PostMapping("/documentos/deletar/{id}")
    public RedirectView moverParaLixeira(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = findDocumentoAtivoById(id);

        boolean isOwner = doc.getUsuarioUpload().getId().equals(user.getId());
        boolean isAdminInDiretoria = user.getRole() == Role.ADMIN && doc.getDepartment().getNome().equals("Diretoria");

        if (!isOwner && !isAdminInDiretoria) {
            throw new AccessDeniedException("Sem permissão para eliminar este documento.");
        }

        doc.setDeleted(true);
        doc.setDeletionDate(LocalDateTime.now());
        documentoRepository.save(doc);

        redirectAttributes.addFlashAttribute("sucesso", "Documento movido para a lixeira.");
        String redirectUrl = user.getRole() == Role.ADMIN ? "/admin/department/" + doc.getDepartment().getId() : "/dashboard";
        return new RedirectView(redirectUrl);
    }

    @PostMapping("/lixeira/restaurar/{id}")
    public String restaurarDocumento(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirect) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Documento doc = documentoRepository.findByIdRegardlessOfDeletion(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado."));

        if (user.getRole() != Role.ADMIN && !doc.getDepartment().getId().equals(user.getDepartment().getId())) {
            throw new AccessDeniedException("Sem permissão para restaurar.");
        }

        doc.setDeleted(false);
        doc.setDeletionDate(null);
        documentoRepository.save(doc);

        redirect.addFlashAttribute("sucesso", "Documento restaurado!");
        return "redirect:/lixeira";
    }

    @PostMapping("/lixeira/excluir/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluirPermanentemente(@PathVariable Long id, RedirectAttributes redirect) {
        Documento doc = documentoRepository.findByIdRegardlessOfDeletion(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado."));

        fileStorageService.deletarArquivo(doc.getCaminhoArquivo());
        documentoRepository.delete(doc);

        redirect.addFlashAttribute("sucesso", "Eliminado permanentemente.");
        return "redirect:/lixeira";
    }
}