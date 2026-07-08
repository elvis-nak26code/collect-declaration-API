package com.collecte.projetCIL.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.dto.request.DeclarationAutorisationRequest;
import com.collecte.projetCIL.dto.request.DeclarationCollecteSiteInternetRequest;
import com.collecte.projetCIL.dto.request.DeclarationNormaleRequest;
import com.collecte.projetCIL.dto.request.DeclarationVideoSurveillanceRequest;
import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.StatutDeclaration;
import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.enums.StatutTraitement;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.Declaration;
import com.collecte.projetCIL.models.DeclarationAutorisation;
import com.collecte.projetCIL.models.DeclarationCollecteSiteInternet;
import com.collecte.projetCIL.models.DeclarationNormale;
import com.collecte.projetCIL.models.DeclarationSystemeVideoSurveillance;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.DeclarationAutorisationRepository;
import com.collecte.projetCIL.repository.DeclarationCollecteSiteInternetRepository;
import com.collecte.projetCIL.repository.DeclarationNormaleRepository;
import com.collecte.projetCIL.repository.DeclarationRepository;
import com.collecte.projetCIL.repository.DeclarationSystemeVideoSurveillanceRepository;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraitementService {

    private final TraitementRepository                          traitementRepository;
    private final SessionCollecteRepository                     sessionCollecteRepository;
    private final UtilisateurMetierRepository                   utilisateurMetierRepository;
    private final DPORepository                                 dpoRepository;
    private final AdministrateurRepository                      administrateurRepository;
    private final DeclarationRepository                         declarationRepository;
    private final DeclarationNormaleRepository                  declarationNormaleRepository;
    private final DeclarationCollecteSiteInternetRepository     declarationCollecteSiteInternetRepository;
    private final DeclarationSystemeVideoSurveillanceRepository declarationVideoSurveillanceRepository;
    private final DeclarationAutorisationRepository             declarationAutorisationRepository;
    private final JournalAuditService                           journalAuditService;
    private final NotificationService                           notificationService;

    // ------------------------------------------------------------------ //
    //  HELPER : récupère LA déclaration d'un traitement (la plus récente)
    //  Remplace l'ancien declarationRepository.findByTraitementId(...)
    //  qui plantait (NonUniqueResultException) dès qu'un traitement avait
    //  plus d'une déclaration en base.
    // ------------------------------------------------------------------ //
    private Optional<Declaration> findDeclarationByTraitementId(Long traitementId) {
        List<Declaration> declarations =
                declarationRepository.findAllByTraitement_IdTraitementOrderByDateSoumissionDesc(traitementId);
        return declarations.isEmpty() ? Optional.empty() : Optional.of(declarations.get(0));
    }

    // ------------------------------------------------------------------ //
    //  HELPERS PRIVÉS
    // ------------------------------------------------------------------ //

    /**
     * Vérifie qu'une session de collecte est encore active (EN_COURS).
     * Une fois qu'une session est marquée TERMINEE par le DPO, plus aucun
     * nouveau traitement ne peut y être ajouté ou rattaché.
     */
    private void verifierSessionActive(SessionCollecte session) {
        if (session != null && session.getStatutSession() == StatutSession.TERMINEE) {
            throw new RuntimeException(
                    "Impossible d'ajouter un traitement à la session « " + session.getNomSession()
                    + " » : cette session est déjà terminée.");
        }
    }

    /** Crée et sauvegarde l'entité Traitement à partir des champs communs.
     *  La session de collecte est désormais optionnelle. */
    private Traitement buildAndSaveTraitement(TraitementRequest request) {
        SessionCollecte session = null;
        if (request.getSessionCollecteId() != null) {
            session = sessionCollecteRepository.findById(request.getSessionCollecteId())
                    .orElseThrow(() -> new RuntimeException("Session introuvable : " + request.getSessionCollecteId()));
            verifierSessionActive(session);
        }

        UtilisateurMetier utilisateurMetier = utilisateurMetierRepository.findById(request.getUtilisateurMetierId())
                .orElseThrow(() -> new RuntimeException("UtilisateurMetier introuvable : " + request.getUtilisateurMetierId()));

        Traitement traitement = new Traitement();
        traitement.setNom(request.getNom());
        traitement.setDepartment(request.getDepartment());
        traitement.setDescription(request.getDescription());
        traitement.setTexte(request.getTexte());
        traitement.setCertificationSecurite(request.getCertificationSecurite());
        traitement.setDureeConservation(request.getDureeConservation());
        traitement.setDateCreation(LocalDateTime.now());
        traitement.setDateFin(request.getDateFin());
        traitement.setNombreDonnee(0L);
        traitement.setStatut(StatutTraitement.EN_COURS);
        traitement.setEnvoyeAuDpo(false);
        traitement.setSessionCollecte(session);
        traitement.setUtilisateurMetier(utilisateurMetier);

        return traitementRepository.save(traitement);
    }

    /** Pré-remplit les champs communs de la classe mère Declaration. */
    private void remplirDeclarationBase(com.collecte.projetCIL.models.Declaration decl,
                                        TraitementRequest request,
                                        Traitement traitement) {
        decl.setTraitement(traitement);
        // BROUILLON : déclaration pré-remplie par le traitement, pas encore soumise par le DPO.
        // Elle passera EN_ATTENTE seulement quand le DPO la complète et la soumet.
        decl.setStatut(StatutDeclaration.BROUILLON);
        decl.setOrigineDeclaration(com.collecte.projetCIL.enums.OrigineDeclaration.AUTOMATIQUE);
        decl.setDateSoumission(LocalDate.now());

        // ── Champs de base ────────────────────────────────────────────────
        decl.setSecteur(request.getSecteur());
        decl.setLieuStockage(request.getLieuStockage());
        decl.setDureeConservation(request.getDureeConservationDeclaration());
        decl.setDateMiseEnOeuvre(request.getDateMiseEnOeuvre());
        decl.setTransfertPaysEtranger(request.getTransfertEtranger());
        decl.setRecoursSousTraitant(request.getSousTraitance());
        decl.setCommunicationAutresOrganismes(request.getCommunicationTiers());

        // ── Étape 3 : Identification & Responsable ────────────────────────
        decl.setNomPrenomResponsable(request.getNomPrenomResponsable());
        decl.setFonctionResponsable(request.getFonctionResponsable());
        decl.setContactConfidentialite(request.getContactConfidentialite());
        decl.setNatureDemande(request.getNatureDemande());

        // ── Étape 4 : Données traitées ────────────────────────────────────
        decl.setCategoriesDonnees(request.getCategoriesDonnees());
        decl.setOrigineDonnees(request.getOrigineDonnees());

        // ── Étape 4 : Communication & destinataires ───────────────────────
        decl.setDestinataireConformeCil(request.getDestinataireConformeCil());

        // ── Étape 4 : Mesures de sécurité ────────────────────────────────
        decl.setMesuresSecurite(request.getMesuresSecurite());
        decl.setMesuresSensibilisation(request.getMesuresSensibilisation());
        decl.setPolitiqueAccesBatiments(request.getPolitiqueAccesBatiments());
        decl.setCategoriesPersonnesAcces(request.getCategoriesPersonnesAcces());

        // ── Informations responsable (entreprise) ─────────────────────────
        decl.setNomRaisonSociale(request.getNomRaisonSociale());
        decl.setRccm(request.getRccm());
        decl.setSecteurActivite(request.getSecteurActivite());
        decl.setAdresse(request.getAdresse());
        decl.setBoitePostale(request.getBoitePostale());
        decl.setVille(request.getVille());
        decl.setTelephoneResponsable(request.getTelephone());
        decl.setAdresseEmailResponsable(request.getAdresseEmail());
        decl.setActivitePrincipale(request.getActivitePrincipale());

        // Si une session est déjà associée, on pré-renseigne le DPO concerné.
        SessionCollecte session = traitement.getSessionCollecte();
        if (session != null) {
            decl.setDpo(session.getDpo());
        }
    }

    /** Enregistre l'audit de création. */
    private void auditCreation(Traitement traitement) {
        UtilisateurMetier um = traitement.getUtilisateurMetier();
        journalAuditService.enregistrer(um, TypeAction.CREATION,
                ModuleConserne.DECLARATION, ResultatAction.SUCCES);
    }

    // ------------------------------------------------------------------ //
    //  1. Créer traitement + DeclarationNormale
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationNormale(TraitementRequest request,
                                                           DeclarationNormaleRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationNormale decl = new DeclarationNormale();
        remplirDeclarationBase(decl, request, traitement);

        decl.setDenominationTraitement(request.getDenominationTraitement() != null
                ? request.getDenominationTraitement() : declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(request.getFinaliteTraitement() != null
                ? request.getFinaliteTraitement() : declRequest.getFinaliteTraitement());
        decl.setTypeTraitement(request.getTypeTraitement() != null
                ? request.getTypeTraitement() : declRequest.getTypeTraitement());
        decl.setCategoriesPersonnesConcernees(request.getCategoriesPersonnesConcernees() != null
                ? request.getCategoriesPersonnesConcernees() : declRequest.getCategoriesPersonnesConcernees());
        decl.setNombrePersonnesConcernees(request.getNombrePersonnesConcernees() != null
                ? request.getNombrePersonnesConcernees() : declRequest.getNombrePersonnesConcernees());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setDescriptionProcedureManuelle(declRequest.getDescriptionProcedureManuelle());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setCaracteristiquesSysteme(declRequest.getCaracteristiquesSysteme());
        decl.setPolitiqueAccesSystemes(declRequest.getPolitiqueAccesSystemes());
        decl.setModalitesDiffusionResultats(declRequest.getModalitesDiffusionResultats());
        decl.setProtocoleRecherche(declRequest.getProtocoleRecherche());
        decl.setDescriptionConnexionFichiers(declRequest.getDescriptionConnexionFichiers());
        decl.setMotifsInterconnexion(declRequest.getMotifsInterconnexion());
        decl.setIdentiteFichiersInterconnexion(declRequest.getIdentiteFichiersInterconnexion());

        DeclarationNormale saved = declarationNormaleRepository.save(decl);
        auditCreation(traitement);

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  2. Créer traitement + DeclarationCollecteSiteInternet
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationCollecteSite(TraitementRequest request,
                                                                DeclarationCollecteSiteInternetRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationCollecteSiteInternet decl = new DeclarationCollecteSiteInternet();
        remplirDeclarationBase(decl, request, traitement);

        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setCaracteristiquesMainStructure(declRequest.getCaracteristiquesMainStructure());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setDonneesConnexion(declRequest.getDonneesConnexion());
        decl.setDescriptionDonneesConnexion(declRequest.getDescriptionDonneesConnexion());
        decl.setCookies(declRequest.getCookies());
        decl.setDescriptionCookies(declRequest.getDescriptionCookies());
        decl.setDureeConservationCookies(declRequest.getDureeConservationCookies());
        decl.setTelechargementTraitement(declRequest.getTelechargementTraitement());

        DeclarationCollecteSiteInternet saved = declarationCollecteSiteInternetRepository.save(decl);
        auditCreation(traitement);

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  3. Créer traitement + DeclarationSystemeVideoSurveillance
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationVideoSurveillance(TraitementRequest request,
                                                                     DeclarationVideoSurveillanceRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationSystemeVideoSurveillance decl = new DeclarationSystemeVideoSurveillance();
        remplirDeclarationBase(decl, request, traitement);

        decl.setFinalites(declRequest.getFinalites());
        decl.setAdresseInstallation(declRequest.getAdresseInstallation());
        decl.setNatureEnvironnement(declRequest.getNatureEnvironnement());
        decl.setEmplacementCameras(declRequest.getEmplacementCameras());
        decl.setNombreTotalCameras(declRequest.getNombreTotalCameras());
        decl.setModeleDispositif(declRequest.getModeleDispositif());
        decl.setVisualisationTempsReel(declRequest.getVisualisationTempsReel());
        decl.setModeTransfert(declRequest.getModeTransfert());
        decl.setSonDeSon(declRequest.getSonDeSon());
        decl.setTypeEnregistrement(declRequest.getTypeEnregistrement());
        decl.setNatureEnregistrement(declRequest.getNatureEnregistrement());
        decl.setLiaisonReseau(declRequest.getLiaisonReseau());
        decl.setUtilisationSystemesExperts(declRequest.getUtilisationSystemesExperts());
        decl.setDescriptionSystemesExperts(declRequest.getDescriptionSystemesExperts());
        decl.setFonctionnalitesTraitement(declRequest.getFonctionnalitesTraitement());
        decl.setAccesImagesDistance(declRequest.getAccesImagesDistance());
        decl.setAccesPhysique(declRequest.getAccesPhysique());
        decl.setAccesLogique(declRequest.getAccesLogique());
        decl.setMesuresSuppression(declRequest.getMesuresSuppression());
        decl.setAttribute(declRequest.getAttribute());
        decl.setLocalisationPictogrammes(declRequest.getLocalisationPictogrammes());

        DeclarationSystemeVideoSurveillance saved = declarationVideoSurveillanceRepository.save(decl);
        auditCreation(traitement);

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  4. Créer traitement + DeclarationAutorisation
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse creerAvecDeclarationAutorisation(TraitementRequest request,
                                                                DeclarationAutorisationRequest declRequest) {
        Traitement traitement = buildAndSaveTraitement(request);

        DeclarationAutorisation decl = new DeclarationAutorisation();
        remplirDeclarationBase(decl, request, traitement);

        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setNombrePersonnesConcernees(declRequest.getNombrePersonnesConcernees());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setCaracteristiquesTechniques(declRequest.getCaracteristiquesTechniques());
        decl.setFonctionnalitesSysteme(declRequest.getFonctionnalitesSysteme());
        decl.setCertificationSecurite(declRequest.getCertificationSecurite());
        decl.setPolitiqueAccesSystemes(declRequest.getPolitiqueAccesSystemes());
        decl.setDescriptionFichier(declRequest.getDescriptionFichier());
        decl.setModeTransfert(declRequest.getModeTransfert());
        decl.setTraitementDonneesSante(declRequest.getTraitementDonneesSante());
        decl.setProfessionalSante(declRequest.getProfessionalSante());
        decl.setDestinataireAdresse(declRequest.getDestinataireAdresse());
        decl.setTexteJuridiqueCommunication(declRequest.getTexteJuridiqueCommunication());
        decl.setModalitesDiffusionResultats(declRequest.getModalitesDiffusionResultats());
        decl.setDestinataireCie(declRequest.getDestinataireCie());
        decl.setConnexionFichiers(declRequest.getConnexionFichiers());
        decl.setCategoriesDonneesInterconnexion(declRequest.getCategoriesDonneesInterconnexion());
        decl.setDureeInterconnexion(declRequest.getDureeInterconnexion());
        decl.setIdentiteFichiersInterconnexion(declRequest.getIdentiteFichiersInterconnexion());
        decl.setTransfertPaysEtranger(declRequest.getTransfertPaysEtranger());
        decl.setRecoursSousTraitant(declRequest.getRecoursSousTraitant());
        decl.setRolesSousTraitants(declRequest.getRolesSousTraitants());
        decl.setCategoriesPersonnesAcces(declRequest.getCategoriesPersonnesAcces());
        decl.setPolitiqueAccesBatiments(declRequest.getPolitiqueAccesBatiments());
        decl.setMesuresSecurite(declRequest.getMesuresSecurite());
        decl.setDescriptionSensibilisation(declRequest.getDescriptionSensibilisation());
        decl.setPaysDestinationProtectionDonnees(declRequest.getPaysDestinationProtectionDonnees());
        decl.setDescriptionFichierTransfert(declRequest.getDescriptionFichierTransfert());
        decl.setNombrePersonnesTransfert(declRequest.getNombrePersonnesTransfert());
        decl.setCategoriesDonneesTransfert(declRequest.getCategoriesDonneesTransfert());
        decl.setFondementJuridique(declRequest.getFondementJuridique());
        decl.setConsentementPersonnesConcernees(declRequest.getConsentementPersonnesConcernees());
        decl.setMethodeRecueilConsentement(declRequest.getMethodeRecueilConsentement());
        decl.setMesuresSecuriteTransfert(declRequest.getMesuresSecuriteTransfert());
        decl.setLieuStockage(declRequest.getLieuStockage());
        decl.setCommunicationAutresOrganismes(declRequest.getCommunicationAutresOrganismes());
        decl.setDestinataireNomPrenom(declRequest.getDestinataireNomPrenom());
        decl.setDureeConservationSante(declRequest.getDureeConservationSante());
        decl.setOrigineDonnees(declRequest.getOrigineDonnees());

        DeclarationAutorisation saved = declarationAutorisationRepository.save(decl);
        auditCreation(traitement);

        return toResponse(traitement, saved.getIdDeclaration());
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements d'une session
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParSession(Long sessionId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> t.getSessionCollecte() != null
                        && t.getSessionCollecte().getIdSession().equals(sessionId))
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements sans session d'un UtilisateurMetier
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerSansSession(Long utilisateurMetierId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> t.getSessionCollecte() == null
                        && t.getUtilisateurMetier() != null
                        && t.getUtilisateurMetier().getId().equals(utilisateurMetierId))
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Lister tous les traitements d'un UtilisateurMetier
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParUtilisateurMetier(Long utilisateurMetierId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> t.getUtilisateurMetier() != null
                        && t.getUtilisateurMetier().getId().equals(utilisateurMetierId))
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Obtenir un traitement par ID
    // ------------------------------------------------------------------ //
    public TraitementResponse getTraitementById(Long id) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Incrémenter le compteur de données
    // ------------------------------------------------------------------ //
    public void incrementerNombreDonnee(Long traitementId, long quantite) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));
        long actuel = traitement.getNombreDonnee() != null ? traitement.getNombreDonnee() : 0L;
        traitement.setNombreDonnee(actuel + quantite);
        traitementRepository.save(traitement);
    }

    // ------------------------------------------------------------------ //
    //  Lier un traitement à une session de collecte
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse lierSession(Long traitementId, Long sessionId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        SessionCollecte session = sessionCollecteRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable : " + sessionId));
        verifierSessionActive(session);

        traitement.setSessionCollecte(session);
        traitementRepository.save(traitement);

        findDeclarationByTraitementId(traitementId).ifPresent(decl -> {
            if (decl.getDpo() == null) {
                decl.setDpo(session.getDpo());
                declarationRepository.save(decl);
            }
        });

        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Délier un traitement de sa session
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse delierSession(Long traitementId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        traitement.setSessionCollecte(null);
        traitementRepository.save(traitement);

        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Envoyer un traitement au DPO
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse envoyerAuDpo(Long traitementId, Long dpoId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        if (Boolean.TRUE.equals(traitement.getEnvoyeAuDpo())) {
            throw new RuntimeException("Ce traitement a déjà été envoyé au DPO.");
        }

        DPO dpo = null;
        if (traitement.getSessionCollecte() != null) {
            dpo = traitement.getSessionCollecte().getDpo();
        }
        if (dpo == null) {
            if (dpoId == null) {
                throw new RuntimeException("Aucun DPO associé : veuillez préciser un dpoId.");
            }
            dpo = dpoRepository.findById(dpoId)
                    .orElseThrow(() -> new RuntimeException("DPO introuvable : " + dpoId));
        }

        Declaration declaration = findDeclarationByTraitementId(traitementId).orElse(null);
        if (declaration != null && declaration.getDpo() == null) {
            declaration.setDpo(dpo);
            declarationRepository.save(declaration);
        }

        traitement.setEnvoyeAuDpo(true);
        traitement.setDateEnvoiDpo(LocalDateTime.now());
        traitementRepository.save(traitement);

        UtilisateurMetier um = traitement.getUtilisateurMetier();
        journalAuditService.enregistrer(um, TypeAction.MODIFICATION,
                ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        notificationService.envoyer(dpo, TypeNotification.ALERTE,
                "Le traitement « " + traitement.getDescription()
                + " » a été envoyé par " + um.getPrenom() + " " + um.getNom()
                + (traitement.getSessionCollecte() != null
                    ? " dans la session #" + traitement.getSessionCollecte().getIdSession()
                    : " (hors session)")
                + ". Une déclaration attend votre complétion.");

        return toResponse(traitement, declaration != null ? declaration.getIdDeclaration() : null);
    }

    // ------------------------------------------------------------------ //
    //  Helper : vérifier si la déclaration associée est modifiable
    //  (admin toujours autorisé ; les autres rôles seulement si statut
    //   n'a pas encore été validé par DG ou CIL)
    // ------------------------------------------------------------------ //
    private void verifierDeclarationModifiable(Traitement traitement, boolean isAdmin) {
        if (isAdmin) return;
        findDeclarationByTraitementId(traitement.getIdTraitement()).ifPresent(decl -> {
            StatutDeclaration s = decl.getStatut();
            if (s == StatutDeclaration.APPROUVEE_DG
                    || s == StatutDeclaration.EN_VERIFICATION_CIL
                    || s == StatutDeclaration.VALIDEE_CIL
                    || s == StatutDeclaration.APPROUVEE) {
                throw new RuntimeException(
                        "Impossible de modifier/supprimer : la déclaration a déjà été validée (statut : " + s + ").");
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Mettre à jour un traitement (champs métier uniquement)
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse updateTraitement(Long id, TraitementRequest request, String emailUser) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));

        boolean isAdmin = administrateurRepository.findByEmail(emailUser).isPresent();
        verifierDeclarationModifiable(traitement, isAdmin);

        // Mise à jour des champs modifiables
        if (request.getNom() != null) traitement.setNom(request.getNom());
        if (request.getDepartment() != null) traitement.setDepartment(request.getDepartment());
        if (request.getDescription() != null) traitement.setDescription(request.getDescription());
        if (request.getTexte() != null) traitement.setTexte(request.getTexte());
        if (request.getCertificationSecurite() != null) traitement.setCertificationSecurite(request.getCertificationSecurite());
        if (request.getDureeConservation() != null) traitement.setDureeConservation(request.getDureeConservation());
        if (request.getDateFin() != null) traitement.setDateFin(request.getDateFin());

        Traitement saved = traitementRepository.save(traitement);

        journalAuditService.enregistrer(traitement.getUtilisateurMetier(), TypeAction.MODIFICATION,
                ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        return toResponse(saved, null);
    }

    // ------------------------------------------------------------------ //
    //  Supprimer un traitement (et sa/ses déclaration(s) associée(s))
    // ------------------------------------------------------------------ //
    @Transactional
    public void deleteTraitement(Long id, String emailUser) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));

        boolean isAdmin = administrateurRepository.findByEmail(emailUser).isPresent();
        verifierDeclarationModifiable(traitement, isAdmin);

        // Supprime TOUTES les déclarations liées (au cas où il y en aurait plusieurs,
        // pour éviter de laisser des lignes orphelines en base).
        List<Declaration> declarations =
                declarationRepository.findAllByTraitement_IdTraitementOrderByDateSoumissionDesc(id);
        declarationRepository.deleteAll(declarations);

        traitementRepository.delete(traitement);

        journalAuditService.enregistrer(traitement.getUtilisateurMetier(), TypeAction.SUPPRESSION,
                ModuleConserne.DECLARATION, ResultatAction.SUCCES);
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private TraitementResponse toResponse(Traitement t, Long declarationId) {
        String nomMetier = (t.getUtilisateurMetier() != null)
                ? t.getUtilisateurMetier().getPrenom() + " " + t.getUtilisateurMetier().getNom()
                : null;

        // On récupère la déclaration une seule fois : elle sert à la fois à
        // déterminer l'ID (si non fourni) et le statut "déclaré / non déclaré".
        Optional<Declaration> declarationOpt = findDeclarationByTraitementId(t.getIdTraitement());

        Long declId = declarationId != null
                ? declarationId
                : declarationOpt.map(Declaration::getIdDeclaration).orElse(null);

        // Un traitement est "déclaré" s'il possède une déclaration dont la
        // soumission a déjà eu lieu (statut différent de BROUILLON).
        boolean isDeclare = declarationOpt
                .map(d -> d.getStatut() != StatutDeclaration.BROUILLON)
                .orElse(false);

        return new TraitementResponse(
                t.getIdTraitement(),
                t.getNom(),                                                              // ← ajouté
                t.getDepartment(),
                t.getDescription(),
                t.getTexte(),
                t.getCertificationSecurite(),
                t.getDureeConservation(),
                t.getDateCreation(),
                t.getDateFin(),
                t.getNombreDonnee(),
                t.getSessionCollecte() != null ? t.getSessionCollecte().getIdSession() : null,
                t.getUtilisateurMetier() != null ? t.getUtilisateurMetier().getId() : null,
                nomMetier,
                declId,
                t.getStatut(),
                t.getEnvoyeAuDpo(),
                t.getDateEnvoiDpo(),
                isDeclare
        );
    }

    // ------------------------------------------------------------------ //
    //  Changer le statut d'un traitement
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse updateStatut(Long id, StatutTraitement nouveauStatut) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
        traitement.setStatut(nouveauStatut);
        traitementRepository.save(traitement);
        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Lister tous les traitements (dashboard DPO global)
    //  -> uniquement ceux envoyés au DPO
    //  -> filtre optionnel "declare" : true = déclarés uniquement,
    //     false = non déclarés uniquement, null = pas de filtre
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerTous(Boolean declare) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnvoyeAuDpo()))
                .map(t -> toResponse(t, null))
                .filter(r -> declare == null || declare.equals(r.getDeclare()))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements des sessions d'un DPO spécifique
    //  -> uniquement ceux envoyés au DPO
    //  -> filtre optionnel "declare" (voir listerTous)
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParDpo(Long dpoId, Boolean declare) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnvoyeAuDpo()))
                .filter(t ->
                        (t.getSessionCollecte() != null
                                && t.getSessionCollecte().getDpo() != null
                                && t.getSessionCollecte().getDpo().getId().equals(dpoId))
                        || findDeclarationByTraitementId(t.getIdTraitement())
                                .map(d -> d.getDpo() != null && d.getDpo().getId().equals(dpoId))
                                .orElse(false)
                )
                .map(t -> toResponse(t, null))
                .filter(r -> declare == null || declare.equals(r.getDeclare()))
                .collect(Collectors.toList());
    }
}