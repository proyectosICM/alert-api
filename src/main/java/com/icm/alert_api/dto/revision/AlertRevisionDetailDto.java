package com.icm.alert_api.dto.revision;

import com.icm.alert_api.dto.revision_photo.AlertRevisionPhotoSummaryDto;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class AlertRevisionDetailDto {

    Long id;

    Long companyId;

    Long alertId;

    String vehiculo;
    String planta;
    String area;
    String operador;

    String motivoFalla;

    LocalDate fechaFalla;

    String accionTomada;

    String revisorNombre;

    String observacionAdicional;

    Instant createdAt;

    List<AlertRevisionPhotoSummaryDto> fotos;
}
