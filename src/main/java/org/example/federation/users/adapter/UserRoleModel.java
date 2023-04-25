package org.example.federation.users.adapter;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
@RequiredArgsConstructor
public class UserRoleModel implements RoleModel {

    private String name;
    private String description;
    private final RealmModel realm;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public void addCompositeRole(RoleModel role) {
    }

    @Override
    public void removeCompositeRole(RoleModel role) {
    }

    @Override
    public Stream<RoleModel> getCompositesStream() {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getCompositesStream(String s, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public boolean isClientRole() {
        return false;
    }

    @Override
    public String getContainerId() {
        return realm.getId();
    }

    @Override
    public RoleContainerModel getContainer() {
        return realm;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return this.equals(role) || this.name.equals(role.getName());
    }

    @Override
    public void setSingleAttribute(String name, String value) {
    }

    @Override
    public void setAttribute(String name, List<String> values) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return null;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return null;
    }
}
