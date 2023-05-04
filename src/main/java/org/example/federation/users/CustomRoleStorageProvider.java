package org.example.federation.users;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleLookupProvider;
import org.keycloak.storage.role.RoleStorageProvider;

import java.util.stream.Stream;

public class CustomRoleStorageProvider implements
        RoleStorageProvider,
        RoleLookupProvider,
        RoleProvider
{

    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorageProvider(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
    }

    @Override
    public void close() {
    }

    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        return null;
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        return null;
    }

    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        return null;
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        return null;
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String name) {
        return RoleProvider.super.addRealmRole(realm, name);
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        return null;
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        return null;
    }

    @Override
    public boolean removeRole(RoleModel role) {
        return false;
    }

    @Override
    public void removeRoles(RealmModel realm) {

    }

    @Override
    public RoleModel addClientRole(ClientModel client, String id, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client, Integer first, Integer max) {
        return null;
    }

    @Override
    public void removeRoles(ClientModel client) {

    }
}
