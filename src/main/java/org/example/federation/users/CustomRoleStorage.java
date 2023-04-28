package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
     * Добавление всех имеющихся ролей в рабочую область (realm).
     * Метод сначала вызывает процедуру findAllRoles() считывания всех ролей из внешнего jdbc хранилища в коллекцию.
     * Затем передаёт эту коллекцию в процедуру AddRolesToRealm(), которая последовательно проверяет наличие
     * каждой роли в рабочей области, и добавляет роль только если там ее нет.
     */
    public void AddRolesToRealmAll() {
        //AddRolesToRealm(findAllRoles());
    }

    /**
     * Добавляет в рабочую область (realm) список ролей с описаниями и аттрибутами (если они заданы).
     * Метод предварительно проверяет наличие в realm каждой отдельной роли. Отсутствующие роли добавляются.
     * @param entitySet список ролей (экземпляров класса UserRoleEntity) для загрузки в рабочую область (realm)
     */
    public void AddRolesToRealm(Set<UserRoleEntity> entitySet) {

        for (UserRoleEntity role : entitySet) {

            String role_name = role.getName();
            RoleModel role_found = realm.getRole(role_name);

            if (role_found == null) {
                // значит такой роли нет в списке нашего Realm - добавляем по имени
                RoleModel added = realm.addRole(role_name);
                // теперь добавляем описание роли если оно было заранее задано
                String role_description = role.getDescription();
                if (!role_description.isBlank()) {
                    added.setDescription(role_description);
                }
                // теперь добавляем аттрибуты, если они заданы для роли
                added.setSingleAttribute("A1", "value 1");
                added.setSingleAttribute("A2", "value 2");
            }
        }

    }





}
