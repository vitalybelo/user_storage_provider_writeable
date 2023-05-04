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
    public RoleModel addRealmRole(RealmModel realm, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return RoleProvider.super.addRealmRole(realm, name);
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return RoleProvider.super.getRealmRolesStream(realm);
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public boolean removeRole(RoleModel role) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return false;
    }

    @Override
    public void removeRoles(RealmModel realm) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
    }

    @Override
    public RoleModel addClientRole(ClientModel client, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return RoleProvider.super.addClientRole(client, name);
    }

    @Override
    public RoleModel addClientRole(ClientModel client, String id, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return RoleProvider.super.getClientRolesStream(client);
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public void removeRoles(ClientModel client) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
    }

    @Override
    public void close() {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
    }

    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }


}
