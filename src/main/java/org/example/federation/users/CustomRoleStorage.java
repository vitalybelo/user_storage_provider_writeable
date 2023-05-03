package org.example.federation.users;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserEntity;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.storage.role.RoleStorageProvider;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class CustomRoleStorage implements RoleProvider {

    protected EntityManager em;
    protected RealmModel realm;
    protected KeycloakSession session;

    public CustomRoleStorage(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    /**
     * Считывает из внешнего хранилища все имеющиеся там роли (с пагинацией)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public Set<UserRoleEntity> findAllRoles(int firstResult, int maxResults) {

        TypedQuery<UserRoleEntity> query = em.createNamedQuery("getAllRoles", UserRoleEntity.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultStream().collect(Collectors.toSet());
    }

    /**
     * Считывает из внешнего хранилища все имеющиеся роли
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public Set<UserRoleEntity> findAllRoles() {
        return findAllRoles(-1, -1);
    }

    /**
     * Считывает из внешнего хранилища роли, имена которых или описания удовлетворяют маске поиска search
     * @param search маска запроса (* - для загрузки всех пользователей)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
     * @return список ролей из внешнего jdbc хранилища (коллекцию экземпляров класса UserRoleEntity)
     */
    public Set<UserRoleEntity> findRoles(String search, int firstResult, int maxResults) {

        if (search.equalsIgnoreCase("*")) {
            return findAllRoles(-1, -1);
        }
        TypedQuery<UserRoleEntity> query = em.createNamedQuery("searchForRoles", UserRoleEntity.class);
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultStream().collect(Collectors.toSet());
    }

    /**
     * Загружает из внешнего хранилища запись по точному названию роли (чувствительно к регистру).
     * @param name название роли, которую необходимо найти и загрузить (в точности с регистром букв)
     * @return экземпляр класса UserRoleEntity в случае успешной загрузки, или null если роль не найдена
     */
    public UserRoleEntity findRoleByName(@NonNull String name) {

        TypedQuery<UserRoleEntity> query = em.createNamedQuery("getRoleByName",UserRoleEntity.class);
        query.setParameter("name", name);
        UserRoleEntity userRole = query.getSingleResult();
        if (userRole == null) {
            log.info(">>>>> Невозможно найти роль по названию = {} >>>>>", name);
        }
        return userRole;
    }

    /**
     * Добавление всех имеющихся ролей в рабочую область (realm).
     * Метод вызывает процедуру findAllRoles() для считывания всех ролей из внешнего jdbc хранилища.
     * Затем передаёт эту коллекцию в процедуру AddRolesToRealm(), которая последовательно проверяет наличие
     * каждой роли в рабочей области, и добавляет роль только если там ее нет.
     */
    public void AddRealmRolesAll() {
        AddRealmRoles(findAllRoles());
    }

    /**
     * Добавляет в рабочую область роли для пользователей. Список пользователей задается параметром маски
     * поиска пользователей во внешнем jdbc хранилище. Если задан параметр "*", осуществляется считывание ролей для
     * всех пользователей. В обратном случае, из внешнего хранилища выбираются пользователи, имена или почта которых
     * содержит маску поиска.
     * @param search строковая маска поиска пользователей, "*" для загрузки всех пользователей
     */
    public void AddRealmRolesForUsers(String search) {

        TypedQuery<UserEntity> query;
        if (search.equalsIgnoreCase("*")) {
            query = em.createNamedQuery("getAllUsers", UserEntity.class);
        } else {
            query = em.createNamedQuery("searchForUser", UserEntity.class);
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }
        List<UserEntity> usersList = query.getResultList();

        if (!usersList.isEmpty()) {
            Set<UserRoleEntity> rolesList = new HashSet<>();
            for (UserEntity user : usersList) {
                rolesList.addAll(user.getRoleList());
            }
            AddRealmRoles(rolesList);
        }
    }

    /**
     * Добавляет в рабочую область (realm) список ролей с описаниями и аттрибутами (если они заданы).
     * Метод предварительно проверяет наличие в realm каждой отдельной роли. Отсутствующие роли добавляются.
     * @param entitySet список ролей (экземпляров класса UserRoleEntity) для загрузки в рабочую область (realm)
     */
    public void AddRealmRoles(Set<UserRoleEntity> entitySet) {

        for (UserRoleEntity role : entitySet) {

            // пробуем получить роль из области
            String roleName = role.getName();
            RoleModel realmRole = realm.getRole(roleName);

            // если роль нет в области (она = null) - создаем новую
            if (realmRole == null) {

                // добавляем роль с описанием в рабочую область (realm)
                realmRole = realm.addRole(roleName);
                realmRole.setDescription(role.getDescription());

                // TODO - заменить на алгоритм добавления аттрибутов ролей или убрать совсем
                realmRole.setSingleAttribute("A1", "value 1");
                realmRole.setSingleAttribute("A2", "value 2");
            }
        }
    }

    public UserRoleEntity saveRole(RoleModel role) {

        // создаем новую пользовательскую роль
        UserRoleEntity entity = new UserRoleEntity();
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());

        System.out.println("\n>>>>>>> SAVE ROLE >>>>>>>>>");
        System.out.println(">>>>>>> name = " + entity.getName());
        System.out.println(">>>>>>> desc = " + entity.getDescription() + "\n");

        // добавляем роль во внешнее хранилище
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();

        return entity;
    }


    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>> ADD REALM ROLE >>>>>>>>>>>>>>>>>>>\n");
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        return null;
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        return null;
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

    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        return null;
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        return null;
    }

    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        return null;
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        return null;
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        return null;
    }
}
