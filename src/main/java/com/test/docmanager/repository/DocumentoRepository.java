package com.test.docmanager.repository;

import com.test.docmanager.model.Department;
import com.test.docmanager.model.Documento;
import com.test.docmanager.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {

    // --- CONSULTAS "VIVAS" (com filtro deleted = false) ---

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.department = :department AND d.deleted = false")
    long countByDepartment(@Param("department") Department department);

    @Query("SELECT d FROM Documento d WHERE d.department = :department AND d.deleted = false ORDER BY d.dataUpload DESC")
    List<Documento> findTop5ByDepartmentOrderByDataUploadDesc(@Param("department") Department department);

    @Query("SELECT d FROM Documento d WHERE d.deleted = false ORDER BY d.dataUpload DESC")
    List<Documento> findTop5ByOrderByDataUploadDesc();

    @Query("SELECT d FROM Documento d WHERE d.department = :department AND d.tipoDocumento = :tipo AND d.deleted = false")
    List<Documento> findByDepartmentAndTipoDocumento(
            @Param("department") Department department,
            @Param("tipo") TipoDocumento tipoDocumento
    );

    @Query("SELECT d FROM Documento d WHERE d.id = :id AND d.deleted = false")
    Optional<Documento> findByIdAndNotDeleted(@Param("id") Long id);


    // --- CONSULTAS DA LIXEIRA (IGNORAM o filtro) ---

    @Query("SELECT d FROM Documento d WHERE d.id = :id")
    Optional<Documento> findByIdRegardlessOfDeletion(@Param("id") Long id);

    @Query("SELECT d FROM Documento d WHERE d.deleted = true")
    List<Documento> findAllDeletedForAdmin();

    @Query("SELECT d FROM Documento d WHERE d.department = :department AND d.deleted = true")
    List<Documento> findAllDeletedForManager(@Param("department") Department department);

    @Query("SELECT d FROM Documento d WHERE d.deleted = true AND d.deletionDate < :dataLimite")
    List<Documento> findAllDeletedBefore(@Param("dataLimite") LocalDateTime dataLimite);
}