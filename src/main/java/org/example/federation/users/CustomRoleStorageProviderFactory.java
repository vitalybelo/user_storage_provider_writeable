package org.example.federation.users;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.role.RoleStorageProviderFactory;

public class CustomRoleStorageProviderFactory implements RoleStorageProviderFactory<CustomRoleStorageProvider> {

    public static final String PROVIDER_ID = "JDBC ROLES";

    @Override
    public CustomRoleStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomRoleStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
