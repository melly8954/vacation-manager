package com.melly.vacationmanager.domain.filestorage.repository;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFileEntity, Long> {
}
