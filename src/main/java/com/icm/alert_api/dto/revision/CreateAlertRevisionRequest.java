package com.icm.alert_api.dto.revision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAlertRevisionRequest {

    @NotNull
    private Long alertId;

    @NotNull
    private Long companyId;

    @Size(max = 100)
    private String vehiculo;

    @Size(max = 150)
    private String planta;

    @Size(max = 150)
    private String area;

    @Size(max = 150)
    private String operador;

    /**
     * Campos “grandes”
     */
    private String motivoFalla;

    @NotNull
    private LocalDate fechaFalla;

    private String accionTomada;

    @NotBlank
    @Size(max = 150)
    private String revisorNombre;

    private String observacionAdicional;
}
