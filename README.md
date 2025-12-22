# 📂 DocManager - Sistema Corporativo de Gestão Eletrônica de Documentos (GED)

> Uma plataforma robusta e escalável para governança e armazenamento seguro de arquivos corporativos, desenvolvida com Java e Spring Boot.

![Status do Projeto](https://img.shields.io/badge/STATUS-RELEASE%201.0-green)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![Security](https://img.shields.io/badge/Spring%20Security-6.0-blue)

## 🎯 Visão Geral

O **DocManager** é um sistema projetado para resolver o caos de arquivos em organizações hierárquicas. Diferente de armazenamentos simples como Google Drive, o DocManager foca em **metadados e permissões baseadas em cargos (RBAC)**, garantindo que a estrutura organizacional seja respeitada.

Ele permite que uma organização crie múltiplos departamentos (RH, Financeiro, Jurídico), delegue gerentes e controle o ciclo de vida de documentos oficiais com segurança de nível empresarial.

---

## 🚀 Funcionalidades Atuais

### 🔐 Segurança & Acesso
* **Autenticação Robusta:** Login seguro com Spring Security e proteção contra ataques de Força Bruta.
* **RBAC (Role-Based Access Control):**
    * `ADMIN`: Visão global, gestão de usuários e configuração dinâmica de departamentos.
    * `MANAGER`: Gestão isolada dos documentos de sua unidade.
* **Convite Seguro:** Fluxo de cadastro via token de e-mail (o administrador convida, o usuário define a senha), eliminando o compartilhamento de credenciais provisórias.

### 📂 Gestão Inteligente
* **Multi-Tenancy Lógico:** Departamentos funcionam como silos isolados. Um gerente do RH não acessa arquivos do Financeiro.
* **Soft Delete (Lixeira Segura):** Exclusão lógica com retenção de 30 dias. Recuperação imediata ou expurgo automático via *Cron Job*.
* **Metadados Ricos:** Classificação automática por tipo (Contrato, Nota Fiscal, Relatório) e data.

### 💻 Interface Moderna
* **UI Responsiva:** Desenvolvida com Thymeleaf e Tailwind CSS.
* **Feedback em Tempo Real:** Alertas de sessão expirando (modal via JavaScript) para evitar perda de dados.

---

## 🔜 Roadmap (Próximos Passos)

O projeto está em evolução constante. As próximas atualizações incluirão:

* [ ] **Trilha de Auditoria:** Logs detalhados de quem visualizou ou baixou cada arquivo (Compliance).
* [ ] **Busca Global:** Pesquisa indexada por título e conteúdo em todos os departamentos.
* [ ] **Visualizador Interno:** Renderização de PDFs diretamente no navegador.

---

## 🛠️ Tech Stack

* **Back-end:** Java 21, Spring Boot 3.5.7, Spring Data JPA.
* **Segurança:** Spring Security 6 (Sessão Stateful, CSRF, Password Encoder).
* **Banco de Dados:** PostgreSQL.
* **Front-end:** Thymeleaf (Server-Side Rendering), Tailwind CSS.
* **Build & Deploy:** Maven, Docker Ready.

---

## ⚙️ Como Rodar o Projeto Localmente

### Pré-requisitos
* Java 21+
* PostgreSQL instalado e rodando.
* Maven.

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/SEU-USUARIO/docmanager.git](https://github.com/SEU-USUARIO/docmanager.git)
    cd docmanager
    ```

2.  **Configure o Banco de Dados:**
    Crie um banco de dados vazio no PostgreSQL chamado `docmanager_db`.

3.  **Configure as Variáveis de Ambiente:**
    Para segurança, o projeto não contém senhas no código. Configure as variáveis na sua IDE ou terminal:
    * `DOCMANAGER_DB_URL`: `jdbc:postgresql://localhost:5432/docmanager_db`
    * `DOCMANAGER_DB_USER`: `postgres`
    * `DOCMANAGER_DB_PASSWORD`: `sua_senha`
    * `DOCMANAGER_MAIL_USER`: `seu_email@gmail.com`
    * `DOCMANAGER_MAIL_PASSWORD`: `sua_senha_de_app`

4.  **Execute a aplicação:**
    ```bash
    mvn spring-boot:run
    ```

5.  **Acesse:**
    Abra `http://localhost:8080`.
    * **Login Admin Padrão:** `admin@docmanager.com`
    * **Senha:** `admin123` (Altere após o primeiro acesso!)

---

## 📞 Contato & LinkedIn

Este projeto faz parte do meu portfólio profissional de Engenharia de Software.

* **LinkedIn:** [Kauã Vilarim](https://www.linkedin.com/in/kaua-vilarim/)
* **E-mail:** vilarim.dev@gmail.com

---
*Desenvolvido com ❤️ e Java.*