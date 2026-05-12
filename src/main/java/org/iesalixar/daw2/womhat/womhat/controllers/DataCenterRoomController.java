package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomFormDTO;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.DataCenterRoomService;
import org.iesalixar.daw2.womhat.womhat.services.DataCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador MVC del módulo de salas de CPD.
 */
@Controller
@RequestMapping("/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class DataCenterRoomController {

    private static final Logger logger = LoggerFactory.getLogger(DataCenterRoomController.class);
    private static final String ACTIVE_MENU = "rooms";

    @Autowired
    private DataCenterRoomService dataCenterRoomService;

    @Autowired
    private DataCenterService dataCenterService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Lista paginada de salas.
     *
     * @param pageable paginación
     * @param model modelo
     * @return vista de listado
     */
    @GetMapping
    public String listRooms(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            Locale locale) {

        logger.info("Listando salas page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<DataCenterRoomDTO> page = dataCenterRoomService.list(pageable);

            model.addAttribute("page", page);
            model.addAttribute("sortParam", resolveSortParam(page));
            model.addAttribute("dataCenters", dataCenterService.listOptions());
            model.addAttribute("active", ACTIVE_MENU);

        } catch (Exception ex) {
            logger.error("Error al listar salas: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage", msg("msg.room.feedback.list.error", locale, "No se pudieron cargar las salas."));
            model.addAttribute("active", ACTIVE_MENU);
        }

        return "views/data-center-room/data-center-room-list";
    }

    /**
     * Muestra el formulario de alta.
     *
     * @param model modelo
     * @return vista formulario
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario de alta de sala.");

        model.addAttribute("room", new DataCenterRoomFormDTO());
        model.addAttribute("dataCenters", dataCenterService.listOptions());
        model.addAttribute("active", ACTIVE_MENU);
        return "views/data-center-room/data-center-room-form";
    }

    /**
     * Muestra el formulario de edición.
     *
     * @param id id de la sala
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista o redirect
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Mostrando formulario de edición de sala id={}", id);

        try {
            DataCenterRoomFormDTO formDTO = dataCenterRoomService.getForm(id);

            model.addAttribute("room", formDTO);
            model.addAttribute("dataCenters", dataCenterService.listOptions());
            model.addAttribute("active", ACTIVE_MENU);
            return "views/data-center-room/data-center-room-form";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la sala id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.notFound", locale, "No se encontró la sala solicitada."));
            return "redirect:/rooms";

        } catch (Exception ex) {
            logger.error("Error al cargar el formulario de sala: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.form.error", locale, "No se pudo cargar el formulario de la sala."));
            return "redirect:/rooms";
        }
    }

    /**
     * Inserta una nueva sala.
     *
     * @param room dto del formulario
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return redirect o formulario
     */
    @PostMapping("/insert")
    public String insertRoom(@Valid @ModelAttribute("room") DataCenterRoomFormDTO room,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Insertando sala name={} en dataCenterId={}", room.getName(), room.getDataCenterId());

        try {
            if (result.hasErrors()) {
                model.addAttribute("dataCenters", dataCenterService.listOptions());
                model.addAttribute("active", ACTIVE_MENU);
                return "views/data-center-room/data-center-room-form";
            }

            dataCenterRoomService.create(room);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.room.feedback.create.success", locale, "Sala creada correctamente."));
            return "redirect:/rooms";

        } catch (DuplicateResourceException ex) {
            logger.warn("Nombre de sala duplicado: {}", room.getName());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.create.duplicate", locale, "Ya existe una sala con ese nombre en el CPD."));
            return "redirect:/rooms/new";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el CPD asociado: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.create.dataCenterNotFound", locale, "No se encontró el CPD seleccionado."));
            return "redirect:/rooms/new";

        } catch (Exception ex) {
            logger.error("Error al crear la sala: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.create.error", locale, "No se pudo crear la sala."));
            return "redirect:/rooms/new";
        }
    }

    /**
     * Actualiza una sala.
     *
     * @param room dto de edición
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return redirect o formulario
     */
    @PostMapping("/update")
    public String updateRoom(@Valid @ModelAttribute("room") DataCenterRoomFormDTO room,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Actualizando sala id={}", room.getId());

        try {
            if (result.hasErrors()) {
                model.addAttribute("dataCenters", dataCenterService.listOptions());
                model.addAttribute("active", ACTIVE_MENU);
                return "views/data-center-room/data-center-room-form";
            }

            dataCenterRoomService.update(room);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.room.feedback.update.success", locale, "Sala actualizada correctamente."));
            return "redirect:/rooms";

        } catch (DuplicateResourceException ex) {
            logger.warn("Nombre de sala duplicado al actualizar: {}", room.getName());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.update.duplicate", locale, "Ya existe otra sala con ese nombre en el CPD."));
            return "redirect:/rooms/edit?id=" + room.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la sala o el CPD: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.update.notFound", locale, "No se encontró la sala o el CPD seleccionado."));
            return "redirect:/rooms";

        } catch (Exception ex) {
            logger.error("Error al actualizar la sala: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.update.error", locale, "No se pudo actualizar la sala."));
            return "redirect:/rooms/edit?id=" + room.getId();
        }
    }

    /**
     * Elimina una sala.
     *
     * @param id id de la sala
     * @param redirectAttributes mensajes flash
     * @return redirect
     */
    @PostMapping("/delete")
    public String deleteRoom(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Eliminando sala id={}", id);

        try {
            dataCenterRoomService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.room.feedback.delete.success", locale, "Sala eliminada correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la sala id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.notFound", locale, "No se encontró la sala solicitada."));

        } catch (Exception ex) {
            logger.error("Error al eliminar la sala id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.delete.error", locale, "No se pudo eliminar la sala."));
        }

        return "redirect:/rooms";
    }

    /**
     * Muestra el detalle de una sala.
     *
     * @param id id de la sala
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista detalle o redirect
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Mostrando detalle de sala id={}", id);

        try {
            DataCenterRoomDetailDTO detailDTO = dataCenterRoomService.getDetail(id);

            model.addAttribute("room", detailDTO);
            model.addAttribute("active", ACTIVE_MENU);
            return "views/data-center-room/data-center-room-detail";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.notFound", locale, "No se encontró la sala solicitada."));
            return "redirect:/rooms";

        } catch (Exception ex) {
            logger.error("Error al cargar el detalle de la sala: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.room.feedback.detail.error", locale, "No se pudo cargar el detalle de la sala."));
            return "redirect:/rooms";
        }
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }

    /**
     * Calcula el parámetro sort actual para la vista.
     *
     * @param page página actual
     * @return valor campo,direccion
     */
    private String resolveSortParam(Page<?> page) {
        String sortParam = "name,asc";

        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next();
            sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
        }

        return sortParam;
    }
}
