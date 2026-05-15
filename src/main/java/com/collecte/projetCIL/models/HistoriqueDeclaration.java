package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.StatutDeclaration;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historique_declaration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistorique;

    @Column(name = "date_declaration")
    private LocalDate dateDeclaration;

    @Column(name = "responsable_declaration")
    private String responsableDeclaration;

    @Enumerated(EnumType.STRING)
    private StatutDeclaration statut;

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    private Declaration declaration;
}
