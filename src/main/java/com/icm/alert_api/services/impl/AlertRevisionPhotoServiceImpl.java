package com.icm.alert_api.services.impl;

import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoDetailDto;
import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import com.icm.alert_api.dto.revision_photo.CreateAlertRevisionPhotoRequest;
import com.icm.alert_api.dto.revision_photo.UpdateAlertRevisionPhotoRequest;
import com.icm.alert_api.mappers.AlertRevisionPhotoMapper;
import com.icm.alert_api.models.AlertRevisionModel;
import com.icm.alert_api.models.AlertRevisionPhotoModel;
import com.icm.alert_api.repositories.AlertRevisionPhotoRepository;
import com.icm.alert_api.repositories.AlertRevisionRepository;
import com.icm.alert_api.services.AlertRevisionPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertRevisionPhotoServiceImpl implements AlertRevisionPhotoService {

    private final AlertRevisionRepository revisionRepository;
    private final AlertRevisionPhotoRepository photoRepository;
    private final AlertRevisionPhotoMapper photoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AlertRevisionPhotoSummaryDto> listByRevision(Long companyId, Long revisionId) {
        AlertRevisionModel revision = requireRevision(companyId, revisionId);
        return photoRepository.findByRevision_IdOrderByCreatedAtAsc(revision.getId())
                .stream()
                .map(photoMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlertRevisionPhotoDetailDto> findById(Long companyId, Long revisionId, Long photoId) {
        requireRevision(companyId, revisionId);

        return photoRepository.findById(photoId)
                .filter(p -> p.getRevision() != null && p.getRevision().getId().equals(revisionId))
                .map(photoMapper::toDetailDto);
    }

    @Override
    public AlertRevisionPhotoDetailDto create(Long companyId, CreateAlertRevisionPhotoRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getRevisionId() == null) throw new IllegalArgumentException("revisionId is required");
        if (request.getDataBase64() == null || request.getDataBase64().isBlank()) {
            throw new IllegalArgumentException("dataBase64 is required");
        }

        AlertRevisionModel revision = requireRevision(companyId, request.getRevisionId());

        byte[] data = decodeBase64(request.getDataBase64());

        AlertRevisionPhotoModel photo = AlertRevisionPhotoModel.builder()
                .revision(revision)
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .data(data)
                .build();

        AlertRevisionPhotoModel saved = photoRepository.save(photo);
        return photoMapper.toDetailDto(saved);
    }

    @Override
    public AlertRevisionPhotoDetailDto update(
            Long companyId,
            Long revisionId,
            Long photoId,
            UpdateAlertRevisionPhotoRequest request
    ) {
        if (request == null) throw new IllegalArgumentException("request is required");

        requireRevision(companyId, revisionId);

        AlertRevisionPhotoModel photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));

        if (photo.getRevision() == null || !photo.getRevision().getId().equals(revisionId)) {
            throw new IllegalArgumentException("Photo does not belong to revision: " + revisionId);
        }

        if (request.getFileName() != null) photo.setFileName(request.getFileName());
        if (request.getContentType() != null) photo.setContentType(request.getContentType());

        if (request.getDataBase64() != null) {
            if (request.getDataBase64().isBlank()) {
                throw new IllegalArgumentException("dataBase64 cannot be blank if provided");
            }
            photo.setData(decodeBase64(request.getDataBase64()));
        }

        AlertRevisionPhotoModel updated = photoRepository.save(photo);
        return photoMapper.toDetailDto(updated);
    }

    @Override
    public void deleteById(Long companyId, Long revisionId, Long photoId) {
        requireRevision(companyId, revisionId);

        AlertRevisionPhotoModel photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));

        if (photo.getRevision() == null || !photo.getRevision().getId().equals(revisionId)) {
            throw new IllegalArgumentException("Photo does not belong to revision: " + revisionId);
        }

        photoRepository.delete(photo);
    }

    @Override
    public List<AlertRevisionPhotoSummaryDto> replaceAll(
            Long companyId,
            Long revisionId,
            List<CreateAlertRevisionPhotoRequest> photos
    ) {
        AlertRevisionModel revision = requireRevision(companyId, revisionId);

        // borrar todas
        photoRepository.deleteByRevisionId(revision.getId());

        if (photos == null || photos.isEmpty()) {
            return List.of();
        }

        // crear todas
        List<AlertRevisionPhotoModel> entities = photos.stream().map(req -> {
            if (req.getDataBase64() == null || req.getDataBase64().isBlank()) {
                throw new IllegalArgumentException("dataBase64 is required for each photo");
            }
            return AlertRevisionPhotoModel.builder()
                    .revision(revision)
                    .fileName(req.getFileName())
                    .contentType(req.getContentType())
                    .data(decodeBase64(req.getDataBase64()))
                    .build();
        }).toList();

        photoRepository.saveAll(entities);

        return photoRepository.findByRevision_IdOrderByCreatedAtAsc(revision.getId())
                .stream()
                .map(photoMapper::toSummaryDto)
                .toList();
    }

    // ===================== helpers =====================

    private AlertRevisionModel requireRevision(Long companyId, Long revisionId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        if (revisionId == null) throw new IllegalArgumentException("revisionId is required");

        AlertRevisionModel revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found: " + revisionId));

        if (!revision.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Revision does not belong to company: " + companyId);
        }

        return revision;
    }

    private byte[] decodeBase64(String base64) {
        String b = base64.trim();
        // por si mandan dataUrl accidentalmente, lo toleramos:
        int comma = b.indexOf(',');
        if (b.startsWith("data:") && comma >= 0) {
            b = b.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(b);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 data", e);
        }
    }
}
