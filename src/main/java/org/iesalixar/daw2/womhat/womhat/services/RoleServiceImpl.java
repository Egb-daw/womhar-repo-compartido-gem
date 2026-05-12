package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.RoleDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.RoleMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de roles.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleDTO> listAll() {
        return RoleMapper.toDTOList(roleRepository.findAll());
    }

    @Override
    public RoleDTO getByName(String name) {
        Role role = roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("role", "name", name));
        return RoleMapper.toDTO(role);
    }
}