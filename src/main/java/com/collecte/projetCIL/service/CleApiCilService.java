package com.collecte.projetCIL.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.collecte.projetCIL.dto.response.CleApiCilResponse;
import com.collecte.projetCIL.models.CleApiCil;
import com.collecte.projetCIL.repository.CleApiCilRepository;

import lombok.RequiredArgsConstructor;

/**
 * Génération et vérification des clés API utilisées par le système externe
 * de la CIL pour appeler /api/cil-externe/** sans login.
 *
 * Entièrement autonome : aucune fiche CIL (ni aucun autre modèle) n'existe
 * ni n'est créée en base pour ce mécanisme. Une clé, c'est juste une ligne
 * dans "cle_api_cil" — rien d'autre.
 *
 * La clé en clair n'est JAMAIS stockée ni ré-affichable (comme Stripe/GitHub) :
 * seule son empreinte SHA-256 est persistée. Elle n'est montrée à l'admin
 * qu'une seule fois, à la génération.
 */
@Service
@RequiredArgsConstructor
public class CleApiCilService {

    private static final String PREFIXE = "cil_live_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CleApiCilRepository cleApiCilRepository;

    /**
     * Crée une nouvelle clé API, immédiatement utilisable.
     * @param libelle nom libre du partenaire/instance (ex: "Système interne CIL - prod")
     * @return la clé EN CLAIR — à communiquer immédiatement, elle ne sera plus jamais récupérable.
     */
    public String genererCle(String libelle) {
        String cleEnClair = PREFIXE + genererChaineAleatoire(32);

        CleApiCil entite = new CleApiCil();
        entite.setCleHachee(hacher(cleEnClair));
        entite.setPrefixeAffichage(cleEnClair.substring(0, PREFIXE.length() + 8) + "...");
        entite.setLibelle(libelle != null && !libelle.isBlank() ? libelle : "Système CIL");
        entite.setActif(true);
        entite.setDateCreation(LocalDateTime.now());
        cleApiCilRepository.save(entite);

        return cleEnClair;
    }

    /**
     * Régénère une clé existante (même partenaire/libellé, nouvelle clé).
     * L'ancienne est désactivée (pas supprimée : traçabilité conservée).
     */
    public String regenererCle(Long cleExistanteId) {
        CleApiCil ancienne = cleApiCilRepository.findById(cleExistanteId)
                .orElseThrow(() -> new RuntimeException("Clé API introuvable : " + cleExistanteId));
        ancienne.setActif(false);
        cleApiCilRepository.save(ancienne);

        return genererCle(ancienne.getLibelle());
    }

    /** Révoque une clé précise (elle ne fonctionnera plus, sans être supprimée de l'historique). */
    public void revoquerCle(Long cleId) {
        CleApiCil c = cleApiCilRepository.findById(cleId)
                .orElseThrow(() -> new RuntimeException("Clé API introuvable : " + cleId));
        c.setActif(false);
        cleApiCilRepository.save(c);
    }

    public List<CleApiCilResponse> listerToutes() {
        return cleApiCilRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Utilisé par le filtre de sécurité : retrouve la clé active correspondant à la clé en clair reçue. */
    public Optional<CleApiCil> verifier(String cleEnClair) {
        if (cleEnClair == null || cleEnClair.isBlank()) return Optional.empty();
        return cleApiCilRepository.findByCleHacheeAndActifTrue(hacher(cleEnClair));
    }

    public void marquerUtilisee(CleApiCil c) {
        c.setDerniereUtilisation(LocalDateTime.now());
        cleApiCilRepository.save(c);
    }

    private String hacher(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valeur.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme de hachage indisponible", e);
        }
    }

    private String genererChaineAleatoire(int nbOctets) {
        byte[] octets = new byte[nbOctets];
        RANDOM.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }

    private CleApiCilResponse toResponse(CleApiCil c) {
        CleApiCilResponse r = new CleApiCilResponse();
        r.setId(c.getId());
        r.setPrefixeAffichage(c.getPrefixeAffichage());
        r.setLibelle(c.getLibelle());
        r.setActif(c.isActif());
        r.setDateCreation(c.getDateCreation());
        r.setDerniereUtilisation(c.getDerniereUtilisation());
        return r;
    }
}
