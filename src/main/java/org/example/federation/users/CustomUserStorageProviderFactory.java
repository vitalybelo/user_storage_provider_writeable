package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

@Slf4j
public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {

    public static final String PROVIDER_ID = "JDBC USER STORAGE PROVIDER";

    @Override
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomUserStorageProvider(session, model);
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
    }

}
