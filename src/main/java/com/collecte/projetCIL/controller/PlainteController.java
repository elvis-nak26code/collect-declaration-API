package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.PlainteRequest;
import com.collecte.projetCIL.dto.response.PlainteResponse;
import com.collecte.projetCIL.service.PlainteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST   /api/plaintes/cil-vers-dpo           → CIL envoie une plainte au DPO
 * GET    /api/plaintes/recues-dpo?dpoId=       → DPO voit les plaintes reçues de la CIL
 * GET    /api/plaintes/par-cil?cilId=          → CIL voit ses plaintes émises
 * GET    /api/plaintes/non-cloturees           → admin — toutes plaintes ouvertes
 */
@RestController
@RequestMapping("/api/plaintes")
@RequiredArgsConstructor
public class PlainteController {

    private final PlainteService plainteService;

    @PostMapping("/cil-vers-dpo")
    @PreAuthorize("hasAnyAuthority('ROLE_CIL','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PlainteResponse> envoyerPlainteCilVersDpo(
            @RequestBody PlainteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(plainteService.envoyerPlainteCilVersDpo(request, userDetails.getUsername()));
    }

    @GetMapping("/recues-dpo")
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<PlainteResponse>> plaintesDpo(@RequestParam Long dpoId) {
        return ResponseEntity.ok(plainteService.listerPlaintegRecuesDpo(dpoId));
    }

    @GetMapping("/par-cil")
    @PreAuthorize("hasAnyAuthority('ROLE_CIL','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<PlainteResponse>> parCil(@RequestParam Long cilId) {
        return ResponseEntity.ok(plainteService.listerParCil(cilId));
    }

    @GetMapping("/non-cloturees")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR','ROLE_DPO')")
    public ResponseEntity<List<PlainteResponse>> nonCloturees() {
        return ResponseEntity.ok(plainteService.listerNonCloturees());
    }
}
