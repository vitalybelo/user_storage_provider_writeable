package org.example.federation.users;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserEntity;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
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
    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorage(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    /**
     * Считывает из внешнего хранилища все имеющиеся там роли (с пагинацией)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults  Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
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
     * @param search      маска запроса (* - для загрузки всех пользователей)
     * @param firstResult Первый результат для возврата. Игнорируется, если отрицательный или нулевой
     * @param maxResults  Максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или нулевой
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
     * Загружает из внешнего хранилища запись точно по названию роли (чувствителен к регистру).
     * @param name название роли, которую необходимо найти и загрузить (в точности с регистром букв)
     * @return экземпляр класса UserRoleEntity в случае успешной загрузки, или null если роль не найдена
     */
    public UserRoleEntity findRoleByName(@NonNull String name) {

        TypedQuery<UserRoleEntity> query = em.createNamedQuery("getRoleByName", UserRoleEntity.class);
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
    public void addRealmRolesAll() {
        addRealmRoles(findAllRoles());
    }

    /**
     * Добавляет в рабочую область роли для пользователей. Список пользователей задается параметром маски
     * поиска пользователей во внешнем jdbc хранилище. Если задан параметр "*", осуществляется считывание ролей для
     * всех пользователей. В обратном случае, из внешнего хранилища выбираются пользователи, имена или почта которых
     * содержит маску поиска.
     * @param search строковая маска поиска пользователей, "*" для загрузки всех пользователей
     */
    public void addRealmRolesForUsers(String search) {

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
            addRealmRoles(rolesList);
        }
    }

    /**
     * Метод проверяет наличие роли в рабочей области по названию. Если такой роли в рабочей области нет,
     * создается роль, добавляется описание и заполняется список атрибутов, если они заданы для роли.
     * @param userRole сущность роли из jdbc хранилища
     * @return модель роли из рабочей области
     */
    public RoleModel addRealmRole(UserRoleEntity userRole) {

        // пробуем получить роль из области
        String userRoleName = userRole.getName();
        RoleModel realmRole = session.roles().getRealmRole(realm, userRoleName); //realm.getRole(userRoleName);

        // если роли нет в области = null, тогда создаем новую
        if (realmRole == null) {

            // добавляем роль с описанием в рабочую область (realm)
            realmRole = session.roles().addRealmRole(realm, userRoleName); //realm.addRole(userRoleName);
            realmRole.setDescription(userRole.getDescription());

            // добавляем права (таблица rights) если они назначены для роли
            if (!userRole.getRightsList().isEmpty()) {
                RoleModel finalRealmRole = realmRole;
                userRole.getRightsList()
                        .forEach(r -> finalRealmRole.setSingleAttribute(r.getKeyName(), r.getValueName()));
            }
        }
        return realmRole;
    }


    /**
     * Добавляет в рабочую область (realm) список ролей с описаниями и аттрибутами (если они заданы).
     * Метод предварительно проверяет наличие в realm каждой отдельной роли. Отсутствующие роли добавляются.
     * @param entitySet список ролей (экземпляров класса UserRoleEntity) для загрузки в рабочую область (realm)
     */
    public void addRealmRoles(Set<UserRoleEntity> entitySet) {
        for (UserRoleEntity userRole : entitySet) {
            addRealmRole(userRole);
        }
    }

    /**
     * Сохраняет роль во внешнем хранилище
     * @param role модель роли из рабочей области
     * @return экземпляр класса UserRoleEntity, сущность роли в jdbc хранилище
     */
    public UserRoleEntity saveRole(RoleModel role) {

        // создаем новую пользовательскую модель роли
        UserRoleEntity entity = new UserRoleEntity();
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setModificationDate(new Timestamp(System.currentTimeMillis()));

        // записываем новую модель роли во внешнее хранилище
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();

        log.info(">>>> SAVE ROLE >>>> роль: \"{}\" добавлена в хранилище", role.getName());
        return entity;
    }


}

