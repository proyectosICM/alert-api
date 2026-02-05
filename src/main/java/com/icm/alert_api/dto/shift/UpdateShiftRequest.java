package com.icm.alert_api.dto.shift;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateShiftRequest {

    /**
     * Normalmente no deberías cambiar rosterDate/shiftName si viene de Excel,
     * pero lo dejo por si editas manualmente.
     */
    private LocalDate rosterDate;

    @Size(max = 120)
    private String shiftName;

    @Size(max = 36)
    private String batchId;

    private Boolean active;

    private List<@Size(max = 15) String> responsibleDnis;

    private List<@Size(max = 50) String> vehiclePlates;
}
