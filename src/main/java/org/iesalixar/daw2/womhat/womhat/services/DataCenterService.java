package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Servicio de negocio para CPDs.
 */
public interface DataCenterService {

    Page<DataCenterDTO> list(Pageable pageable);

    List<DataCenterOptionDTO> listOptions();

    DataCenterDetailDTO getDetail(Long id);

    DataCenterFormDTO getForm(Long id);

    void create(DataCenterFormDTO dto);

    void update(DataCenterFormDTO dto);

    void delete(Long id);
}