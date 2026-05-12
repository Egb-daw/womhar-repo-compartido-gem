package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterOptionDTO;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenter;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.DataCenterMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.DataCenterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de CPDs.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DataCenterServiceImpl implements DataCenterService {

    private static final Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);

    private final DataCenterRepository dataCenterRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DataCenterDTO> list(Pageable pageable) {
        return dataCenterRepository.findAll(pageable).map(DataCenterMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataCenterOptionDTO> listOptions() {
        return DataCenterMapper.toOptionList(
                dataCenterRepository.findAll(Sort.by(Sort.Direction.ASC, "code"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DataCenterDetailDTO getDetail(Long id) {
        DataCenter entity = dataCenterRepository.findWithRoomsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", id));
        return DataCenterMapper.toDetailDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DataCenterFormDTO getForm(Long id) {
        DataCenter entity = dataCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", id));
        return DataCenterMapper.toFormDTO(entity);
    }

    @Override
    public void create(DataCenterFormDTO dto) {
        logger.info("Creando CPD con code={}", dto.getCode());

        dataCenterRepository.findByCodeIgnoreCase(dto.getCode()).ifPresent(existing -> {
            throw new DuplicateResourceException("dataCenter", "code", dto.getCode());
        });

        DataCenter entity = DataCenterMapper.toEntity(dto);
        dataCenterRepository.save(entity);
    }

    @Override
    public void update(DataCenterFormDTO dto) {
        logger.info("Actualizando CPD con id={}", dto.getId());

        DataCenter entity = dataCenterRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", dto.getId()));

        dataCenterRepository.findByCodeIgnoreCase(dto.getCode())
                .filter(existing -> !existing.getId().equals(dto.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("dataCenter", "code", dto.getCode());
                });

        DataCenterMapper.copyToExistingEntity(dto, entity);
        dataCenterRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        logger.info("Eliminando CPD con id={}", id);

        DataCenter entity = dataCenterRepository.findWithRoomsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", id));

        if (entity.getRooms() != null && !entity.getRooms().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el CPD porque tiene salas asociadas.");
        }

        dataCenterRepository.delete(entity);
    }
}