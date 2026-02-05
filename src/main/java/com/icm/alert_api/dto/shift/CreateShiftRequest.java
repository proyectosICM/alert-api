package com.icm.alert_api.dto.shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateShiftRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private LocalDate rosterDate;

    @NotBlank
    @Size(max = 120)
    private String shiftName;

    /**
     * Si creas manualmente podrías no enviar batchId y el service lo genera.
     * Para import, también puedes enviarlo si quieres agrupar.
     */
    @Size(max = 36)
    private String batchId;

    /**
     * Si no se envía, por defecto true.
     */
    private Boolean active;

    /**
     * Snapshot DNIs del Excel o manual.
     */
    private List<@Size(max = 15) String> responsibleDnis;

    /**
     * Snapshot placas (idealmente normalizadas).
     */
    private List<@Size(max = 50) String> vehiclePlates;
}
