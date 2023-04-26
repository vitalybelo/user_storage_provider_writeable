package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleLookupProvider;
import org.keycloak.storage.role.RoleStorageProvider;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

@Slf4j
public class CustomRoleStorageProvider implements
        RoleStorageProvider,
        RoleLookupProvider,
        RoleProvider
{

    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorageProvider(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();

    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        return Stream.empty();
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
}
