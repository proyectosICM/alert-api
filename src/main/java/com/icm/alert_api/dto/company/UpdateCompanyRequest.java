package com.icm.alert_api.dto.company;


import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCompanyRequest {

    @Size(max = 150)
    private String name;
}