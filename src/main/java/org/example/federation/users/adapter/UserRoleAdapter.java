package org.example.federation.users.adapter;

import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UserRoleAdapter implements RoleModel {

    protected RealmModel realm;
    protected ComponentModel model;
    protected UserRoleEntity entity;
    protected KeycloakSession session;

    public UserRoleAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserRoleEntity entity) {
        this.session = session;
        this.realm = realm;
        this.model = model;
        this.entity = entity;
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
        return entity.getName();
    }

    public UserRoleEntity getEntity() {
        return entity;
    }

    public UserRoleAdapter getRoleAdapter(RoleModel role) {
        return (UserRoleAdapter)role;
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