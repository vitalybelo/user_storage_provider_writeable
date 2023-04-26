package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.role.RoleStorageProviderFactory;

@Slf4j
public class CustomRoleStorageProviderFactory implements RoleStorageProviderFactory<CustomRoleStorageProvider> {

    public static final String PROVIDER_ID = "JDBC ROLES";
    @Override
    public CustomRoleStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        log.info(">>>>>> Creating factory >>>>>>");
        return new CustomRoleStorageProvider(keycloakSession, componentModel);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        log.info(">>>> OnCreate called >>>>");
    }

    @Override
    public String getHelpText() {
        return "JPA Example Role Storage Provider";
    }

    @Override
    public void close() {
    }
}
