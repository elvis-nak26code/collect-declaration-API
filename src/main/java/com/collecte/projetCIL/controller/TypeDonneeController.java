package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.request.TypeDonneeRequest;
import com.collecte.projetCIL.dto.response.TypeDonneeResponse;
import com.collecte.projetCIL.models.TypeDonnee;
import com.collecte.projetCIL.repository.TypeDonneeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/types-donnee")
@RequiredArgsConstructor
public class TypeDonneeController {

    private final TypeDonneeRepository typeDonneeRepository;

    /**
     * GET /api/types-donnee
     * Liste tous les types de données existants (ex: Email, Téléphone, Identité...)
     * pour permettre de les sélectionner dans un menu déroulant côté front,
     * au lieu de devoir connaître leur ID.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<TypeDonneeResponse>> lister() {
        List<TypeDonneeResponse> resultats = typeDonneeRepository.findAll().stream()
                .map(t -> new TypeDonneeResponse(t.getIdTypeDonnee(), t.getNom(), t.getSensible()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultats);
    }

    /**
     * POST /api/types-donnee
     * Permet au DPO/Admin de créer un nouveau type de donnée si celui dont
     * l'utilisateur métier a besoin n'existe pas encore.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DPO','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TypeDonneeResponse> creer(@RequestBody TypeDonneeRequest request) {
        TypeDonnee t = new TypeDonnee();
        t.setNom(request.getNom());
        t.setSensible(request.getSensible() != null ? request.getSensible() : false);
        TypeDonnee saved = typeDonneeRepository.save(t);
        return ResponseEntity.ok(new TypeDonneeResponse(saved.getIdTypeDonnee(), saved.getNom(), saved.getSensible()));
    }
}
