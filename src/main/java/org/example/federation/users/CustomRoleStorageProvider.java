package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.adapter.UserRoleAdapter;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleStorageProvider;
import java.util.stream.Stream;

@Slf4j
public class CustomRoleStorageProvider implements
        RoleStorageProvider,
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
        return Stream.empty();
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return Stream.empty();
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String name) {

        log.info(">>>>>>>> addRealmRole(RealmModel realm, String name) >>>>");
        UserRoleEntity role = new UserRoleEntity();
        role.setName(name);
        return new UserRoleAdapter(session, realm, model, role);
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {

        log.info(">>>>>>>> addRealmRole(RealmModel realm, String id, String name) >>>>");
        UserRoleEntity role = new UserRoleEntity();
        role.setName(name);
        return new UserRoleAdapter(session, realm, model, role);
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return Stream.empty();
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
    public RoleModel addClientRole(ClientModel client, String id, String name) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client, Integer first, Integer max) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
        return Stream.empty();
    }

    @Override
    public void removeRoles(ClientModel client) {
        System.out.println("\n\n************************** YOU LUCKY ***************************\n");
    }



}
