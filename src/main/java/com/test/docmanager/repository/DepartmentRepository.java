package com.test.docmanager.repository;

import com.test.docmanager.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Procura o departamento principal da organização.
     * No nosso DataInitializer, definimos este nome como 'Diretoria'.
     */
    @Query("SELECT d FROM Department d WHERE d.nome = 'Diretoria'")
    Optional<Department> findDiretoria();

    /**
     * Lista todos os departamentos, exceto a Diretoria, para exibição no Dashboard.
     */
    @Query("SELECT d FROM Department d WHERE d.nome != 'Diretoria' ORDER BY d.nome")
    List<Department> findOutrosDepartamentos();
}