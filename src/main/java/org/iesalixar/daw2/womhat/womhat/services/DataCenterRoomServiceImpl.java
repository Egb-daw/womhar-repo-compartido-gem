package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RoomOptionDTO;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenter;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenterRoom;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.DataCenterRoomMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.DataCenterRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.DataCenterRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de salas de CPD.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DataCenterRoomServiceImpl implements DataCenterRoomService {

    private static final Logger logger = LoggerFactory.getLogger(DataCenterRoomServiceImpl.class);

    private final DataCenterRoomRepository dataCenterRoomRepository;
    private final DataCenterRepository dataCenterRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DataCenterRoomDTO> list(Pageable pageable) {
        return dataCenterRoomRepository.findAll(pageable).map(DataCenterRoomMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataCenterRoomDTO> listByDataCenter(Long dataCenterId) {
        return DataCenterRoomMapper.toDTOList(
                dataCenterRoomRepository.findByDataCenter_IdOrderByNameAsc(dataCenterId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomOptionDTO> listOptions() {
        return DataCenterRoomMapper.toOptionList(
                dataCenterRoomRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DataCenterRoomDetailDTO getDetail(Long id) {
        DataCenterRoom entity = dataCenterRoomRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", id));
        return DataCenterRoomMapper.toDetailDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DataCenterRoomFormDTO getForm(Long id) {
        DataCenterRoom entity = dataCenterRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", id));
        return DataCenterRoomMapper.toFormDTO(entity);
    }

    @Override
    public void create(DataCenterRoomFormDTO dto) {
        logger.info("Creando sala name={} en dataCenterId={}", dto.getName(), dto.getDataCenterId());

        DataCenter dataCenter = dataCenterRepository.findById(dto.getDataCenterId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", dto.getDataCenterId()));

        boolean alreadyExists = dataCenterRoomRepository.existsByDataCenter_IdAndName(dto.getDataCenterId(), dto.getName());
        if (alreadyExists) {
            throw new DuplicateResourceException("dataCenterRoom", "name", dto.getName());
        }

        DataCenterRoom entity = DataCenterRoomMapper.toEntity(dto, dataCenter);
        dataCenterRoomRepository.save(entity);
    }

    @Override
    public void update(DataCenterRoomFormDTO dto) {
        logger.info("Actualizando sala id={}", dto.getId());

        DataCenterRoom entity = dataCenterRoomRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", dto.getId()));

        DataCenter dataCenter = dataCenterRepository.findById(dto.getDataCenterId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenter", "id", dto.getDataCenterId()));

        dataCenterRoomRepository.findByDataCenter_IdAndName(dto.getDataCenterId(), dto.getName())
                .filter(existing -> !existing.getId().equals(dto.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("dataCenterRoom", "name", dto.getName());
                });

        DataCenterRoomMapper.copyToExistingEntity(dto, entity, dataCenter);
        dataCenterRoomRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        logger.info("Eliminando sala id={}", id);

        DataCenterRoom entity = dataCenterRoomRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", id));

        if (entity.getRacks() != null && !entity.getRacks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la sala porque tiene racks asociados.");
        }

        dataCenterRoomRepository.delete(entity);
    }
}