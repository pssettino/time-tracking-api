package com.scrumbox.mm.timetrackingapi.controller;

import com.scrumbox.mm.timetrackingapi.persistence.domain.Fichaje;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Fichaje;
import com.scrumbox.mm.timetrackingapi.service.FichajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class FichajeController {
    @Autowired
    private FichajeService fichajeService;

    @GetMapping("/")
    public List<Fichaje> getAll() {
        return fichajeService.getAll();
    }


    @PutMapping("/")
    public Fichaje addFichaje(@RequestBody Fichaje fichaje) {
        return fichajeService.save(fichaje);
    }

    @GetMapping("/dni")
    public Fichaje findByDni(@RequestParam Integer dni) {
        return fichajeService.findByDni(dni);
    }
}
