package com.coreoz.plume.admin.services.permission;

import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionServiceBasic;
import com.google.inject.Inject;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectAdminPermissionService implements AdminPermissionService {

    private final Set<String> permissionsAvailable;

    @Inject
    public ProjectAdminPermissionService(AdminPermissionServiceBasic adminPermissionServiceBasic) {
        this.permissionsAvailable = Stream
            .of(ProjectAdminPermission.class.getDeclaredFields())
            .map(field -> {
                try {
                    return (String) field.get(null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toSet());
        this.permissionsAvailable.addAll(adminPermissionServiceBasic.permissionsAvailable());
    }

    @Override
    public Set<String> permissionsAvailable() {
        return permissionsAvailable;
    }
}