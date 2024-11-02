package com.example.management.role;

import com.example.management.exception.DataNotFoundException;
import com.example.management.permission.Permission;
import com.example.management.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private static final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    private static final String ROLE_NOT_FOUND = "Role not found";

    public RoleDto createRole(RoleDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);
        attachPermissionToRole(role, roleDto.permissionIds());
        return roleMapper.toDto(roleRepository.save(role));
    }

    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND));
        roleMapper.toUpdateEntity(role, roleDto);
        return roleMapper.toDto(roleRepository.save(role));
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public RoleDto getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND));
        return roleMapper.toDto(role);
    }

    public RoleDto getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND));
        return roleMapper.toDto(role);
    }

    public List<RoleDto> getRoles() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDto(roles);
    }

    public void attachPermissions(Long roleId, Set<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return;
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DataNotFoundException(ROLE_NOT_FOUND));

        attachPermissionToRole(role, permissionIds);
    }

    private void attachPermissionToRole(Role role, Set<Long> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new DataNotFoundException("Some permissions not found");
        }

        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
    }
}