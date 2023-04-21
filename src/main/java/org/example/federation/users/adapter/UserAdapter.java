package org.example.federation.users.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.UserEntity;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    public String getFirstName() {
        return entity.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        entity.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return entity.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        entity.setLastName(lastName);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new LegacyUserCredentialManager(session, realm, this);
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
        Long timestamp = entity.getCreated();
        if (timestamp == null) {
            entity.setCreated(System.currentTimeMillis());
        }
        return entity.getCreated();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        entity.setCreated(Objects.requireNonNullElseGet(timestamp, System::currentTimeMillis));
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

        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();

        attributes.add(UserModel.USERNAME, entity.getUsername());
        attributes.add(UserModel.FIRST_NAME, entity.getFirstName());
        attributes.add(UserModel.LAST_NAME, entity.getLastName());
        attributes.add(UserModel.EMAIL, entity.getEmail());

        attributes.add("phone", entity.getPhone());
        attributes.add("middle_name", entity.getMiddleName());

        return attributes;
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
//        if (user.getRoles() != null) {
//            return user.getRoles().stream()
//                    .map(roleName -> new CustomUserRoleModel(roleName, realm)).collect(Collectors.toSet());
//        }
//        return Set.of();
        return Set.of();
    }

}
