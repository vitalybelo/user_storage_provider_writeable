package org.example.federation.users.adapter;

import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UserRoleModel implements RoleModel {

    protected RealmModel realm;
    protected UserRoleEntity entity;
    protected String keycloakId;

    public UserRoleModel(UserRoleEntity entity, ComponentModel model, RealmModel realm) {
        this.entity = entity;
        this.realm = realm;
        this.keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getRoleId()));
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public void setName(String name) {
        entity.setName(name);
    }

    @Override
    public String getDescription() {
        return entity.getDescription();
    }

    @Override
    public void setDescription(String description) {
        entity.setDescription(description);
    }

    @Override
    public String getId() {
        return keycloakId;
        //return entity.getName();
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public void addCompositeRole(RoleModel role) {
    }

    @Override
    public void removeCompositeRole(RoleModel role) {
    }

    @Override
    public Stream<RoleModel> getCompositesStream() {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getCompositesStream(String s, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public boolean isClientRole() {
        return false;
    }

    @Override
    public String getContainerId() {
        return realm.getId();
    }

    @Override
    public RoleContainerModel getContainer() {
        return realm;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return this.equals(role) || entity.getName().equals(role.getName());
    }

    @Override
    public void setSingleAttribute(String name, String value) {
    }

    @Override
    public void setAttribute(String name, List<String> values) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public String getFirstAttribute(String name) {
        return RoleModel.super.getFirstAttribute(name);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return Stream.empty();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Map.of();
    }


}