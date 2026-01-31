package com.icm.alert_api.dto.revision;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateAlertRevisionRequest {

    @Size(max = 100)
    private String vehiculo;

    @Size(max = 150)
    private String planta;

    @Size(max = 150)
    private String area;

    @Size(max = 150)
    private String operador;

    private String motivoFalla;

    private LocalDate fechaFalla;

    private String accionTomada;

    @Size(max = 150)
    private String revisorNombre;

    private String observacionAdicional;
}
