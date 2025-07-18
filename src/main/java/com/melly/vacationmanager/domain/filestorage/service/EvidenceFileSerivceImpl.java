package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvidenceFileSerivceImpl implements IEvidenceFileService{
    private final EvidenceFileRepository repository;
}
