package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RoleProviderFactory;

@Slf4j
public class CustomRoleProviderFactory implements RoleProviderFactory<CustomRoleProvider> {

    public static final String PROVIDER_ID = "JDBC-ROLE-PROVIDER";

    @Override
    public CustomRoleProvider create(KeycloakSession session) {
        log.info(">>>>>>>>> RoleProviderFactory >>>>>>>>> Create factory >>>>>>>>>>>");
        return new CustomRoleProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        log.info(">>>>>>>>> RoleProviderFactory >>>>>>>>> Init >>>>>>>>>>>");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        log.info(">>>>>>>>> RoleProviderFactory >>>>>>>>> Post Init >>>>>>>>>>>");
    }

    @Override
    public void close() {
        log.info("<<<<<<<<< RoleProviderFactory <<<<<<<<< Close factory <<<<<<<<<<");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        return RoleProviderFactory.super.order();
    }
}
