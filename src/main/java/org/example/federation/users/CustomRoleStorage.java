package org.example.federation.users;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserEntity;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Slf4j
public class CustomRoleStorage {

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
        List<UserRoleEntity> roleList = query.getResultList();

        if (roleList.isEmpty()) {
            log.info(">>>>> findRoleByName() :: роль \"{}\" не найдена", name);
            return null;
        }
        return roleList.get(0);
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
        entity.setModificationDate(new Timestamp(System.currentTimeMillis()));

        // добавляем роль во внешнее хранилище
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();

        log.info(">>>> в хранилище добавлена роль: \"{}\"", role.getName());
        return entity;
    }




}
