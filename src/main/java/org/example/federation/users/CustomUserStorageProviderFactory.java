package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.UserStorageProviderFactory;

@Slf4j
public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {

    public static final String PROVIDER_ID = "JDBC USERS";

    @Override
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        log.info(">>>>>> Creating factory >>>>>>");
        return new CustomUserStorageProvider(session, model);
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        log.info(">>>> ON CREATE >>>>");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "JPA Example User Storage Provider";
    }

    @Override
    public void close() {
        log.info("<<<<<< Closing factory <<<<<<");
    }


}
