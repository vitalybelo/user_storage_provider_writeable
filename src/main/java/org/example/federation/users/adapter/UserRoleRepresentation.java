package org.example.federation.users.adapter;

import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.RoleRepresentation;

public class UserRoleRepresentation extends RoleRepresentation {

    protected UserRoleEntity roleEntity;
    protected KeycloakSession session;

    public UserRoleRepresentation(KeycloakSession session, UserRoleEntity roleEntity) {
        this.session = session;
        this.roleEntity = roleEntity;
    }

    @Override
    public String getName() {
        return roleEntity.getName();
    }

    @Override
    public void setName(String name) {
        if (name != null)
            roleEntity.setName(name);
    }

    @Override
    public String getDescription() {
        return roleEntity.getDescription();
    }

    @Override
    public void setDescription(String description) {
        if (description != null)
            roleEntity.setDescription(description);
    }



}
