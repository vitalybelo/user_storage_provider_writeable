package org.example.federation.users;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.role.RoleStorageProviderFactory;

public class CustomRoleStorageProviderFactory implements RoleStorageProviderFactory<CustomRoleStorageProvider> {

    public static final String PROVIDER_ID = "JDBC ROLE STORAGE PROVIDER";


    @Override
    public CustomRoleStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomRoleStorageProvider(session, model);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
