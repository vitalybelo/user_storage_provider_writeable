package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.role.RoleStorageProviderFactory;

@Slf4j
public class CustomRoleStorageProviderFactory implements RoleStorageProviderFactory<CustomRoleStorageProvider> {

    public static final String PROVIDER_ID = "ROLE_STORAGE";

    @Override
    public CustomRoleStorageProvider create(KeycloakSession session, ComponentModel model) {
        log.info(">>>>>>>>> RoleStorageProviderFactory >>>>>>>>> Create factory >>>>>>>>>>>");
        return new CustomRoleStorageProvider(session, model);
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        log.info(">>>>>>>>> RoleStorageProviderFactory >>>>>>>>> On create factory >>>>>>>>>>>");
        RoleStorageProviderFactory.super.onCreate(session, realm, model);
    }

    @Override
    public void init(Config.Scope config) {
        log.info(">>>>>>>>> RoleStorageProviderFactory >>>>>>>>> Init factory >>>>>>>>>>>");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        log.info("<<<<<<<<< RoleStorageProviderFactory <<<<<<<<< Close factory <<<<<<<<<<");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
