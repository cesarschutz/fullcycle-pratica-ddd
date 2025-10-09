package com.fullcycle.ticketsales.application.partner;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para criação de parceiro.
 *
 * No NestJS: Usaria class-validator com decorators
 * No Spring: Usa Bean Validation (jakarta.validation)
 */
@Data
public class CreatePartnerRequest {

    @NotBlank(message = "Partner name is required")
    private String name;
}
