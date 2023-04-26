package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.adapter.UserRoleModel;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleLookupProvider;
import org.keycloak.storage.role.RoleStorageProvider;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CustomRoleStorageProvider implements RoleStorageProvider, RoleLookupProvider, RoleProvider
{

    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorageProvider(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    /**
     * Считывает из внешнего хранилища все имеющиеся там роли (с пагинацией)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public List<UserRoleEntity> findAllRoles(int firstResult, int maxResults) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА FIND_ALL_ROLES >>>>>>>>>>>>");
        System.out.println();

        TypedQuery<UserRoleEntity> query = em.createNamedQuery("getAllRoles", UserRoleEntity.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    /**
     * Считывает из внешнего хранилища все имеющиеся роли
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public List<UserRoleEntity> findAllRoles() {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА FIND_ALL_ROLES >>>>>>>>>>>>");
        System.out.println();
        return findAllRoles(-1, -1);
    }

    /**
     * Считывает из внешнего хранилища роли, имена которых или описания удовлетворяют маске поиска search
     * @param search маска запроса (* - для загрузки всех пользователей)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public List<UserRoleEntity> findRoles(String search, int firstResult, int maxResults) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА FIND_ROLES >>>>>>>>>>>>");
        System.out.println();
        if (search.equalsIgnoreCase("*")) {
            return findAllRoles(firstResult, maxResults);
        }
        TypedQuery<UserRoleEntity> query = em.createNamedQuery("searchForRoles", UserRoleEntity.class);
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    // ******************************************************************************************************
    //  TODO :: Реализация интерфейса RoleLookupProvider
    //  Интерфейс нужен для поиска как ролей области, так и ролей клиента по идентификатору, имени и описанию
    //  *****************************************************************************************************

    /**
     * Точный поиск роли по имени (чувствителен к регистру).
     * @param realm Рабочая область
     * @param name Строковое имя роли
     * @return Модель роли или null, если роль не найдена.
     */
    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА getRealmRole() >>>>>>>>>>>>");
        System.out.println(">>>>>>>>>>> Name = " + name);
        System.out.println();
        TypedQuery<UserRoleEntity> query = em.createNamedQuery("getRoleByName", UserRoleEntity.class);
        query.setParameter("name", name);
        List<UserRoleEntity> roles = query.getResultList();
        if (roles.isEmpty()) {
            log.info(">>>> невозможно найти роль с именем = {} >>>>", name);
            return null;
        }
        return new UserRoleModel(roles.get(0).getName(), roles.get(0).getDescription(), realm);
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА getRealmId() >>>>>>>>>>>>");
        System.out.println(">>>>>>>>>>> ID = " + id);
        System.out.println();
        return getRealmRole(realm, id);
    }

    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА searchForRolesStream() >>>>>>>>>>>>");
        System.out.println(">>>>>>>>>>> ID = " + search);
        System.out.println();
        return findRoles(search, first, max).stream()
                .map(role -> new UserRoleModel(role.getName(), role.getDescription(), realm));
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        return Stream.empty();
    }





    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        return new UserRoleModel(name, "", realm);
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        System.out.println();
        System.out.println(">>>>>>>>>>> ВЫЗОВ МЕТОДА getRealmRolesStream() >>>>>>>>>>>>");
        System.out.println();
        return findAllRoles(first, max).stream()
                .map(role -> new UserRoleModel(role.getName(), role.getDescription(), realm));
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        return findAllRoles(first, max).stream()
                .map(role -> new UserRoleModel(role.getName(), role.getDescription(), realm));
    }

    @Override
    public boolean removeRole(RoleModel role) {
        return false;
    }

    @Override
    public void removeRoles(RealmModel realm) {
    }

    @Override
    public RoleModel addClientRole(ClientModel client, String id, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client, Integer first, Integer max) {
        return null;
    }

    @Override
    public void removeRoles(ClientModel client) {

    }

    @Override
    public void close() {

    }


}
