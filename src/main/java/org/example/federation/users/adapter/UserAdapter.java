package org.example.federation.users.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserEntity;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private static final String ENABLED_TRUE = "ACTIVE";
    private static final String ENABLED_FALSE = "DELETED";

    private static final String ATTRIBUTE_PHONE = "номер телефона";
    private static final String ATTRIBUTE_MIDDLE_NAME = "отчество";
    private static final String ATTRIBUTE_DEPARTMENT = "подразделение";
    private static final String ATTRIBUTE_POSITION = "должность";
    private static final String ATTRIBUTE_IP_ADDRESS = "IP адрес";
    private static final String ATTRIBUTE_BANNER_VIEWED = "показ баннера безопасности";
    private static final String ATTRIBUTE_BLOCKING_DATE = "дата начала блокировки аккаунта";

    protected UserEntity entity;
    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getAccountId()));
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

    public boolean isCustomAttributeMissedSetup(String name, String value) {
        switch (name) {
            case ATTRIBUTE_PHONE: entity.setPhone(value); return false;
            case ATTRIBUTE_MIDDLE_NAME: entity.setMiddleName(value); return false;
            case ATTRIBUTE_DEPARTMENT: entity.setDepartment(value); return false;
            case ATTRIBUTE_POSITION: entity.setPosition(value); return false;
            case ATTRIBUTE_IP_ADDRESS: entity.setIpAddress(value); return false;
            case ATTRIBUTE_BANNER_VIEWED: entity.setBannerViewed(Boolean.parseBoolean(value)); return false;
            default:
                return true;
        }
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (isCustomAttributeMissedSetup(name, value))
            super.setSingleAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        if (isCustomAttributeMissedSetup(name, null)) {
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
            default:
                if (isCustomAttributeMissedSetup(name, values.get(0))) {
                    setSingleAttribute(name, values.get(0));  
                }
                break;
        }
    }

    @Override
    public String getFirstAttribute(String name) {

        switch (name) {
            case ATTRIBUTE_PHONE: return entity.getPhone();
            case ATTRIBUTE_MIDDLE_NAME: return entity.getMiddleName();
            case ATTRIBUTE_DEPARTMENT: return entity.getDepartment();
            case ATTRIBUTE_POSITION: return entity.getPosition();
            case ATTRIBUTE_IP_ADDRESS: return entity.getIpAddress();
            case ATTRIBUTE_BANNER_VIEWED: return String.valueOf(entity.isBannerViewed());
            default:
                return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {

        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();

        // Добавляем стандартные атрибуты пользовательской модели Keycloak
        attributes.add(UserModel.USERNAME, entity.getUsername());
        attributes.add(UserModel.FIRST_NAME, entity.getFirstName());
        attributes.add(UserModel.LAST_NAME, entity.getLastName());
        attributes.add(UserModel.EMAIL, entity.getEmail());

        // Добавляем кастомные атрибуты к пользовательской модели Keycloak
        attributes.add(ATTRIBUTE_PHONE, entity.getPhone());
        attributes.add(ATTRIBUTE_MIDDLE_NAME, entity.getMiddleName());
        attributes.add(ATTRIBUTE_DEPARTMENT, entity.getDepartment());
        attributes.add(ATTRIBUTE_POSITION, entity.getPosition());
        attributes.add(ATTRIBUTE_IP_ADDRESS, entity.getIpAddress());
        attributes.add(ATTRIBUTE_BANNER_VIEWED, String.valueOf(entity.isBannerViewed()));

        return attributes;
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        if (entity.getRoleList() != null) {
            return entity.getRoleList().stream()
                    .map(role -> new UserRoleModel(role.getName(), role.getDescription(), realm)).collect(Collectors.toSet());
        }
        return Set.of();
    }

}
