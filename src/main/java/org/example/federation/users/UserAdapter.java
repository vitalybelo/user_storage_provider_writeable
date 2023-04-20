package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    protected UserEntity entity;
    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getId()));
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    public String getPassword() {
        return entity.getPassword();
    }

    public void setPassword(String password) {
        entity.setPassword(password);
    }

    @Override
    public String getUsername() {
        return entity.getUsername();
    }

    @Override
    public void setUsername(String username) {
        entity.setUsername(username);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public void setSingleAttribute(String name, String value) {

        if (name.equals("phone")) {
            entity.setPhone(value);
        } else if (name.equals("middle_name")) {
            entity.setMiddleName(value);
        } else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {

        if (name.equals("phone")) {
            entity.setPhone(null);
        } else if (name.equals("middle_name")) {
            entity.setMiddleName(null);
        } else {
            super.removeAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {

        if (name.equals("phone")) {
            entity.setPhone(values.get(0));
        } else if (name.equals("middle_name")) {
            entity.setMiddleName(values.get(0));
        } else {
            super.setAttribute(name, values);
        }
    }

    @Override
    public String getFirstAttribute(String name) {

        if (name.equals("phone")) {
            return entity.getPhone();
        } else if (name.equals("middle_name")) {
            return entity.getMiddleName();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {

        // *****************************************
        log.info(">>>> GEY ATTRIBUTES MAP >>>>");
        // *****************************************
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add("phone", entity.getPhone());
        all.add("middle_name", entity.getMiddleName());
        return all;
    }

    @Override
    public boolean isEnabled() {
        return entity.getStatus().equals("ACTIVE");
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            entity.setStatus("ACTIVE");
        } else {
            entity.setStatus("DELETED");
        }
        super.setEnabled(enabled);
    }

    @Override
    public boolean isEmailVerified() {
        return super.isEmailVerified();
    }

    @Override
    public void setEmailVerified(boolean verified) {
        super.setEmailVerified(verified);
    }

    @Override
    public Long getCreatedTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        super.setCreatedTimestamp(timestamp);
    }

}
