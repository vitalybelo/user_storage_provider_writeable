package org.example.federation.users;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleLookupProvider;
import org.keycloak.storage.role.RoleStorageProvider;

import java.util.stream.Stream;

public class CustomRoleStorageProvider implements
        RoleStorageProvider,
        RoleLookupProvider
{

    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorageProvider(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
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
