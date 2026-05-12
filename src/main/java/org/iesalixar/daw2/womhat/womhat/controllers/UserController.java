package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.UserCreateDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserUpdateDTO;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.repositories.RoleRepository;
import org.iesalixar.daw2.womhat.womhat.services.UserService;
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

import java.util.List;
import java.util.Locale;

/**
 * Controlador MVC que maneja las operaciones CRUD para la entidad {@code User}.
 *
 * El controller delega en {@link UserService} la lógica de negocio y se centra en:
 * - flujo web,
 * - validación,
 * - carga de vistas,
 * - mensajes i18n.
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    /**
     * Se mantiene RoleRepository en el controller para cargar
     * el listado de roles en el formulario.
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Lista paginada de usuarios.
     *
     * @param pageable paginación y ordenación
     * @param model modelo para la vista
     * @return plantilla del listado
     */
    @GetMapping
    public String listUsers(
            @PageableDefault(size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            Locale locale) {

        logger.info("Listando usuarios page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<UserDTO> page = userService.list(pageable);

            logger.info("Se han cargado {} usuarios en la página {}.",
                    page.getNumberOfElements(), page.getNumber());

            model.addAttribute("page", page);
            model.addAttribute("active", "users");

            String sortParam = "email,asc";
            if (page.getSort().isSorted()) {
                Sort.Order order = page.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);

        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.list.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("active", "users");
        }
        return "views/user/user-list";
    }

    /**
     * Muestra el formulario de alta de usuario.
     *
     * @param model modelo
     * @return vista del formulario
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new UserCreateDTO());
        loadAssignableRoles(model);
        model.addAttribute("active", "users");
        return "views/user/user-form";
    }

    /**
     * Muestra el formulario de edición.
     *
     * @param id id del usuario
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @param locale locale actual
     * @return vista o redirect
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);

        try {
            UserUpdateDTO userDTO = userService.getForEdit(id);

            model.addAttribute("user", userDTO);
            loadAssignableRoles(model);
            model.addAttribute("active", "users");
            return "views/user/user-form";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario con ID {}", id);
            String msg = messageSource.getMessage("msg.user-controller.edit.notfound", new Object[]{id}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";

        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.user-controller.edit.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }

    /**
     * Inserta un nuevo usuario.
     *
     * @param userDTO dto de alta
     * @param result validación
     * @param redirectAttributes mensajes flash
     * @param model modelo
     * @param locale locale
     * @return redirect o formulario
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Locale locale) {

        logger.info("Insertando nuevo usuario con email {}", userDTO.getEmail());

        try {
            if (result.hasErrors()) {
                loadAssignableRoles(model);
                model.addAttribute("active", "users");
                return "views/user/user-form";
            }

            userService.create(userDTO);

            logger.info("Usuario {} insertado con éxito.", userDTO.getEmail());

            String successMessage = messageSource.getMessage(
                    "msg.user-controller.insert.success",
                    new Object[]{userDTO.getEmail()},
                    locale
            );
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            return "redirect:/users";

        } catch (DuplicateResourceException ex) {
            logger.warn("El email {} ya existe.", userDTO.getEmail());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.emailExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo crear el usuario por referencia inexistente: {}", ex.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.roleNotFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";
        }
    }

    /**
     * Actualiza un usuario.
     *
     * @param userDTO dto de edición
     * @param result validación
     * @param redirectAttributes mensajes flash
     * @param model modelo
     * @param locale locale
     * @return redirect o formulario
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Locale locale) {

        logger.info("Actualizando usuario con ID {}", userDTO.getId());

        try {
            if (result.hasErrors()) {
                loadAssignableRoles(model);
                model.addAttribute("active", "users");
                return "views/user/user-form";
            }

            userService.update(userDTO);

            logger.info("Usuario con ID {} actualizado con éxito.", userDTO.getId());

            String successMessage = messageSource.getMessage(
                    "msg.user-controller.update.success",
                    new Object[]{userDTO.getEmail()},
                    locale
            );
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            return "redirect:/users";

        } catch (DuplicateResourceException ex) {
            logger.warn("El email {} ya existe para otro usuario.", userDTO.getEmail());
            String errorMessage = messageSource.getMessage("msg.user-controller.update.emailExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/edit?id=" + userDTO.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo actualizar el usuario por recurso inexistente: {}", ex.getMessage());
            String notFound = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/users";

        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/edit?id=" + userDTO.getId();
        }
    }

    /**
     * Elimina un usuario.
     *
     * @param id id del usuario
     * @param redirectAttributes mensajes flash
     * @param locale locale actual
     * @return redirect
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Eliminando usuario con ID {}", id);

        try {
            userService.delete(id);

            logger.info("Usuario con ID {} eliminado con éxito.", id);

            String successMessage = messageSource.getMessage(
                    "msg.user-controller.delete.success",
                    new Object[]{id},
                    locale
            );
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            return "redirect:/users";

        } catch (IllegalStateException ex) {
            logger.warn("No se pudo eliminar el usuario con ID {}: {}", id, ex.getMessage());

            String errorMessage = messageSource.getMessage(
                    ex.getMessage(),
                    null,
                    ex.getMessage(),
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);

            return "redirect:/users";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario con ID {}", id);
            String notFound = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/users";

        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users";
        }
    }

    /**
     * Muestra el detalle de un usuario.
     *
     * @param id id del usuario
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @param locale locale
     * @return vista detalle o redirect
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Mostrando detalle del usuario con ID {}", id);

        try {
            UserDetailDTO userDTO = userService.getDetail(id);

            model.addAttribute("user", userDTO);
            model.addAttribute("active", "users");
            return "views/user/user-detail";

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";

        } catch (Exception e) {
            logger.error("Error al obtener el detalle del usuario {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.user-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }

    private void loadAssignableRoles(Model model) {
        List<?> roles = roleRepository.findAll().stream().toList();
        model.addAttribute("allRoles", roles);
    }
}
