package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RoomOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Servicio de negocio para salas de CPD.
 */
public interface DataCenterRoomService {

    Page<DataCenterRoomDTO> list(Pageable pageable);

    List<DataCenterRoomDTO> listByDataCenter(Long dataCenterId);

    List<RoomOptionDTO> listOptions();

    DataCenterRoomDetailDTO getDetail(Long id);

    DataCenterRoomFormDTO getForm(Long id);

    void create(DataCenterRoomFormDTO dto);

    void update(DataCenterRoomFormDTO dto);

    void delete(Long id);
}