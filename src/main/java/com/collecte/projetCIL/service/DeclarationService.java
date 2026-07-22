package com.collecte.projetCIL.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.dto.request.DeclarationAutorisationRequest;
import com.collecte.projetCIL.dto.request.DeclarationCollecteSiteInternetRequest;
import com.collecte.projetCIL.dto.request.DeclarationNormaleRequest;
import com.collecte.projetCIL.dto.request.DeclarationVideoSurveillanceRequest;
import com.collecte.projetCIL.dto.response.DeclarationResponse;
import com.collecte.projetCIL.dto.response.HistoriqueDeclarationResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.NatureDemande;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.StatutDeclaration;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.CIL;
import com.collecte.projetCIL.models.DG;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.Declaration;
import com.collecte.projetCIL.models.DeclarationAutorisation;
import com.collecte.projetCIL.models.DeclarationCollecteSiteInternet;
import com.collecte.projetCIL.models.DeclarationNormale;
import com.collecte.projetCIL.models.DeclarationSystemeVideoSurveillance;
import com.collecte.projetCIL.models.HistoriqueDeclaration;
import com.collecte.projetCIL.models.CleApiCil;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.CILRepository;
import com.collecte.projetCIL.repository.DGRepository;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.DeclarationAutorisationRepository;
import com.collecte.projetCIL.repository.DeclarationCollecteSiteInternetRepository;
import com.collecte.projetCIL.repository.DeclarationNormaleRepository;
import com.collecte.projetCIL.repository.DeclarationRepository;
import com.collecte.projetCIL.repository.DeclarationSystemeVideoSurveillanceRepository;
import com.collecte.projetCIL.repository.HistoriqueDeclarationRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final DeclarationNormaleRepository               normaleRepo;
    private final DeclarationCollecteSiteInternetRepository  collecteSiteRepo;
    private final DeclarationSystemeVideoSurveillanceRepository videoRepo;
    private final DeclarationAutorisationRepository          autorisationRepo;
    private final DeclarationRepository                      declarationRepo;
    private final TraitementRepository                       traitementRepo;
    private final HistoriqueDeclarationRepository             historiqueDeclarationRepo;
    private final DPORepository                              dpoRepo;
    private final AdministrateurRepository                   administrateurRepo;
    private final DGRepository                               dgRepo;
    private final CILRepository                              cilRepo;
    private final UtilisateurMetierRepository                utilisateurMetierRepo;
    private final JournalAuditService                        journalAuditService;
    private final NotificationService                        notificationService;

    // ================================================================== //
    //  DÉCLARATION NORMALE
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationNormale(DeclarationNormaleRequest req, String emailDpo) {

        DPO dpo         = getDpo(emailDpo);
        Traitement trt  = getTraitement(req.getTraitementId());

        // ── Un traitement ne peut être déclaré qu'UNE seule fois ────────
        Declaration existante = getDeclarationReutilisable(trt.getIdTraitement());
        if (existante != null && !(existante instanceof DeclarationNormale)) {
            declarationRepo.delete(existante);
            existante = null;
        }

        DeclarationNormale d = existante != null ? (DeclarationNormale) existante : new DeclarationNormale();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDescriptionProcedureManuelle(req.getDescriptionProcedureManuelle());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setCaracteristiquesSysteme(req.getCaracteristiquesSysteme());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setProtocoleRecherche(req.getProtocoleRecherche());
        d.setDescriptionConnexionFichiers(req.getDescriptionConnexionFichiers());
        d.setMotifsInterconnexion(req.getMotifsInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());
        d.setIntituleTraitement(req.getIntituleTraitement());
        d.setSupportTraitement(req.getSupportTraitement());
        d.setCategoriesDonneesCollectees(req.getCategoriesDonneesCollectees());
        d.setDonneesSensibles(req.getDonneesSensibles());
        d.setNatureDonneesSensibles(req.getNatureDonneesSensibles());
        d.setServiceResponsable(req.getServiceResponsable());
        d.setDateSignature(req.getDateSignature());
        d.setLieuSignature(req.getLieuSignature());
        d.setPaysDestination(req.getPaysDestination());
        d.setGarantiesProtectionEtranger(req.getGarantiesProtectionEtranger());

        d.setTraitement(trt);
        d.setOrigineDeclaration(com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE);

        DeclarationNormale saved = normaleRepo.save(d);
        postCreation(dpo, trt, saved, "NORMALE");
        return toResponse(saved, "NORMALE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION COLLECTE SITE INTERNET
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationCollecteSite(DeclarationCollecteSiteInternetRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        Declaration existante = getDeclarationReutilisable(trt.getIdTraitement());
        if (existante != null && !(existante instanceof DeclarationCollecteSiteInternet)) {
            declarationRepo.delete(existante);
            existante = null;
        }

        DeclarationCollecteSiteInternet d = existante != null ? (DeclarationCollecteSiteInternet) existante : new DeclarationCollecteSiteInternet();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setCaracteristiquesMainStructure(req.getCaracteristiquesMainStructure());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDonneesConnexion(req.getDonneesConnexion());
        d.setDescriptionDonneesConnexion(req.getDescriptionDonneesConnexion());
        d.setCookies(req.getCookies());
        d.setDescriptionCookies(req.getDescriptionCookies());
        d.setDureeConservationCookies(req.getDureeConservationCookies());
        d.setTelechargementTraitement(req.getTelechargementTraitement());
        d.setUrlSite(req.getUrlSite());
        d.setTypeCookies(req.getTypeCookies());
        d.setConsentementCookies(req.getConsentementCookies());
        d.setFormulairesEnLigne(req.getFormulairesEnLigne());
        d.setDonneesFormulaires(req.getDonneesFormulaires());
        d.setServiceResponsable(req.getServiceResponsable());
        d.setDateSignature(req.getDateSignature());
        d.setLieuSignature(req.getLieuSignature());
        d.setPaysDestination(req.getPaysDestination());
        d.setGarantiesProtectionEtranger(req.getGarantiesProtectionEtranger());

        d.setTraitement(trt);
        d.setOrigineDeclaration(com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE);

        DeclarationCollecteSiteInternet saved = collecteSiteRepo.save(d);
        postCreation(dpo, trt, saved, "COLLECTE_SITE");
        return toResponse(saved, "COLLECTE_SITE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION VIDÉO SURVEILLANCE
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationVideoSurveillance(DeclarationVideoSurveillanceRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        Declaration existante = getDeclarationReutilisable(trt.getIdTraitement());
        if (existante != null && !(existante instanceof DeclarationSystemeVideoSurveillance)) {
            declarationRepo.delete(existante);
            existante = null;
        }

        DeclarationSystemeVideoSurveillance d = existante != null ? (DeclarationSystemeVideoSurveillance) existante : new DeclarationSystemeVideoSurveillance();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setFinalites(req.getFinalites());
        d.setAdresseInstallation(req.getAdresseInstallation());
        d.setNatureEnvironnement(req.getNatureEnvironnement());
        d.setEmplacementCameras(req.getEmplacementCameras());
        d.setNombreTotalCameras(req.getNombreTotalCameras());
        d.setModeleDispositif(req.getModeleDispositif());
        d.setVisualisationTempsReel(req.getVisualisationTempsReel());
        d.setModeTransfert(req.getModeTransfert());
        d.setSonDeSon(req.getSonDeSon());
        d.setTypeEnregistrement(req.getTypeEnregistrement());
        d.setNatureEnregistrement(req.getNatureEnregistrement());
        d.setLiaisonReseau(req.getLiaisonReseau());
        d.setUtilisationSystemesExperts(req.getUtilisationSystemesExperts());
        d.setDescriptionSystemesExperts(req.getDescriptionSystemesExperts());
        d.setFonctionnalitesTraitement(req.getFonctionnalitesTraitement());
        d.setAccesImagesDistance(req.getAccesImagesDistance());
        d.setAccesPhysique(req.getAccesPhysique());
        d.setAccesLogique(req.getAccesLogique());
        d.setMesuresSuppression(req.getMesuresSuppression());
        d.setAttribute(req.getAttribute());
        d.setLocalisationPictogrammes(req.getLocalisationPictogrammes());
        d.setDureeConservationVideo(req.getDureeConservationVideo());
        d.setModalitesAccesDistance(req.getModalitesAccesDistance());
        d.setPersonnesHabilitees(req.getPersonnesHabilitees());
        d.setServiceResponsable(req.getServiceResponsable());
        d.setDateSignature(req.getDateSignature());
        d.setLieuSignature(req.getLieuSignature());
        d.setPaysDestination(req.getPaysDestination());
        d.setGarantiesProtectionEtranger(req.getGarantiesProtectionEtranger());

        d.setTraitement(trt);
        d.setOrigineDeclaration(com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE);

        DeclarationSystemeVideoSurveillance saved = videoRepo.save(d);
        postCreation(dpo, trt, saved, "VIDEO_SURVEILLANCE");
        return toResponse(saved, "VIDEO_SURVEILLANCE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION AUTORISATION
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationAutorisation(DeclarationAutorisationRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        Declaration existante = getDeclarationReutilisable(trt.getIdTraitement());
        if (existante != null && !(existante instanceof DeclarationAutorisation)) {
            declarationRepo.delete(existante);
            existante = null;
        }

        DeclarationAutorisation d = existante != null ? (DeclarationAutorisation) existante : new DeclarationAutorisation();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setFonctionnalitesSysteme(req.getFonctionnalitesSysteme());
        d.setCertificationSecurite(req.getCertificationSecurite());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setDescriptionFichier(req.getDescriptionFichier());
        d.setModeTransfert(req.getModeTransfert());
        d.setTraitementDonneesSante(req.getTraitementDonneesSante());
        d.setProfessionalSante(req.getProfessionalSante());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setDestinataireAdresse(req.getDestinataireAdresse());
        d.setTexteJuridiqueCommunication(req.getTexteJuridiqueCommunication());
        d.setDestinataireNomPrenom(req.getDestinataireNomPrenom());
        d.setConnexionFichiers(req.getConnexionFichiers());
        d.setCategoriesDonneesInterconnexion(req.getCategoriesDonneesInterconnexion());
        d.setDureeInterconnexion(req.getDureeInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());
        d.setTransfertPaysEtranger(req.getTransfertPaysEtranger());
        d.setRecoursSousTraitant(req.getRecoursSousTraitant());
        d.setRolesSousTraitants(req.getRolesSousTraitants());
        d.setCategoriesPersonnesAcces(req.getCategoriesPersonnesAcces());
        d.setPolitiqueAccesBatiments(req.getPolitiqueAccesBatiments());
        d.setMesuresSecurite(req.getMesuresSecurite());
        d.setDescriptionSensibilisation(req.getDescriptionSensibilisation());
        d.setPaysDestinationProtectionDonnees(req.getPaysDestinationProtectionDonnees());
        d.setDescriptionFichierTransfert(req.getDescriptionFichierTransfert());
        d.setNombrePersonnesTransfert(req.getNombrePersonnesTransfert());
        d.setCategoriesDonneesTransfert(req.getCategoriesDonneesTransfert());
        d.setFondementJuridique(req.getFondementJuridique());
        d.setConsentementPersonnesConcernees(req.getConsentementPersonnesConcernees());
        d.setMethodeRecueilConsentement(req.getMethodeRecueilConsentement());
        d.setMesuresSecuriteTransfert(req.getMesuresSecuriteTransfert());
        d.setDureeConservationSante(req.getDureeConservationSante());
        d.setOrigineDonnees(req.getOrigineDonnees());
        d.setFinaliteSante(req.getFinaliteSante());
        d.setPaysDestinationTransfert(req.getPaysDestinationTransfert());
        d.setServiceResponsable(req.getServiceResponsable());
        d.setDateSignature(req.getDateSignature());
        d.setLieuSignature(req.getLieuSignature());
        d.setPaysDestination(req.getPaysDestination());
        d.setGarantiesProtectionEtranger(req.getGarantiesProtectionEtranger());

        d.setTraitement(trt);
        d.setOrigineDeclaration(com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE);

        DeclarationAutorisation saved = autorisationRepo.save(d);
        postCreation(dpo, trt, saved, "AUTORISATION");
        return toResponse(saved, "AUTORISATION", trt);
    }

    // ================================================================== //
    //  SOUMISSION DPO : passe une déclaration BROUILLON → EN_ATTENTE     //
    // ================================================================== //
    @Transactional
    public DeclarationResponse soumettre(Long declarationId, String emailDpo) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        DPO dpo = getDpo(emailDpo);

        if (d.getStatut() != StatutDeclaration.BROUILLON
                && d.getStatut() != StatutDeclaration.REJETEE_DG
                && d.getStatut() != StatutDeclaration.REJETEE_CIL) {
            throw new RuntimeException("Cette déclaration ne peut pas être soumise (statut : " + d.getStatut() + ").");
        }

        d.setStatut(StatutDeclaration.EN_ATTENTE);
        d.setDateSoumission(LocalDate.now());
        d.setMotifRejetCil(null);
        if (d.getDpo() == null) d.setDpo(dpo);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(dpo, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        dgRepo.findAll().stream().findFirst().ifPresent(dg ->
                notificationService.envoyer(dg, TypeNotification.ALERTE,
                        "Nouvelle déclaration #" + declarationId + " soumise par "
                        + dpo.getPrenom() + " " + dpo.getNom()
                        + " — en attente de votre validation."));

        return toResponse(saved, detecterType(saved), null);
    }

    // ================================================================== //
    //  CONSULTATION
    // ================================================================== //
    public List<DeclarationResponse> listerParDpo(Long dpoId) {
        return declarationRepo.findByDpoId(dpoId).stream()
                .filter(d -> d.getOrigineDeclaration() == com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE)
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    public List<DeclarationResponse> listerEnAttente() {
        return declarationRepo.findEnAttenteAvecDpo().stream()
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    /**
     * Historique persisté (table historique_declaration) d'un DPO donné.
     * Contrairement à listerParDpo() (qui renvoie l'état courant des déclarations),
     * ceci renvoie chaque événement du cycle de vie (création, soumission,
     * validation/rejet DG, validation/rejet CIL), conservé en base pour
     * une consultation fiable côté DPO, y compris après rafraîchissement.
     */
    public List<HistoriqueDeclarationResponse> listerHistoriqueParDpo(Long dpoId) {
        return historiqueDeclarationRepo.findByDpoId(dpoId).stream()
                .map(this::toHistoriqueResponse)
                .collect(Collectors.toList());
    }

    /**
     * DG : historique complet de toutes les déclarations traitées (hors brouillons),
     * limité aux déclarations créées manuellement par un DPO (origine MANUELLE).
     * Les déclarations auto-générées en même temps qu'un traitement (origine
     * AUTOMATIQUE) ne doivent jamais apparaître côté DG, même une fois soumises.
     */
    public List<DeclarationResponse> listerHistoriqueDg() {
        return declarationRepo.findAll().stream()
                .filter(d -> d.getDpo() != null
                        && d.getStatut() != StatutDeclaration.BROUILLON
                        && d.getOrigineDeclaration() == com.collecte.projetCIL.enums.OrigineDeclaration.MANUELLE)
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    public List<DeclarationResponse> listerPourCil() {
        return declarationRepo.findByStatut(StatutDeclaration.EN_VERIFICATION_CIL).stream()
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    public DeclarationResponse getById(Long id) {
        Declaration d = declarationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + id));
        return toResponse(d, detecterType(d), null);
    }

    // ================================================================== //
    //  WORKFLOW DG
    // ================================================================== //
    @Transactional
    public DeclarationResponse validerDeclaration(Long declarationId, String emailDg) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        DG dg = dgRepo.findByEmail(emailDg)
                .orElseThrow(() -> new RuntimeException("DG introuvable : " + emailDg));

        d.setStatut(StatutDeclaration.EN_VERIFICATION_CIL);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(dg, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.CONFIRMATION,
                    "Votre déclaration #" + declarationId + " a été approuvée par le DG et transmise à la CIL pour vérification de conformité.");
        }

        cilRepo.findAll().stream().findFirst().ifPresent(cil ->
                notificationService.envoyer(cil, TypeNotification.ALERTE,
                        "Une nouvelle déclaration (#" + declarationId + ") vous a été transmise par le DG pour vérification de conformité."));

        return toResponse(saved, detecterType(saved), null);
    }

    @Transactional
    public DeclarationResponse rejeterDeclaration(Long declarationId, String emailDg, String commentaire) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        DG dg = dgRepo.findByEmail(emailDg)
                .orElseThrow(() -> new RuntimeException("DG introuvable : " + emailDg));

        d.setStatut(StatutDeclaration.REJETEE_DG);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(dg, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.ALERTE,
                    "Votre déclaration #" + declarationId + " a été rejetée par le DG. Motif : " + commentaire
                    + " — Veuillez la corriger et la soumettre à nouveau.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    // ================================================================== //
    //  WORKFLOW CIL
    // ================================================================== //
    @Transactional
    public DeclarationResponse validerConformiteCil(Long declarationId, String emailCil) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        CIL cil = cilRepo.findByEmail(emailCil)
                .orElseThrow(() -> new RuntimeException("CIL introuvable : " + emailCil));

        d.setStatut(StatutDeclaration.VALIDEE_CIL);
        d.setCil(cil);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(cil, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.CONFIRMATION,
                    "Votre déclaration #" + declarationId + " a été validée conforme par la CIL. Le processus est terminé.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    @Transactional
    public DeclarationResponse rejeterConformiteCil(Long declarationId, String emailCil, String commentaire) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        CIL cil = cilRepo.findByEmail(emailCil)
                .orElseThrow(() -> new RuntimeException("CIL introuvable : " + emailCil));

        d.setStatut(StatutDeclaration.REJETEE_CIL);
        d.setCil(cil);
        d.setMotifRejetCil(commentaire);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(cil, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.ALERTE,
                    "Votre déclaration #" + declarationId + " a été jugée non conforme par la CIL. Motif : " + commentaire
                    + " — Veuillez corriger et soumettre à nouveau.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    // ================================================================== //
    //  CIL EXTERNE (clé API — pas de login, aucune fiche CIL en base)
    // ================================================================== //

    @Transactional
    public DeclarationResponse validerConformiteCilExterne(Long declarationId, CleApiCil cle) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        d.setStatut(StatutDeclaration.VALIDEE_CIL);
        d.setCleApiCil(cle);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(null, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.CONFIRMATION,
                    "Votre déclaration #" + declarationId + " a été validée conforme par la CIL ("
                    + cle.getLibelle() + "). Le processus est terminé.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    @Transactional
    public DeclarationResponse rejeterConformiteCilExterne(Long declarationId, CleApiCil cle, String commentaire) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        d.setStatut(StatutDeclaration.REJETEE_CIL);
        d.setCleApiCil(cle);
        d.setMotifRejetCil(commentaire);
        Declaration saved = declarationRepo.save(d);

        journalAuditService.enregistrer(null, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.ALERTE,
                    "Votre déclaration #" + declarationId + " a été jugée non conforme par la CIL ("
                    + cle.getLibelle() + "). Motif : " + commentaire
                    + " — Veuillez corriger et soumettre à nouveau.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    /** Déclarations déjà traitées par un partenaire externe (identifié par sa clé API). */
    public List<HistoriqueDeclarationResponse> listerHistoriqueParCleApi(Long cleApiCilId) {
        // Une déclaration change de statut plusieurs fois pendant son cycle de vie
        // (EN_ATTENTE_DG → EN_VERIFICATION_CIL → VALIDEE_CIL/REJETEE_CIL), et chaque
        // changement crée une ligne d'historique. Pour l'écran "Historique" de la CIL,
        // on ne veut voir QUE la décision finale de la CIL (validée ou rejetée), et une
        // seule fois par déclaration — la plus récente (utile si rejetée puis corrigée
        // et re-décidée).
        return historiqueDeclarationRepo.findByCleApiCilId(cleApiCilId).stream()
                .filter(h -> h.getStatut() == StatutDeclaration.VALIDEE_CIL
                          || h.getStatut() == StatutDeclaration.REJETEE_CIL)
                .collect(Collectors.toMap(
                        h -> h.getDeclaration().getIdDeclaration(),
                        h -> h,
                        (existing, candidate) -> existing.getIdHistorique() >= candidate.getIdHistorique() ? existing : candidate))
                .values().stream()
                .map(this::toHistoriqueResponse)
                .sorted((a, b) -> b.getIdHistorique().compareTo(a.getIdHistorique()))
                .collect(Collectors.toList());
    }

    // ================================================================== //
    //  UPDATE DÉCLARATIONS (4 sous-types)
    // ================================================================== //

    private void verifierModifiable(Declaration d, boolean isAdmin) {
        if (isAdmin) return;
        StatutDeclaration s = d.getStatut();
        if (s == StatutDeclaration.APPROUVEE_DG
                || s == StatutDeclaration.EN_VERIFICATION_CIL
                || s == StatutDeclaration.VALIDEE_CIL
                || s == StatutDeclaration.APPROUVEE) {
            throw new RuntimeException(
                    "Impossible de modifier/supprimer : la déclaration a déjà été validée (statut : " + s + ").");
        }
    }

    @Transactional
    public DeclarationResponse updateDeclarationNormale(Long id, DeclarationNormaleRequest req, String emailUser) {
        DeclarationNormale d = normaleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration normale introuvable : " + id));

        boolean isAdmin = administrateurRepo.findByEmail(emailUser).isPresent();
        verifierModifiable(d, isAdmin);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDescriptionProcedureManuelle(req.getDescriptionProcedureManuelle());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setCaracteristiquesSysteme(req.getCaracteristiquesSysteme());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setProtocoleRecherche(req.getProtocoleRecherche());
        d.setDescriptionConnexionFichiers(req.getDescriptionConnexionFichiers());
        d.setMotifsInterconnexion(req.getMotifsInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());
        d.setIntituleTraitement(req.getIntituleTraitement());
        d.setSupportTraitement(req.getSupportTraitement());
        d.setCategoriesDonneesCollectees(req.getCategoriesDonneesCollectees());
        d.setDonneesSensibles(req.getDonneesSensibles());
        d.setNatureDonneesSensibles(req.getNatureDonneesSensibles());
        mettreAJourChampsBase(d, req);

        DeclarationNormale saved = normaleRepo.save(d);

        DPO dpo = d.getDpo();
        journalAuditService.enregistrer(dpo, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        return toResponse(saved, "NORMALE", saved.getTraitement());
    }

    @Transactional
    public DeclarationResponse updateDeclarationCollecteSite(Long id, DeclarationCollecteSiteInternetRequest req, String emailUser) {
        DeclarationCollecteSiteInternet d = collecteSiteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration collecte site introuvable : " + id));

        boolean isAdmin = administrateurRepo.findByEmail(emailUser).isPresent();
        verifierModifiable(d, isAdmin);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setCaracteristiquesMainStructure(req.getCaracteristiquesMainStructure());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDonneesConnexion(req.getDonneesConnexion());
        d.setDescriptionDonneesConnexion(req.getDescriptionDonneesConnexion());
        d.setCookies(req.getCookies());
        d.setDescriptionCookies(req.getDescriptionCookies());
        d.setDureeConservationCookies(req.getDureeConservationCookies());
        d.setTelechargementTraitement(req.getTelechargementTraitement());
        d.setUrlSite(req.getUrlSite());
        d.setTypeCookies(req.getTypeCookies());
        d.setConsentementCookies(req.getConsentementCookies());
        d.setFormulairesEnLigne(req.getFormulairesEnLigne());
        d.setDonneesFormulaires(req.getDonneesFormulaires());
        mettreAJourChampsBase(d, req);

        DeclarationCollecteSiteInternet saved = collecteSiteRepo.save(d);

        DPO dpo = d.getDpo();
        journalAuditService.enregistrer(dpo, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        return toResponse(saved, "COLLECTE_SITE", saved.getTraitement());
    }

    @Transactional
    public DeclarationResponse updateDeclarationVideoSurveillance(Long id, DeclarationVideoSurveillanceRequest req, String emailUser) {
        DeclarationSystemeVideoSurveillance d = videoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration vidéo surveillance introuvable : " + id));

        boolean isAdmin = administrateurRepo.findByEmail(emailUser).isPresent();
        verifierModifiable(d, isAdmin);

        d.setFinalites(req.getFinalites());
        d.setAdresseInstallation(req.getAdresseInstallation());
        d.setNatureEnvironnement(req.getNatureEnvironnement());
        d.setEmplacementCameras(req.getEmplacementCameras());
        d.setNombreTotalCameras(req.getNombreTotalCameras());
        d.setModeleDispositif(req.getModeleDispositif());
        d.setVisualisationTempsReel(req.getVisualisationTempsReel());
        d.setModeTransfert(req.getModeTransfert());
        d.setSonDeSon(req.getSonDeSon());
        d.setTypeEnregistrement(req.getTypeEnregistrement());
        d.setNatureEnregistrement(req.getNatureEnregistrement());
        d.setLiaisonReseau(req.getLiaisonReseau());
        d.setUtilisationSystemesExperts(req.getUtilisationSystemesExperts());
        d.setDescriptionSystemesExperts(req.getDescriptionSystemesExperts());
        d.setFonctionnalitesTraitement(req.getFonctionnalitesTraitement());
        d.setAccesImagesDistance(req.getAccesImagesDistance());
        d.setAccesPhysique(req.getAccesPhysique());
        d.setAccesLogique(req.getAccesLogique());
        d.setMesuresSuppression(req.getMesuresSuppression());
        d.setAttribute(req.getAttribute());
        d.setLocalisationPictogrammes(req.getLocalisationPictogrammes());
        d.setDureeConservationVideo(req.getDureeConservationVideo());
        d.setModalitesAccesDistance(req.getModalitesAccesDistance());
        d.setPersonnesHabilitees(req.getPersonnesHabilitees());
        mettreAJourChampsBase(d, req);

        DeclarationSystemeVideoSurveillance saved = videoRepo.save(d);

        DPO dpo = d.getDpo();
        journalAuditService.enregistrer(dpo, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        return toResponse(saved, "VIDEO_SURVEILLANCE", saved.getTraitement());
    }

    @Transactional
    public DeclarationResponse updateDeclarationAutorisation(Long id, DeclarationAutorisationRequest req, String emailUser) {
        DeclarationAutorisation d = autorisationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration autorisation introuvable : " + id));

        boolean isAdmin = administrateurRepo.findByEmail(emailUser).isPresent();
        verifierModifiable(d, isAdmin);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setFonctionnalitesSysteme(req.getFonctionnalitesSysteme());
        d.setCertificationSecurite(req.getCertificationSecurite());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setDescriptionFichier(req.getDescriptionFichier());
        d.setModeTransfert(req.getModeTransfert());
        d.setTraitementDonneesSante(req.getTraitementDonneesSante());
        d.setProfessionalSante(req.getProfessionalSante());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setDestinataireAdresse(req.getDestinataireAdresse());
        d.setTexteJuridiqueCommunication(req.getTexteJuridiqueCommunication());
        d.setDestinataireNomPrenom(req.getDestinataireNomPrenom());
        d.setConnexionFichiers(req.getConnexionFichiers());
        d.setCategoriesDonneesInterconnexion(req.getCategoriesDonneesInterconnexion());
        d.setDureeInterconnexion(req.getDureeInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());
        d.setTransfertPaysEtranger(req.getTransfertPaysEtranger());
        d.setRecoursSousTraitant(req.getRecoursSousTraitant());
        d.setRolesSousTraitants(req.getRolesSousTraitants());
        d.setCategoriesPersonnesAcces(req.getCategoriesPersonnesAcces());
        d.setPolitiqueAccesBatiments(req.getPolitiqueAccesBatiments());
        d.setMesuresSecurite(req.getMesuresSecurite());
        d.setDescriptionSensibilisation(req.getDescriptionSensibilisation());
        d.setPaysDestinationProtectionDonnees(req.getPaysDestinationProtectionDonnees());
        d.setDescriptionFichierTransfert(req.getDescriptionFichierTransfert());
        d.setNombrePersonnesTransfert(req.getNombrePersonnesTransfert());
        d.setCategoriesDonneesTransfert(req.getCategoriesDonneesTransfert());
        d.setFondementJuridique(req.getFondementJuridique());
        d.setConsentementPersonnesConcernees(req.getConsentementPersonnesConcernees());
        d.setMethodeRecueilConsentement(req.getMethodeRecueilConsentement());
        d.setMesuresSecuriteTransfert(req.getMesuresSecuriteTransfert());
        d.setDureeConservationSante(req.getDureeConservationSante());
        d.setOrigineDonnees(req.getOrigineDonnees());
        d.setFinaliteSante(req.getFinaliteSante());
        d.setPaysDestinationTransfert(req.getPaysDestinationTransfert());
        mettreAJourChampsBase(d, req);

        DeclarationAutorisation saved = autorisationRepo.save(d);

        DPO dpo = d.getDpo();
        journalAuditService.enregistrer(dpo, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        return toResponse(saved, "AUTORISATION", saved.getTraitement());
    }

    // ================================================================== //
    //  SUPPRESSION DÉCLARATION
    // ================================================================== //

    @Transactional
    public void deleteDeclaration(Long id, String emailUser) {
        Declaration d = declarationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + id));

        boolean isAdmin = administrateurRepo.findByEmail(emailUser).isPresent();
        verifierModifiable(d, isAdmin);

        declarationRepo.delete(d);

        DPO dpo = d.getDpo();
        journalAuditService.enregistrer(dpo, TypeAction.SUPPRESSION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
    }

    // ================================================================== //
    //  HELPERS PRIVÉS
    // ================================================================== //
    /**
     * Persiste un événement d'historique pour la déclaration donnée, avec son
     * statut ACTUEL au moment de l'appel. À appeler juste après chaque
     * changement de statut sauvegardé en base (creerDeclaration*, soumettre,
     * validerDeclaration, rejeterDeclaration, validerConformiteCil, rejeterConformiteCil).
     */
    private void enregistrerHistorique(Declaration d) {
        HistoriqueDeclaration h = new HistoriqueDeclaration();
        h.setDateDeclaration(LocalDate.now());
        h.setResponsableDeclaration(
                d.getResponsableDeclaration() != null
                        ? d.getResponsableDeclaration()
                        : (d.getDpo() != null ? d.getDpo().getPrenom() + " " + d.getDpo().getNom() : "N/A"));
        h.setStatut(d.getStatut());
        h.setDeclaration(d);
        historiqueDeclarationRepo.save(h);
    }

    private HistoriqueDeclarationResponse toHistoriqueResponse(HistoriqueDeclaration h) {
        HistoriqueDeclarationResponse r = new HistoriqueDeclarationResponse();
        r.setIdHistorique(h.getIdHistorique());
        r.setDateDeclaration(h.getDateDeclaration());
        r.setResponsableDeclaration(h.getResponsableDeclaration());
        r.setStatut(h.getStatut());

        Declaration d = h.getDeclaration();
        if (d != null) {
            r.setIdDeclaration(d.getIdDeclaration());
            r.setTypeDeclaration(detecterType(d));
            r.setIntitule(extraireIntitule(d));
            r.setMotifRejetCil(d.getMotifRejetCil());
            if (d.getTraitement() != null) {
                r.setTraitementId(d.getTraitement().getIdTraitement());
                r.setTraitementNom(d.getTraitement().getNom());
            }
        }
        return r;
    }

    /** Extrait un intitulé lisible selon le sous-type concret de la déclaration. */
    private String extraireIntitule(Declaration d) {
        if (d instanceof DeclarationNormale dn) return dn.getDenominationTraitement();
        if (d instanceof DeclarationCollecteSiteInternet dc) return dc.getDenominationTraitement();
        if (d instanceof DeclarationAutorisation da) return da.getDenominationTraitement();
        if (d instanceof DeclarationSystemeVideoSurveillance dv) return dv.getFinalites();
        return null;
    }

    private void postCreation(DPO dpo, Traitement trt, Declaration saved, String type) {
        journalAuditService.enregistrer(dpo, TypeAction.CREATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);
        enregistrerHistorique(saved);

        dgRepo.findAll().stream().findFirst().ifPresent(dg ->
                notificationService.envoyer(dg, TypeNotification.ALERTE,
                        "Nouvelle déclaration de type " + type + " soumise par "
                        + dpo.getPrenom() + " " + dpo.getNom()
                        + " — en attente de votre validation. (#" + saved.getIdDeclaration() + ")"));
    }

    private void mettreAJourChampsBase(Declaration d, Object req) {
        if (req instanceof DeclarationNormaleRequest r) {
            if (r.getDateSoumission() != null) d.setDateSoumission(r.getDateSoumission());
            if (r.getSecteur() != null) d.setSecteur(r.getSecteur());
            if (r.getNatureDemande() != null) d.setNatureDemande(r.getNatureDemande());
            if (r.getResponsableDeclaration() != null) d.setResponsableDeclaration(r.getResponsableDeclaration());
            if (r.getContactConfidentialite() != null) d.setContactConfidentialite(r.getContactConfidentialite());
            if (r.getDateMiseEnOeuvre() != null) d.setDateMiseEnOeuvre(r.getDateMiseEnOeuvre());
            if (r.getCategoriesDonnees() != null) d.setCategoriesDonnees(r.getCategoriesDonnees());
            if (r.getOrigineDonnees() != null) d.setOrigineDonnees(r.getOrigineDonnees());
            if (r.getDureeConservation() != null) d.setDureeConservation(r.getDureeConservation());
            if (r.getLieuStockage() != null) d.setLieuStockage(r.getLieuStockage());
            if (r.getCommunicationAutresOrganismes() != null) d.setCommunicationAutresOrganismes(r.getCommunicationAutresOrganismes());
            if (r.getDestinataireNom() != null) d.setDestinataireNom(r.getDestinataireNom());
            if (r.getDestinataireAdresse() != null) d.setDestinataireAdresse(r.getDestinataireAdresse());
            if (r.getTexteJuridiqueCommunication() != null) d.setTexteJuridiqueCommunication(r.getTexteJuridiqueCommunication());
            if (r.getFinaliteCommunication() != null) d.setFinaliteCommunication(r.getFinaliteCommunication());
            if (r.getDestinataireConformeCil() != null) d.setDestinataireConformeCil(r.getDestinataireConformeCil());
            if (r.getTransfertPaysEtranger() != null) d.setTransfertPaysEtranger(r.getTransfertPaysEtranger());
            if (r.getRecoursSousTraitant() != null) d.setRecoursSousTraitant(r.getRecoursSousTraitant());
            if (r.getContratConfidentialiteSousTraitant() != null) d.setContratConfidentialiteSousTraitant(r.getContratConfidentialiteSousTraitant());
            if (r.getRolesSousTraitants() != null) d.setRolesSousTraitants(r.getRolesSousTraitants());
            if (r.getCategoriesPersonnesAcces() != null) d.setCategoriesPersonnesAcces(r.getCategoriesPersonnesAcces());
            if (r.getPolitiqueAccesBatiments() != null) d.setPolitiqueAccesBatiments(r.getPolitiqueAccesBatiments());
            if (r.getMesuresSecurite() != null) d.setMesuresSecurite(r.getMesuresSecurite());
            if (r.getMesuresSensibilisation() != null) d.setMesuresSensibilisation(r.getMesuresSensibilisation());
            if (r.getMoyensInformationDroits() != null) d.setMoyensInformationDroits(r.getMoyensInformationDroits());
            if (r.getMoyensExerciceDroits() != null) d.setMoyensExerciceDroits(r.getMoyensExerciceDroits());
            if (r.getCoordonneesExerciceDroits() != null) d.setCoordonneesExerciceDroits(r.getCoordonneesExerciceDroits());
            if (r.getDelaiCommunicationDroits() != null) d.setDelaiCommunicationDroits(r.getDelaiCommunicationDroits());
            if (r.getNomPrenomResponsable() != null) d.setNomPrenomResponsable(r.getNomPrenomResponsable());
            if (r.getFonctionResponsable() != null) d.setFonctionResponsable(r.getFonctionResponsable());
            if (r.getServiceResponsable() != null) d.setServiceResponsable(r.getServiceResponsable());
            if (r.getDateSignature() != null) d.setDateSignature(r.getDateSignature());
            if (r.getLieuSignature() != null) d.setLieuSignature(r.getLieuSignature());
            if (r.getPaysDestination() != null) d.setPaysDestination(r.getPaysDestination());
            if (r.getGarantiesProtectionEtranger() != null) d.setGarantiesProtectionEtranger(r.getGarantiesProtectionEtranger());
        } else if (req instanceof DeclarationCollecteSiteInternetRequest r) {
            if (r.getDateSoumission() != null) d.setDateSoumission(r.getDateSoumission());
            if (r.getSecteur() != null) d.setSecteur(r.getSecteur());
            if (r.getNatureDemande() != null) d.setNatureDemande(r.getNatureDemande());
            if (r.getResponsableDeclaration() != null) d.setResponsableDeclaration(r.getResponsableDeclaration());
            if (r.getContactConfidentialite() != null) d.setContactConfidentialite(r.getContactConfidentialite());
            if (r.getDateMiseEnOeuvre() != null) d.setDateMiseEnOeuvre(r.getDateMiseEnOeuvre());
            if (r.getCategoriesDonnees() != null) d.setCategoriesDonnees(r.getCategoriesDonnees());
            if (r.getOrigineDonnees() != null) d.setOrigineDonnees(r.getOrigineDonnees());
            if (r.getDureeConservation() != null) d.setDureeConservation(r.getDureeConservation());
            if (r.getLieuStockage() != null) d.setLieuStockage(r.getLieuStockage());
            if (r.getCommunicationAutresOrganismes() != null) d.setCommunicationAutresOrganismes(r.getCommunicationAutresOrganismes());
            if (r.getDestinataireNom() != null) d.setDestinataireNom(r.getDestinataireNom());
            if (r.getDestinataireAdresse() != null) d.setDestinataireAdresse(r.getDestinataireAdresse());
            if (r.getTexteJuridiqueCommunication() != null) d.setTexteJuridiqueCommunication(r.getTexteJuridiqueCommunication());
            if (r.getFinaliteCommunication() != null) d.setFinaliteCommunication(r.getFinaliteCommunication());
            if (r.getDestinataireConformeCil() != null) d.setDestinataireConformeCil(r.getDestinataireConformeCil());
            if (r.getTransfertPaysEtranger() != null) d.setTransfertPaysEtranger(r.getTransfertPaysEtranger());
            if (r.getRecoursSousTraitant() != null) d.setRecoursSousTraitant(r.getRecoursSousTraitant());
            if (r.getContratConfidentialiteSousTraitant() != null) d.setContratConfidentialiteSousTraitant(r.getContratConfidentialiteSousTraitant());
            if (r.getRolesSousTraitants() != null) d.setRolesSousTraitants(r.getRolesSousTraitants());
            if (r.getCategoriesPersonnesAcces() != null) d.setCategoriesPersonnesAcces(r.getCategoriesPersonnesAcces());
            if (r.getPolitiqueAccesBatiments() != null) d.setPolitiqueAccesBatiments(r.getPolitiqueAccesBatiments());
            if (r.getMesuresSecurite() != null) d.setMesuresSecurite(r.getMesuresSecurite());
            if (r.getMesuresSensibilisation() != null) d.setMesuresSensibilisation(r.getMesuresSensibilisation());
            if (r.getMoyensInformationDroits() != null) d.setMoyensInformationDroits(r.getMoyensInformationDroits());
            if (r.getMoyensExerciceDroits() != null) d.setMoyensExerciceDroits(r.getMoyensExerciceDroits());
            if (r.getCoordonneesExerciceDroits() != null) d.setCoordonneesExerciceDroits(r.getCoordonneesExerciceDroits());
            if (r.getDelaiCommunicationDroits() != null) d.setDelaiCommunicationDroits(r.getDelaiCommunicationDroits());
            if (r.getNomPrenomResponsable() != null) d.setNomPrenomResponsable(r.getNomPrenomResponsable());
            if (r.getFonctionResponsable() != null) d.setFonctionResponsable(r.getFonctionResponsable());
            if (r.getServiceResponsable() != null) d.setServiceResponsable(r.getServiceResponsable());
            if (r.getDateSignature() != null) d.setDateSignature(r.getDateSignature());
            if (r.getLieuSignature() != null) d.setLieuSignature(r.getLieuSignature());
            if (r.getPaysDestination() != null) d.setPaysDestination(r.getPaysDestination());
            if (r.getGarantiesProtectionEtranger() != null) d.setGarantiesProtectionEtranger(r.getGarantiesProtectionEtranger());
        } else if (req instanceof DeclarationVideoSurveillanceRequest r) {
            if (r.getDateSoumission() != null) d.setDateSoumission(r.getDateSoumission());
            if (r.getSecteur() != null) d.setSecteur(r.getSecteur());
            if (r.getNatureDemande() != null) d.setNatureDemande(r.getNatureDemande());
            if (r.getResponsableDeclaration() != null) d.setResponsableDeclaration(r.getResponsableDeclaration());
            if (r.getContactConfidentialite() != null) d.setContactConfidentialite(r.getContactConfidentialite());
            if (r.getDateMiseEnOeuvre() != null) d.setDateMiseEnOeuvre(r.getDateMiseEnOeuvre());
            if (r.getCategoriesDonnees() != null) d.setCategoriesDonnees(r.getCategoriesDonnees());
            if (r.getOrigineDonnees() != null) d.setOrigineDonnees(r.getOrigineDonnees());
            if (r.getDureeConservation() != null) d.setDureeConservation(r.getDureeConservation());
            if (r.getLieuStockage() != null) d.setLieuStockage(r.getLieuStockage());
            if (r.getCommunicationAutresOrganismes() != null) d.setCommunicationAutresOrganismes(r.getCommunicationAutresOrganismes());
            if (r.getDestinataireNom() != null) d.setDestinataireNom(r.getDestinataireNom());
            if (r.getDestinataireAdresse() != null) d.setDestinataireAdresse(r.getDestinataireAdresse());
            if (r.getTexteJuridiqueCommunication() != null) d.setTexteJuridiqueCommunication(r.getTexteJuridiqueCommunication());
            if (r.getFinaliteCommunication() != null) d.setFinaliteCommunication(r.getFinaliteCommunication());
            if (r.getDestinataireConformeCil() != null) d.setDestinataireConformeCil(r.getDestinataireConformeCil());
            if (r.getTransfertPaysEtranger() != null) d.setTransfertPaysEtranger(r.getTransfertPaysEtranger());
            if (r.getRecoursSousTraitant() != null) d.setRecoursSousTraitant(r.getRecoursSousTraitant());
            if (r.getContratConfidentialiteSousTraitant() != null) d.setContratConfidentialiteSousTraitant(r.getContratConfidentialiteSousTraitant());
            if (r.getRolesSousTraitants() != null) d.setRolesSousTraitants(r.getRolesSousTraitants());
            if (r.getCategoriesPersonnesAcces() != null) d.setCategoriesPersonnesAcces(r.getCategoriesPersonnesAcces());
            if (r.getPolitiqueAccesBatiments() != null) d.setPolitiqueAccesBatiments(r.getPolitiqueAccesBatiments());
            if (r.getMesuresSecurite() != null) d.setMesuresSecurite(r.getMesuresSecurite());
            if (r.getMesuresSensibilisation() != null) d.setMesuresSensibilisation(r.getMesuresSensibilisation());
            if (r.getMoyensInformationDroits() != null) d.setMoyensInformationDroits(r.getMoyensInformationDroits());
            if (r.getMoyensExerciceDroits() != null) d.setMoyensExerciceDroits(r.getMoyensExerciceDroits());
            if (r.getCoordonneesExerciceDroits() != null) d.setCoordonneesExerciceDroits(r.getCoordonneesExerciceDroits());
            if (r.getDelaiCommunicationDroits() != null) d.setDelaiCommunicationDroits(r.getDelaiCommunicationDroits());
            if (r.getNomPrenomResponsable() != null) d.setNomPrenomResponsable(r.getNomPrenomResponsable());
            if (r.getFonctionResponsable() != null) d.setFonctionResponsable(r.getFonctionResponsable());
            if (r.getServiceResponsable() != null) d.setServiceResponsable(r.getServiceResponsable());
            if (r.getDateSignature() != null) d.setDateSignature(r.getDateSignature());
            if (r.getLieuSignature() != null) d.setLieuSignature(r.getLieuSignature());
            if (r.getPaysDestination() != null) d.setPaysDestination(r.getPaysDestination());
            if (r.getGarantiesProtectionEtranger() != null) d.setGarantiesProtectionEtranger(r.getGarantiesProtectionEtranger());
        } else if (req instanceof DeclarationAutorisationRequest r) {
            if (r.getDateSoumission() != null) d.setDateSoumission(r.getDateSoumission());
            if (r.getSecteur() != null) d.setSecteur(r.getSecteur());
            if (r.getNatureDemande() != null) d.setNatureDemande(r.getNatureDemande());
            if (r.getResponsableDeclaration() != null) d.setResponsableDeclaration(r.getResponsableDeclaration());
            if (r.getContactConfidentialite() != null) d.setContactConfidentialite(r.getContactConfidentialite());
            if (r.getDateMiseEnOeuvre() != null) d.setDateMiseEnOeuvre(r.getDateMiseEnOeuvre());
            if (r.getCategoriesDonnees() != null) d.setCategoriesDonnees(r.getCategoriesDonnees());
            if (r.getOrigineDonnees() != null) d.setOrigineDonnees(r.getOrigineDonnees());
            if (r.getDureeConservation() != null) d.setDureeConservation(r.getDureeConservation());
            if (r.getLieuStockage() != null) d.setLieuStockage(r.getLieuStockage());
            if (r.getCommunicationAutresOrganismes() != null) d.setCommunicationAutresOrganismes(r.getCommunicationAutresOrganismes());
            if (r.getDestinataireNom() != null) d.setDestinataireNom(r.getDestinataireNom());
            if (r.getDestinataireAdresse() != null) d.setDestinataireAdresse(r.getDestinataireAdresse());
            if (r.getTexteJuridiqueCommunication() != null) d.setTexteJuridiqueCommunication(r.getTexteJuridiqueCommunication());
            if (r.getFinaliteCommunication() != null) d.setFinaliteCommunication(r.getFinaliteCommunication());
            if (r.getDestinataireConformeCil() != null) d.setDestinataireConformeCil(r.getDestinataireConformeCil());
            if (r.getTransfertPaysEtranger() != null) d.setTransfertPaysEtranger(r.getTransfertPaysEtranger());
            if (r.getRecoursSousTraitant() != null) d.setRecoursSousTraitant(r.getRecoursSousTraitant());
            if (r.getContratConfidentialiteSousTraitant() != null) d.setContratConfidentialiteSousTraitant(r.getContratConfidentialiteSousTraitant());
            if (r.getRolesSousTraitants() != null) d.setRolesSousTraitants(r.getRolesSousTraitants());
            if (r.getCategoriesPersonnesAcces() != null) d.setCategoriesPersonnesAcces(r.getCategoriesPersonnesAcces());
            if (r.getPolitiqueAccesBatiments() != null) d.setPolitiqueAccesBatiments(r.getPolitiqueAccesBatiments());
            if (r.getMesuresSecurite() != null) d.setMesuresSecurite(r.getMesuresSecurite());
            if (r.getMesuresSensibilisation() != null) d.setMesuresSensibilisation(r.getMesuresSensibilisation());
            if (r.getMoyensInformationDroits() != null) d.setMoyensInformationDroits(r.getMoyensInformationDroits());
            if (r.getMoyensExerciceDroits() != null) d.setMoyensExerciceDroits(r.getMoyensExerciceDroits());
            if (r.getCoordonneesExerciceDroits() != null) d.setCoordonneesExerciceDroits(r.getCoordonneesExerciceDroits());
            if (r.getDelaiCommunicationDroits() != null) d.setDelaiCommunicationDroits(r.getDelaiCommunicationDroits());
            if (r.getNomPrenomResponsable() != null) d.setNomPrenomResponsable(r.getNomPrenomResponsable());
            if (r.getFonctionResponsable() != null) d.setFonctionResponsable(r.getFonctionResponsable());
            if (r.getServiceResponsable() != null) d.setServiceResponsable(r.getServiceResponsable());
            if (r.getDateSignature() != null) d.setDateSignature(r.getDateSignature());
            if (r.getLieuSignature() != null) d.setLieuSignature(r.getLieuSignature());
            if (r.getPaysDestination() != null) d.setPaysDestination(r.getPaysDestination());
            if (r.getGarantiesProtectionEtranger() != null) d.setGarantiesProtectionEtranger(r.getGarantiesProtectionEtranger());
            if (r.getDescriptionSensibilisation() != null && d instanceof DeclarationAutorisation da) da.setDescriptionSensibilisation(r.getDescriptionSensibilisation());
        }
    }

    private void remplirChampBase(Declaration d,
                                LocalDate dateSoumission, String secteur, NatureDemande nature,
                                String responsable, String contact, LocalDate dateMiseEnOeuvre,
                                String categoriesDonnees, String origineDonnees, String dureeConservation,
                                String lieuStockage, Boolean communicationAutresOrganismes,
                                String destinataireNom, String destinataireAdresse,
                                String texteJuridiqueCommunication, String finaliteCommunication,
                                Boolean destinataireConformeCil, Boolean transfertPaysEtranger,
                                Boolean recoursSousTraitant, Boolean contratConfidentialiteSousTraitant,
                                String rolesSousTraitants, String categoriesPersonnesAcces,
                                Boolean politiqueAccesBatiments, String mesuresSecurite,
                                Boolean mesuresSensibilisation, String moyensInformationDroits,
                                String moyensExerciceDroits, String coordonneesExerciceDroits,
                                String delaiCommunicationDroits, String nomPrenomResponsable,
                                String fonctionResponsable, DPO dpo) {

        d.setDateSoumission(dateSoumission != null ? dateSoumission : LocalDate.now());
        d.setSecteur(secteur);
        d.setNatureDemande(nature);
        d.setResponsableDeclaration(responsable);
        d.setContactConfidentialite(contact);
        d.setDateMiseEnOeuvre(dateMiseEnOeuvre);
        d.setCategoriesDonnees(categoriesDonnees);
        d.setOrigineDonnees(origineDonnees);
        d.setDureeConservation(dureeConservation);
        d.setLieuStockage(lieuStockage);
        d.setCommunicationAutresOrganismes(communicationAutresOrganismes);
        d.setDestinataireNom(destinataireNom);
        d.setDestinataireAdresse(destinataireAdresse);
        d.setTexteJuridiqueCommunication(texteJuridiqueCommunication);
        d.setFinaliteCommunication(finaliteCommunication);
        d.setDestinataireConformeCil(destinataireConformeCil);
        d.setTransfertPaysEtranger(transfertPaysEtranger);
        d.setRecoursSousTraitant(recoursSousTraitant);
        d.setContratConfidentialiteSousTraitant(contratConfidentialiteSousTraitant);
        d.setRolesSousTraitants(rolesSousTraitants);
        d.setCategoriesPersonnesAcces(categoriesPersonnesAcces);
        d.setPolitiqueAccesBatiments(politiqueAccesBatiments);
        d.setMesuresSecurite(mesuresSecurite);
        d.setMesuresSensibilisation(mesuresSensibilisation);
        d.setMoyensInformationDroits(moyensInformationDroits);
        d.setMoyensExerciceDroits(moyensExerciceDroits);
        d.setCoordonneesExerciceDroits(coordonneesExerciceDroits);
        d.setDelaiCommunicationDroits(delaiCommunicationDroits);
        d.setNomPrenomResponsable(nomPrenomResponsable);
        d.setFonctionResponsable(fonctionResponsable);
        d.setStatut(StatutDeclaration.EN_ATTENTE);
        d.setDpo(dpo);
    }

    private DPO getDpo(String email) {
        return dpoRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DPO introuvable : " + email));
    }

    private Traitement getTraitement(Long id) {
        return traitementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
    }

    /**
     * Retourne la déclaration existante d'un traitement (le brouillon
     * auto-créé à la création du traitement, ou une déclaration déjà
     * complétée) si elle peut encore être réutilisée/complétée, càd si
     * elle n'a pas encore été soumise (statut BROUILLON ou REJETEE_*).
     *
     * Si le traitement a déjà une déclaration SOUMISE, on lève une
     * exception : un traitement ne peut être déclaré qu'une seule fois.
     */
    private Declaration getDeclarationReutilisable(Long traitementId) {
        List<Declaration> existantes = declarationRepo.findAllByTraitement_IdTraitementOrderByDateSoumissionDesc(traitementId);
        if (existantes == null || existantes.isEmpty()) return null;

        Declaration plusRecente = existantes.get(0);
        StatutDeclaration s = plusRecente.getStatut();

        boolean dejaSoumise = s != null
                && s != StatutDeclaration.BROUILLON
                && s != StatutDeclaration.REJETEE_DG
                && s != StatutDeclaration.REJETEE_CIL;

        if (dejaSoumise) {
            throw new RuntimeException(
                    "Ce traitement a déjà été déclaré (déclaration #" + plusRecente.getIdDeclaration()
                            + ", statut : " + s + "). Un traitement ne peut être déclaré qu'une seule fois.");
        }
        return plusRecente;
    }

    /**
     * Retourne la déclaration (brouillon ou existante, avec toutes ses
     * données) associée à un traitement, pour pré-remplissage du formulaire
     * côté DPO. Retourne null si aucune déclaration n'existe encore.
     */
    @Transactional(readOnly = true)
    public DeclarationResponse getBrouillonByTraitement(Long traitementId) {
        List<Declaration> existantes = declarationRepo.findAllByTraitement_IdTraitementOrderByDateSoumissionDesc(traitementId);
        if (existantes == null || existantes.isEmpty()) return null;
        Declaration d = existantes.get(0);
        return toResponse(d, detecterType(d), d.getTraitement());
    }

    private String detecterType(Declaration d) {
        if (d instanceof DeclarationNormale)                      return "NORMALE";
        if (d instanceof DeclarationCollecteSiteInternet)         return "COLLECTE_SITE";
        if (d instanceof DeclarationSystemeVideoSurveillance)     return "VIDEO_SURVEILLANCE";
        if (d instanceof DeclarationAutorisation)                 return "AUTORISATION";
        return "INCONNUE";
    }

    private DeclarationResponse toResponse(Declaration d, String type, Traitement trt) {
        DeclarationResponse r = new DeclarationResponse();

        r.setIdDeclaration(d.getIdDeclaration());
        r.setTypeDeclaration(type);
        r.setDateSoumission(d.getDateSoumission());
        r.setStatut(d.getStatut());
        r.setMotifRejetCil(d.getMotifRejetCil());
        r.setOrigineDeclaration(d.getOrigineDeclaration() != null ? d.getOrigineDeclaration().name() : "MANUELLE");

        Traitement effectiveTrt = trt != null ? trt : d.getTraitement();
        if (effectiveTrt != null) {
            r.setTraitementId(effectiveTrt.getIdTraitement());
            r.setTraitementDescription(effectiveTrt.getDescription());
            r.setTraitementNom(effectiveTrt.getNom());
        }
        if (d.getDpo() != null) {
            r.setDpoId(d.getDpo().getId());
            r.setDpoNomPrenom(d.getDpo().getPrenom() + " " + d.getDpo().getNom());
        }
        if (d.getCil() != null) {
            r.setCilId(d.getCil().getId());
            r.setCilNomPrenom(d.getCil().getPrenom() + " " + d.getCil().getNom());
        }

        r.setSecteur(d.getSecteur());
        r.setNatureDemande(d.getNatureDemande());
        r.setResponsableDeclaration(d.getResponsableDeclaration());
        r.setContactConfidentialite(d.getContactConfidentialite());
        r.setDateMiseEnOeuvre(d.getDateMiseEnOeuvre());
        r.setCategoriesDonnees(d.getCategoriesDonnees());
        r.setOrigineDonnees(d.getOrigineDonnees());
        r.setDureeConservation(d.getDureeConservation());
        r.setLieuStockage(d.getLieuStockage());
        r.setCommunicationAutresOrganismes(d.getCommunicationAutresOrganismes());
        r.setDestinataireNom(d.getDestinataireNom());
        r.setDestinataireAdresse(d.getDestinataireAdresse());
        r.setTexteJuridiqueCommunication(d.getTexteJuridiqueCommunication());
        r.setFinaliteCommunication(d.getFinaliteCommunication());
        r.setDestinataireConformeCil(d.getDestinataireConformeCil());
        r.setTransfertPaysEtranger(d.getTransfertPaysEtranger());
        r.setPaysDestination(d.getPaysDestination());
        r.setGarantiesProtectionEtranger(d.getGarantiesProtectionEtranger());
        r.setRecoursSousTraitant(d.getRecoursSousTraitant());
        r.setContratConfidentialiteSousTraitant(d.getContratConfidentialiteSousTraitant());
        r.setRolesSousTraitants(d.getRolesSousTraitants());
        r.setCategoriesPersonnesAcces(d.getCategoriesPersonnesAcces());
        r.setPolitiqueAccesBatiments(d.getPolitiqueAccesBatiments());
        r.setMesuresSecurite(d.getMesuresSecurite());
        r.setMesuresSensibilisation(d.getMesuresSensibilisation());
        r.setMoyensInformationDroits(d.getMoyensInformationDroits());
        r.setMoyensExerciceDroits(d.getMoyensExerciceDroits());
        r.setCoordonneesExerciceDroits(d.getCoordonneesExerciceDroits());
        r.setDelaiCommunicationDroits(d.getDelaiCommunicationDroits());
        r.setNomPrenomResponsable(d.getNomPrenomResponsable());
        r.setFonctionResponsable(d.getFonctionResponsable());
        r.setServiceResponsable(d.getServiceResponsable());
        r.setDateSignature(d.getDateSignature());
        r.setLieuSignature(d.getLieuSignature());

        if (d instanceof DeclarationNormale dn) {
            r.setDenominationTraitement(dn.getDenominationTraitement());
            r.setFinaliteTraitement(dn.getFinaliteTraitement());
            r.setTexteJuridique(dn.getTexteJuridique());
            r.setCategoriesPersonnesConcernees(dn.getCategoriesPersonnesConcernees());
            r.setNombrePersonnesConcernees(dn.getNombrePersonnesConcernees());
            r.setTypeTraitement(dn.getTypeTraitement());
            r.setCaracteristiquesTechniques(dn.getCaracteristiquesTechniques());
            r.setDescriptionProcedureManuelle(dn.getDescriptionProcedureManuelle());
            r.setCaracteristiquesSysteme(dn.getCaracteristiquesSysteme());
            r.setPolitiqueAccesSystemes(dn.getPolitiqueAccesSystemes());
            r.setModalitesDiffusionResultatsBool(dn.getModalitesDiffusionResultats());
            r.setProtocoleRecherche(dn.getProtocoleRecherche());
            r.setDescriptionConnexionFichiers(dn.getDescriptionConnexionFichiers());
            r.setMotifsInterconnexion(dn.getMotifsInterconnexion());
            r.setIdentiteFichiersInterconnexion(dn.getIdentiteFichiersInterconnexion());
            r.setIntituleTraitement(dn.getIntituleTraitement());
            r.setSupportTraitement(dn.getSupportTraitement());
            r.setCategoriesDonneesCollectees(dn.getCategoriesDonneesCollectees());
            r.setDonneesSensibles(dn.getDonneesSensibles());
            r.setNatureDonneesSensibles(dn.getNatureDonneesSensibles());
        } else if (d instanceof DeclarationCollecteSiteInternet dc) {
            r.setDenominationTraitement(dc.getDenominationTraitement());
            r.setFinaliteTraitement(dc.getFinaliteTraitement());
            r.setTexteJuridique(dc.getTexteJuridique());
            r.setCategoriesPersonnesConcernees(dc.getCategoriesPersonnesConcernees());
            r.setCaracteristiquesMainStructure(dc.getCaracteristiquesMainStructure());
            r.setCaracteristiquesTechniques(dc.getCaracteristiquesTechniques());
            r.setTypeTraitement(dc.getTypeTraitement());
            r.setDonneesConnexion(dc.getDonneesConnexion());
            r.setDescriptionDonneesConnexion(dc.getDescriptionDonneesConnexion());
            r.setCookies(dc.getCookies());
            r.setDescriptionCookies(dc.getDescriptionCookies());
            r.setDureeConservationCookies(dc.getDureeConservationCookies());
            r.setTelechargementTraitement(dc.getTelechargementTraitement());
            r.setUrlSite(dc.getUrlSite());
            r.setTypeCookies(dc.getTypeCookies());
            r.setConsentementCookies(dc.getConsentementCookies());
            r.setFormulairesEnLigne(dc.getFormulairesEnLigne());
            r.setDonneesFormulaires(dc.getDonneesFormulaires());
        } else if (d instanceof DeclarationSystemeVideoSurveillance dv) {
            r.setFinalites(dv.getFinalites());
            r.setAdresseInstallation(dv.getAdresseInstallation());
            r.setNatureEnvironnement(dv.getNatureEnvironnement());
            r.setEmplacementCameras(dv.getEmplacementCameras());
            r.setNombreTotalCameras(dv.getNombreTotalCameras());
            r.setModeleDispositif(dv.getModeleDispositif());
            r.setVisualisationTempsReel(dv.getVisualisationTempsReel());
            r.setModeTransfert(dv.getModeTransfert());
            r.setSonDeSon(dv.getSonDeSon());
            r.setTypeEnregistrement(dv.getTypeEnregistrement());
            r.setNatureEnregistrement(dv.getNatureEnregistrement());
            r.setLiaisonReseau(dv.getLiaisonReseau());
            r.setUtilisationSystemesExperts(dv.getUtilisationSystemesExperts());
            r.setDescriptionSystemesExperts(dv.getDescriptionSystemesExperts());
            r.setFonctionnalitesTraitement(dv.getFonctionnalitesTraitement());
            r.setAccesImagesDistance(dv.getAccesImagesDistance());
            r.setAccesPhysique(dv.getAccesPhysique());
            r.setAccesLogique(dv.getAccesLogique());
            r.setMesuresSuppression(dv.getMesuresSuppression());
            r.setAttribute(dv.getAttribute());
            r.setLocalisationPictogrammes(dv.getLocalisationPictogrammes());
            r.setDureeConservationVideo(dv.getDureeConservationVideo());
            r.setModalitesAccesDistance(dv.getModalitesAccesDistance());
            r.setPersonnesHabilitees(dv.getPersonnesHabilitees());
        } else if (d instanceof DeclarationAutorisation da) {
            r.setDenominationTraitement(da.getDenominationTraitement());
            r.setFinaliteTraitement(da.getFinaliteTraitement());
            r.setTexteJuridique(da.getTexteJuridique());
            r.setCategoriesPersonnesConcernees(da.getCategoriesPersonnesConcernees());
            r.setNombrePersonnesConcernees(da.getNombrePersonnesConcernees());
            r.setTypeTraitement(da.getTypeTraitement());
            r.setCaracteristiquesTechniques(da.getCaracteristiquesTechniques());
            r.setModeTransfert(da.getModeTransfert());
            r.setCertificationSecurite(da.getCertificationSecurite());
            r.setFonctionnalitesSysteme(da.getFonctionnalitesSysteme());
            r.setPolitiqueAccesSystemes(da.getPolitiqueAccesSystemes());
            r.setDescriptionFichier(da.getDescriptionFichier());
            r.setTraitementDonneesSante(da.getTraitementDonneesSante());
            r.setProfessionalSante(da.getProfessionalSante());
            r.setModalitesDiffusionResultats(da.getModalitesDiffusionResultats());
            r.setDestinataireCie(da.getDestinataireCie());
            r.setConnexionFichiers(da.getConnexionFichiers());
            r.setCategoriesDonneesInterconnexion(da.getCategoriesDonneesInterconnexion());
            r.setDureeInterconnexion(da.getDureeInterconnexion());
            r.setIdentiteFichiersInterconnexion(da.getIdentiteFichiersInterconnexion());
            r.setPaysDestinationProtectionDonnees(da.getPaysDestinationProtectionDonnees());
            r.setDescriptionFichierTransfert(da.getDescriptionFichierTransfert());
            r.setNombrePersonnesTransfert(da.getNombrePersonnesTransfert());
            r.setCategoriesDonneesTransfert(da.getCategoriesDonneesTransfert());
            r.setFondementJuridique(da.getFondementJuridique());
            r.setConsentementPersonnesConcernees(da.getConsentementPersonnesConcernees());
            r.setMethodeRecueilConsentement(da.getMethodeRecueilConsentement());
            r.setMesuresSecuriteTransfert(da.getMesuresSecuriteTransfert());
            r.setDestinataireNomPrenom(da.getDestinataireNomPrenom());
            r.setDureeConservationSante(da.getDureeConservationSante());
            r.setDescriptionSensibilisation(da.getDescriptionSensibilisation());
            r.setFinaliteSante(da.getFinaliteSante());
            r.setPaysDestinationTransfert(da.getPaysDestinationTransfert());
        }

        return r;
    }
}