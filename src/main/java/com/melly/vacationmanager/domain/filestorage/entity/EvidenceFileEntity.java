package com.melly.vacationmanager.domain.filestorage.entity;

import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_file_tbl")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private VacationRequestEntity vacationRequest;

    @Column(name = "is_used")   // “휴가 신청”에 실제로 첨부되어 사용되고 있는지 여부
    private Boolean isUsed;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "unique_name")
    private String uniqueName;

    @Column(name = "saved_path")
    private String savedPath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        if (this.uploadedAt == null) {  // null 체크 꼭 하기
            this.uploadedAt = LocalDateTime.now();
        }
    }
}