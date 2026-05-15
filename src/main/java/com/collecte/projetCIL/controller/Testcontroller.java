package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.models.teste;
import com.collecte.projetCIL.service.Testservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class Testcontroller {

    @Autowired
    private Testservice testservice;

    @PostMapping
    public teste ajouter(@RequestBody teste t) {
        return testservice.ajouter(t);
    }

    @GetMapping
    public List<teste> afficher() {
        return testservice.afficher();
    }
}