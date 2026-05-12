package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
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
 * Controlador MVC del módulo de CPDs.
 *
 * Gestiona:
 * - listado,
 * - alta,
 * - edición,
 * - detalle,
 * - eliminación.
 */
@Controller
@RequestMapping("/data-centers")
@PreAuthorize("hasRole('ADMIN')")
public class DataCenterController {

    private static final Logger logger = LoggerFactory.getLogger(DataCenterController.class);
    private static final String ACTIVE_MENU = "data-centers";

    @Autowired
    private DataCenterService dataCenterService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Lista paginada de CPDs.
     *
     * @param pageable paginación y ordenación
     * @param model modelo
     * @return vista del listado
     */
    @GetMapping
    public String listDataCenters(
            @PageableDefault(size = 10, sort = "code", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            Locale locale) {

        logger.info("Listando CPDs page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<DataCenterDTO> page = dataCenterService.list(pageable);

            model.addAttribute("page", page);
            model.addAttribute("sortParam", resolveSortParam(page));
            model.addAttribute("active", ACTIVE_MENU);

        } catch (Exception e) {
            logger.error("Error al listar CPDs: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", msg("msg.dataCenter.feedback.list.error", locale, "No se pudieron cargar los CPDs."));
            model.addAttribute("active", ACTIVE_MENU);
        }

        return "views/data-center/data-center-list";
    }

    /**
     * Muestra el formulario de alta.
     *
     * @param model modelo
     * @return vista del formulario
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario de alta de CPD.");

        model.addAttribute("dataCenter", new DataCenterFormDTO());
        model.addAttribute("statuses", DataCenterStatus.values());
        model.addAttribute("active", ACTIVE_MENU);
        return "views/data-center/data-center-form";
    }

    /**
     * Muestra el formulario de edición.
     *
     * @param id id del CPD
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista o redirect
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Mostrando formulario de edición del CPD id={}", id);

        try {
            DataCenterFormDTO formDTO = dataCenterService.getForm(id);

            model.addAttribute("dataCenter", formDTO);
            model.addAttribute("statuses", DataCenterStatus.values());
            model.addAttribute("active", ACTIVE_MENU);
            return "views/data-center/data-center-form";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el CPD id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.notFound", locale, "No se encontró el CPD solicitado."));
            return "redirect:/data-centers";

        } catch (Exception ex) {
            logger.error("Error al cargar el formulario del CPD id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.form.error", locale, "No se pudo cargar el formulario del CPD."));
            return "redirect:/data-centers";
        }
    }

    /**
     * Inserta un nuevo CPD.
     *
     * @param dataCenter dto del formulario
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return redirect o formulario
     */
    @PostMapping("/insert")
    public String insertDataCenter(@Valid @ModelAttribute("dataCenter") DataCenterFormDTO dataCenter,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {

        logger.info("Insertando CPD con code={}", dataCenter.getCode());

        try {
            if (result.hasErrors()) {
                model.addAttribute("statuses", DataCenterStatus.values());
                model.addAttribute("active", ACTIVE_MENU);
                return "views/data-center/data-center-form";
            }

            dataCenterService.create(dataCenter);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.dataCenter.feedback.create.success", locale, "CPD creado correctamente."));
            return "redirect:/data-centers";

        } catch (DuplicateResourceException ex) {
            logger.warn("Código de CPD duplicado: {}", dataCenter.getCode());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.create.duplicate", locale, "Ya existe un CPD con ese código."));
            return "redirect:/data-centers/new";

        } catch (Exception ex) {
            logger.error("Error al crear el CPD: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.create.error", locale, "No se pudo crear el CPD."));
            return "redirect:/data-centers/new";
        }
    }

    /**
     * Actualiza un CPD existente.
     *
     * @param dataCenter dto del formulario
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return redirect o formulario
     */
    @PostMapping("/update")
    public String updateDataCenter(@Valid @ModelAttribute("dataCenter") DataCenterFormDTO dataCenter,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {

        logger.info("Actualizando CPD id={}", dataCenter.getId());

        try {
            if (result.hasErrors()) {
                model.addAttribute("statuses", DataCenterStatus.values());
                model.addAttribute("active", ACTIVE_MENU);
                return "views/data-center/data-center-form";
            }

            dataCenterService.update(dataCenter);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.dataCenter.feedback.update.success", locale, "CPD actualizado correctamente."));
            return "redirect:/data-centers";

        } catch (DuplicateResourceException ex) {
            logger.warn("Código de CPD duplicado al actualizar: {}", dataCenter.getCode());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.update.duplicate", locale, "Ya existe otro CPD con ese código."));
            return "redirect:/data-centers/edit?id=" + dataCenter.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el CPD id={}", dataCenter.getId());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.notFound", locale, "No se encontró el CPD solicitado."));
            return "redirect:/data-centers";

        } catch (Exception ex) {
            logger.error("Error al actualizar el CPD: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.update.error", locale, "No se pudo actualizar el CPD."));
            return "redirect:/data-centers/edit?id=" + dataCenter.getId();
        }
    }

    /**
     * Elimina un CPD.
     *
     * @param id id del CPD
     * @param redirectAttributes mensajes flash
     * @return redirect
     */
    @PostMapping("/delete")
    public String deleteDataCenter(@RequestParam("id") Long id,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {

        logger.info("Eliminando CPD id={}", id);

        try {
            dataCenterService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.dataCenter.feedback.delete.success", locale, "CPD eliminado correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el CPD id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.notFound", locale, "No se encontró el CPD solicitado."));

        } catch (Exception ex) {
            logger.error("Error al eliminar el CPD id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.delete.error", locale, "No se pudo eliminar el CPD."));
        }

        return "redirect:/data-centers";
    }

    /**
     * Muestra el detalle de un CPD.
     *
     * @param id id del CPD
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista detalle o redirect
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Mostrando detalle del CPD id={}", id);

        try {
            DataCenterDetailDTO detailDTO = dataCenterService.getDetail(id);

            model.addAttribute("dataCenter", detailDTO);
            model.addAttribute("active", ACTIVE_MENU);
            return "views/data-center/data-center-detail";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.notFound", locale, "No se encontró el CPD solicitado."));
            return "redirect:/data-centers";

        } catch (Exception ex) {
            logger.error("Error al cargar el detalle del CPD id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.dataCenter.feedback.detail.error", locale, "No se pudo cargar el detalle del CPD."));
            return "redirect:/data-centers";
        }
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }

    /**
     * Calcula el parámetro sort actual para reutilizarlo en la vista.
     *
     * @param page página actual
     * @return texto campo,direccion
     */
    private String resolveSortParam(Page<?> page) {
        String sortParam = "code,asc";

        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next();
            sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
        }

        return sortParam;
    }
}
