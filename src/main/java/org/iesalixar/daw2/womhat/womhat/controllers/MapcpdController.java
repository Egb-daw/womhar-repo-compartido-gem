package org.iesalixar.daw2.womhat.womhat.controllers;

import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.services.MapcpdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MapcpdController {

    private static final Logger logger = LoggerFactory.getLogger(MapcpdController.class);

    @Autowired
    private MapcpdService mapcpdService;

    @GetMapping("/mapcpd")
    public String showMapcpd(Model model) {
        logger.info("Cargando vista Mapa CPD");
        return "views/Mapcpd/index";
    }

    @GetMapping("/api/mapcpd/racks")
    @ResponseBody
    public List<RackDTO> getMapDataJson() {
        logger.info("Solicitud API para obtener datos del mapa");
        return mapcpdService.getMapData();
    }

    @PostMapping("/api/mapcpd/racks")
    @ResponseBody
    public ResponseEntity<RackDTO> saveRack(@RequestBody RackDTO rackDTO) {
        try {
            logger.info("Guardando rack: {}", rackDTO.getLocationLabel());
            RackDTO savedRack = mapcpdService.saveRackWithEquipment(rackDTO);
            return ResponseEntity.ok(savedRack);
        } catch (Exception e) {
            logger.error("Error al guardar rack", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/api/mapcpd/racks/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteRack(@PathVariable Long id) {
        try {
            mapcpdService.deleteRack(id);
            logger.info("Rack borrado: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al borrar rack", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}