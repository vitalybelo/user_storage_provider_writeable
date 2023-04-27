package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Slf4j
public class CustomRoleStorage {

    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;

    public CustomRoleStorage(KeycloakSession session, ComponentModel model) {
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
        return query.getResultList();
    }

}
