package com.fullcycle.ticketsales.interfaces.rest;

import com.fullcycle.ticketsales.application.partner.CreatePartnerRequest;
import com.fullcycle.ticketsales.application.partner.PartnerApplicationService;
import com.fullcycle.ticketsales.domain.partner.Partner;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para Partner.
 *
 * No NestJS: @Controller('partners') com @Get(), @Post()
 * No Spring: @RestController com @GetMapping, @PostMapping
 *
 * DIFERENÇAS:
 * - Spring usa @RestController (= @Controller + @ResponseBody)
 * - Métodos retornam objetos que são serializados automaticamente para JSON
 * - @Valid valida automaticamente o request body
 */
@RestController
@RequestMapping("/api/partners")
@Slf4j
public class PartnerController {

    private final PartnerApplicationService partnerService;

    public PartnerController(PartnerApplicationService partnerService) {
        this.partnerService = partnerService;
    }

    /**
     * GET /api/partners
     * Lista todos os parceiros.
     */
    @GetMapping
    public ResponseEntity<List<Partner>> listAll() {
        log.info("GET /api/partners - Listing all partners");
        List<Partner> partners = partnerService.listAll();
        return ResponseEntity.ok(partners);
    }

    /**
     * POST /api/partners
     * Cria um novo parceiro.
     *
     * @Valid dispara validação automática do request
     */
    @PostMapping
    public ResponseEntity<Partner> create(@Valid @RequestBody CreatePartnerRequest request) {
        log.info("POST /api/partners - Creating partner: {}", request.getName());
        Partner partner = partnerService.create(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(partner);
    }

    /**
     * PUT /api/partners/{id}/name
     * Atualiza nome do parceiro.
     */
    @PutMapping("/{id}/name")
    public ResponseEntity<Partner> updateName(
            @PathVariable String id,
            @Valid @RequestBody CreatePartnerRequest request) {
        log.info("PUT /api/partners/{}/name - Updating to: {}", id, request.getName());
        Partner partner = partnerService.updateName(id, request.getName());
        return ResponseEntity.ok(partner);
    }
}
