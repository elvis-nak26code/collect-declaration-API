package com.collecte.projetCIL.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    private final TraitementRepository                        traitementRepository;
    private final SessionCollecteRepository                   sessionCollecteRepository;
    private final UtilisateurMetierRepository                 utilisateurMetierRepository;
    private final DPORepository                               dpoRepository;
    private final DeclarationRepository                       declarationRepository;
    private final DeclarationNormaleRepository                declarationNormaleRepository;
    private final DeclarationCollecteSiteInternetRepository   declarationCollecteSiteInternetRepository;
    private final DeclarationSystemeVideoSurveillanceRepository declarationVideoSurveillanceRepository;
    private final DeclarationAutorisationRepository           declarationAutorisationRepository;
    private final JournalAuditService                         journalAuditService;
    private final NotificationService                         notificationService;

    // ------------------------------------------------------------------ //
    //  HELPERS PRIVÉS
    // ------------------------------------------------------------------ //

    /** Crée et sauvegarde l'entité Traitement à partir des champs communs.
     *  La session de collecte est désormais optionnelle. */
    private Traitement buildAndSaveTraitement(TraitementRequest request) {
        SessionCollecte session = null;
        if (request.getSessionCollecteId() != null) {
            session = sessionCollecteRepository.findById(request.getSessionCollecteId())
                    .orElseThrow(() -> new RuntimeException("Session introuvable : " + request.getSessionCollecteId()));
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
        decl.setStatut(StatutDeclaration.EN_ATTENTE);
        decl.setDateSoumission(LocalDate.now());

        decl.setSecteur(request.getSecteur());
        decl.setLieuStockage(request.getLieuStockage());
        decl.setDureeConservation(request.getDureeConservationDeclaration());
        decl.setDateMiseEnOeuvre(request.getDateMiseEnOeuvre());
        decl.setTransfertPaysEtranger(request.getTransfertEtranger());
        decl.setRecoursSousTraitant(request.getSousTraitance());
        decl.setCommunicationAutresOrganismes(request.getCommunicationTiers());

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
        // Sinon le DPO sera défini lors de l'envoi explicite au DPO.
        SessionCollecte session = traitement.getSessionCollecte();
        if (session != null) {
            decl.setDpo(session.getDpo());
        }
    }

    /** Enregistre l'audit de création (sans notifier le DPO : il ne doit rien
     *  voir avant l'envoi explicite par l'UtilisateurMetier). */
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

        // Champs spécifiques DeclarationNormale
        decl.setDenominationTraitement(declRequest.getDenominationTraitement());
        decl.setFinaliteTraitement(declRequest.getFinaliteTraitement());
        decl.setTypeTraitement(declRequest.getTypeTraitement());
        decl.setTexteJuridique(declRequest.getTexteJuridique());
        decl.setCategoriesPersonnesConcernees(declRequest.getCategoriesPersonnesConcernees());
        decl.setNombrePersonnesConcernees(declRequest.getNombrePersonnesConcernees());
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

        // Champs spécifiques DeclarationCollecteSiteInternet
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

        // Champs spécifiques DeclarationSystemeVideoSurveillance
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

        // Champs spécifiques DeclarationAutorisation
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
    //  Lister les traitements sans session (orphelins) d'un UtilisateurMetier
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
    //  Lister tous les traitements d'un UtilisateurMetier (avec ou sans session)
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
    //  Lier un traitement existant à une session de collecte
    // ------------------------------------------------------------------ //
    @Transactional
    public TraitementResponse lierSession(Long traitementId, Long sessionId) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + traitementId));

        SessionCollecte session = sessionCollecteRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable : " + sessionId));

        traitement.setSessionCollecte(session);
        traitementRepository.save(traitement);

        // Si une déclaration existe déjà pour ce traitement et qu'elle n'a pas
        // encore de DPO assigné, on la rattache au DPO de la session.
        declarationRepository.findByTraitementId(traitementId).ifPresent(decl -> {
            if (decl.getDpo() == null) {
                decl.setDpo(session.getDpo());
                declarationRepository.save(decl);
            }
        });

        return toResponse(traitement, null);
    }

    // ------------------------------------------------------------------ //
    //  Délier un traitement de sa session (le rendre orphelin)
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
    //  Envoyer un traitement (et sa déclaration) au DPO
    //  - Tant que cette action n'est pas effectuée, le DPO ne voit pas
    //    le traitement (listerTous / listerParDpo l'excluent).
    //  - dpoId est requis uniquement si le traitement n'a pas de session
    //    (donc pas de DPO déjà déterminé).
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

        // Assigner le DPO à la déclaration pré-remplie si elle existe et n'en a pas
        Declaration declaration = declarationRepository.findByTraitementId(traitementId).orElse(null);
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
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private TraitementResponse toResponse(Traitement t, Long declarationId) {
        String nomMetier = (t.getUtilisateurMetier() != null)
                ? t.getUtilisateurMetier().getPrenom() + " " + t.getUtilisateurMetier().getNom()
                : null;

        Long declId = declarationId;
        if (declId == null) {
            declId = declarationRepository.findByTraitementId(t.getIdTraitement())
                    .map(Declaration::getIdDeclaration)
                    .orElse(null);
        }

        return new TraitementResponse(
                t.getIdTraitement(),
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
                t.getDateEnvoiDpo()
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
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerTous() {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnvoyeAuDpo()))
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements des sessions d'un DPO spécifique
    //  -> uniquement ceux envoyés au DPO
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParDpo(Long dpoId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnvoyeAuDpo()))
                .filter(t ->
                        (t.getSessionCollecte() != null
                                && t.getSessionCollecte().getDpo() != null
                                && t.getSessionCollecte().getDpo().getId().equals(dpoId))
                        || declarationRepository.findByTraitementId(t.getIdTraitement())
                                .map(d -> d.getDpo() != null && d.getDpo().getId().equals(dpoId))
                                .orElse(false)
                )
                .map(t -> toResponse(t, null))
                .collect(Collectors.toList());
    }

}