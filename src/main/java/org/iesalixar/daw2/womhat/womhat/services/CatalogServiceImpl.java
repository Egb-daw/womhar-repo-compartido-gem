package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.entities.RackPurchaseOrder;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.RackMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.RackPurchaseOrderRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.RackRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio del catálogo interno de racks y pedidos asociados.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final RackRepository rackRepository;
    private final UserRepository userRepository;
    private final RackPurchaseOrderRepository rackPurchaseOrderRepository;
    private final UserRackAccessService userRackAccessService;

    /**
     * Lista racks publicados en catálogo aplicando filtros funcionales y orden estable.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RackDTO> listCatalog(Pageable pageable,
                                     String query,
                                     RackStatus status,
                                     Integer minCapacityU,
                                     BigDecimal maxPrice) {

        List<RackDTO> content = rackRepository.findAll()
                .stream()
                .filter(this::isPublishedInCatalog)
                .filter(rack -> status == null || rack.getStatus() == status)
                .filter(rack -> minCapacityU == null || (rack.getCapacityU() != null && rack.getCapacityU() >= minCapacityU))
                .filter(rack -> maxPrice == null || (rack.getCatalogPrice() != null && rack.getCatalogPrice().compareTo(maxPrice) <= 0))
                .filter(rack -> matchesQuery(rack, query))
                .sorted(resolveComparator(pageable))
                .map(RackMapper::toDTO)
                .toList();

        return toPage(content, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RackDetailDTO getCatalogDetail(Long rackId) {
        Rack rack = rackRepository.findDetailedById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", rackId));

        if (!isPublishedInCatalog(rack)) {
            throw new ResourceNotFoundException("rack", "id", rackId);
        }

        return RackMapper.toDetailDTO(rack);
    }

    /**
     * Crea un pedido pendiente sobre un rack físico disponible en catálogo.
     */
    @Override
    public void placeOrder(String email, RackPurchaseOrderFormDTO form) {
        User user = findUser(email);
        Rack rack = rackRepository.findById(form.getRackId())
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", form.getRackId()));

        if (!isPublishedInCatalog(rack)) {
            throw new ResourceNotFoundException("rack", "id", form.getRackId());
        }

        if (rack.getStatus() != RackStatus.ACTIVE) {
            throw new IllegalStateException("El rack no está disponible ahora mismo para pedidos.");
        }

        int currentStock = rack.getCatalogStock() != null ? rack.getCatalogStock() : 0;
        int requestedQuantity = form.getQuantity() != null ? form.getQuantity() : 1;

        if (requestedQuantity != 1) {
            throw new IllegalStateException("Cada pedido del catálogo solo permite una unidad por rack.");
        }

        if (currentStock <= 0) {
            throw new IllegalStateException("No hay stock disponible para este rack.");
        }

        if (rackPurchaseOrderRepository.existsByRack_IdAndStatus(rack.getId(), RackPurchaseOrderStatus.PLACED)) {
            throw new IllegalStateException("Este rack ya tiene un pedido pendiente y no puede volver a solicitarse.");
        }

        if (currentStock > 1) {
            logger.warn("Rack {} con stock {} en catálogo. Se normaliza a flujo físico 0/1 por pedido.",
                    rack.getId(), currentStock);
        }

        if (rack.getCatalogPrice() == null) {
            throw new IllegalStateException("El rack no tiene precio configurado en catálogo.");
        }

        RackPurchaseOrder order = new RackPurchaseOrder();
        order.setUser(user);
        order.setRack(rack);
        order.setQuantity(1);
        order.setUnitPrice(rack.getCatalogPrice());
        order.setTotalPrice(rack.getCatalogPrice());
        order.setStatus(RackPurchaseOrderStatus.PLACED);
        order.setNotes(StringUtils.hasText(form.getNotes()) ? form.getNotes().trim() : null);

        rack.setCatalogStock(0);

        rackRepository.save(rack);
        rackPurchaseOrderRepository.save(order);
        logger.info("Pedido creado: user={}, rackId={}, orderStatus={}", user.getEmail(), rack.getId(), order.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RackPurchaseOrderDTO> listOrders(String email, Pageable pageable, boolean adminAccess) {
        if (adminAccess) {
            return rackPurchaseOrderRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(this::toOrderDTO);
        }

        User user = findUser(email);
        return rackPurchaseOrderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toOrderDTO);
    }

    /**
     * Cancela un pedido pendiente y restaura stock si no existe una venta completada.
     */
    @Override
    public void cancelOrder(String email, Long orderId, boolean adminAccess) {
        RackPurchaseOrder order;

        if (adminAccess) {
            order = rackPurchaseOrderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("rackPurchaseOrder", "id", orderId));
        } else {
            User user = findUser(email);
            order = rackPurchaseOrderRepository.findByIdAndUser_Id(orderId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("rackPurchaseOrder", "id", orderId));
        }

        if (order.getStatus() != RackPurchaseOrderStatus.PLACED) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado pendiente.");
        }

        Rack rack = order.getRack();
        if (rack != null) {
            boolean alreadySold = rackPurchaseOrderRepository.existsByRack_IdAndStatus(rack.getId(), RackPurchaseOrderStatus.FULFILLED);
            rack.setCatalogStock(alreadySold ? 0 : 1);
            rackRepository.save(rack);
        }

        order.setStatus(RackPurchaseOrderStatus.CANCELLED);
        rackPurchaseOrderRepository.save(order);
        logger.info("Pedido cancelado: orderId={}, actor={}, adminAccess={}", orderId, email, adminAccess);
    }

    /**
     * Completa un pedido pendiente, bloquea disponibilidad del rack y concede
     * acceso funcional al comprador como propietario original cuando aplica.
     */
    @Override
    public void fulfillOrder(Long orderId, String actorEmail) {
        RackPurchaseOrder order = rackPurchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("rackPurchaseOrder", "id", orderId));

        if (order.getStatus() != RackPurchaseOrderStatus.PLACED) {
            throw new IllegalStateException("Solo se pueden completar pedidos en estado pendiente.");
        }

        Rack rack = order.getRack();
        if (rack == null || rack.getId() == null) {
            throw new IllegalStateException("El pedido no tiene rack asociado.");
        }

        if (!isPublishedInCatalog(rack)) {
            throw new IllegalStateException("El rack no está publicado en catálogo y no puede completarse este pedido.");
        }

        long fulfilledForRack = rackPurchaseOrderRepository.countByRack_IdAndStatus(rack.getId(), RackPurchaseOrderStatus.FULFILLED);
        if (fulfilledForRack > 0) {
            throw new IllegalStateException("Este rack ya tiene un pedido completado y no puede volver a venderse.");
        }

        rack.setCatalogStock(0);
        rackRepository.save(rack);

        order.setStatus(RackPurchaseOrderStatus.FULFILLED);
        rackPurchaseOrderRepository.save(order);
        logger.info("Pedido completado: orderId={}, actor={}", orderId, actorEmail);

        if (order.getUser() != null
                && StringUtils.hasText(order.getUser().getEmail())
                && rack.getId() != null) {
            userRackAccessService.grantOriginalOwnerByEmail(
                    actorEmail,
                    order.getUser().getEmail(),
                    rack.getId()
            );
            logger.info("Acceso de propietario funcional concedido tras completar pedido: orderId={}, ownerEmail={}, rackId={}",
                    orderId, order.getUser().getEmail(), rack.getId());
        }
    }

    private boolean isPublishedInCatalog(Rack rack) {
        return rack != null
                && rack.isCatalogVisible()
                && rack.getCatalogPrice() != null;
    }

    private RackPurchaseOrderDTO toOrderDTO(RackPurchaseOrder order) {
        return new RackPurchaseOrderDTO(
                order.getId(),
                order.getUser() != null ? order.getUser().getEmail() : null,
                order.getRack() != null ? order.getRack().getId() : null,
                order.getRack() != null ? order.getRack().getLocationLabel() : null,
                order.getRack() != null ? RackMapper.buildCatalogDisplayName(order.getRack()) : null,
                order.getRack() != null ? order.getRack().getCatalogSummary() : null,
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getNotes(),
                order.getCreatedAt()
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
    }

    private boolean matchesQuery(Rack rack, String query) {
        if (!StringUtils.hasText(query)) {
            return true;
        }

        String normalized = query.trim().toLowerCase();

        return contains(rack.getLocationLabel(), normalized)
                || contains(rack.getFunctionName(), normalized)
                || contains(rack.getGroupName(), normalized)
                || contains(rack.getDimension(), normalized)
                || contains(rack.getCatalogSummary(), normalized)
                || (rack.getRoom() != null && contains(rack.getRoom().getName(), normalized))
                || (rack.getRoom() != null
                && rack.getRoom().getDataCenter() != null
                && (contains(rack.getRoom().getDataCenter().getCode(), normalized)
                || contains(rack.getRoom().getDataCenter().getName(), normalized)));
    }

    private boolean contains(String value, String normalized) {
        return value != null && value.toLowerCase().contains(normalized);
    }

    private Comparator<Rack> resolveComparator(Pageable pageable) {
        Comparator<Rack> fallback = Comparator
                .comparing(Rack::getCatalogPrice, Comparator.nullsLast(BigDecimal::compareTo))
                .thenComparing(Rack::getLocationLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        if (pageable == null || pageable.getSort().isUnsorted()) {
            return fallback;
        }

        Comparator<Rack> comparator = null;

        for (Sort.Order order : pageable.getSort()) {
            Comparator<Rack> nextComparator = buildComparator(order.getProperty());

            if (!order.isAscending()) {
                nextComparator = nextComparator.reversed();
            }

            comparator = (comparator == null) ? nextComparator : comparator.thenComparing(nextComparator);
        }

        return comparator != null
                ? comparator.thenComparing(Rack::getLocationLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                : fallback;
    }

    private Comparator<Rack> buildComparator(String property) {
        return switch (property) {
            case "capacityU" -> Comparator.comparing(Rack::getCapacityU, Comparator.nullsLast(Integer::compareTo));
            case "locationLabel" -> Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "catalogStock" -> Comparator.comparing(Rack::getCatalogStock, Comparator.nullsLast(Integer::compareTo));
            case "catalogPrice" -> Comparator.comparing(Rack::getCatalogPrice, Comparator.nullsLast(BigDecimal::compareTo));
            default -> Comparator.comparing(Rack::getCatalogPrice, Comparator.nullsLast(BigDecimal::compareTo));
        };
    }

    private <T> Page<T> toPage(List<T> content, Pageable pageable) {
        int start = Math.toIntExact(pageable.getOffset());

        if (start >= content.size()) {
            return new PageImpl<>(List.of(), pageable, content.size());
        }

        int end = Math.min(start + pageable.getPageSize(), content.size());
        return new PageImpl<>(content.subList(start, end), pageable, content.size());
    }
}
