package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.List;
import java.util.Map;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    protected static String ENABLED_TRUE = "ACTIVE";
    protected static String ENABLED_FALSE = "DELETED";
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

        if (values.isEmpty()) return;
        switch (name) {
            case UserModel.LAST_NAME: entity.setLastName(values.get(0)); break;
            case UserModel.FIRST_NAME: entity.setFirstName(values.get(0)); break;
            case UserModel.EMAIL: entity.setEmail(values.get(0)); break;
            case "phone":
                entity.setPhone(values.get(0));
                break;
            case "middle_name":
                entity.setMiddleName(values.get(0));
                break;
            default:
                setSingleAttribute(name, values.get(0));
                break;
        }
    }

    @Override
    public String getFirstAttribute(String name) {

        // *****************************************
        log.info(">>>> GEY FIRST ATTRIBUTE >>>>");
        // *****************************************
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
//        Map<String, List<String>> attrs = super.getAttributes();
//        for (Map.Entry entry : attrs.entrySet()) {
//            System.out.println();
//            System.out.println("Attribute = " + entry.getKey() + " :: Value = " + entry.getValue());
//        }
//        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
//        all.putAll(attrs);

        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();

        all.add(UserModel.USERNAME, entity.getUsername());
        all.add(UserModel.FIRST_NAME, entity.getFirstName());
        all.add(UserModel.LAST_NAME, entity.getLastName());
        all.add(UserModel.EMAIL, entity.getEmail());

        all.add("phone", entity.getPhone());
        all.add("middle_name", entity.getMiddleName());
        return all;
    }

    @Override
    public boolean isEnabled() {
        return entity.getStatus().equals(ENABLED_TRUE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            entity.setStatus(ENABLED_TRUE);
        } else {
            entity.setStatus(ENABLED_FALSE);
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
