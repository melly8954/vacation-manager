package com.melly.vacationmanager.domain.filestorage.repository;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFileEntity, Long> {
    List<EvidenceFileEntity> findAllByVacationRequest_RequestId(Long requestId);

    Optional<EvidenceFileEntity> findByFileId(Long fileId);
}
