package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.encoder.KeycloakBCryptPasswordEncoder;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class MyUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        UserQueryProvider,
        CredentialInputUpdater,
        CredentialInputValidator,
        OnUserCache
{

    public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";

    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;
    private final KeycloakBCryptPasswordEncoder encoder = new KeycloakBCryptPasswordEncoder();

    MyUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO Реализация интерфейса serStorageProvider extends Provider
     * --------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void preRemove(RealmModel realm) {}

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {}

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {}

    @Override
    public void close() {}


    @Override
    public UserModel getUserById(RealmModel realm, String id)
    {
        String persistenceId = StorageId.externalId(id);
        // *****************************************************
        log.info(">>>> GET USER BY ID {} >>>>: ", id);
        // *****************************************************
        UserEntity entity = em.find(UserEntity.class, Integer.parseInt(persistenceId));
        if (entity == null) {
            log.info(">>>> COULD NOT FIND USER BY ID = {} >>> ", persistenceId);
            return null;
        }
        return new UserAdapter(session, realm, model, entity);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username)
    {
        // *****************************************************
        log.info(">>>> GET USER BY NAME: {} >>>>", username);
        // *****************************************************
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
        query.setParameter("username", username);
        List<UserEntity> result = query.getResultList();

        if (result.isEmpty()) {
            log.info(">>>> COULD NOT FIND USERNAME = {} >>>>", username);
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email)
    {
        // *****************************************************
        log.info(">>>> GET USER BY EMAIL: {} >>>>", email);
        // *****************************************************
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();
        if (result.isEmpty()) return null;
        return new UserAdapter(session, realm, model, result.get(0));
    }

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO Реализация интерфейса UserRegistrationProvider
     * Это необязательный интерфейс возможностей, который предназначен для реализации любым UserStorageProvider,
     * поддерживающим добавление новых пользователей. Вы должны реализовать этот интерфейс, если хотите использовать
     * это хранилище для регистрации новых пользователей.
     * --------------------------------------------------------------------------------------------------------------
     */

    /**
     * Все поставщики хранилищ, реализующие этот интерфейс, будут проходить через цикл.
     * Если этот метод возвращает значение null, будет вызван метод addUser() следующего поставщика хранилища.
     * Если никакие поставщики хранилища не обрабатывают добавление, пользователь будет создан в локальном хранилище.
     * Возврат null полезен, когда вам нужна дополнительная поддержка для добавления пользователей. Например, наш
     * LDAP-провайдер может включать и отключать возможность добавления пользователей.
     * @param realm ссылка на область
     * @param username имя пользователя, которому будет присвоен созданный новый пользователь
     * @return модель созданного пользователя
     */
    @Override
    public UserModel addUser(RealmModel realm, String username)
    {
        UserEntity entity = new UserEntity();
        entity.setStatus("ACTIVE");
        entity.setUsername(username);
        entity.setPassword(encoder.encode(username));
        em.persist(entity);
        log.info(">>>> ADDED USER: {}", username);
        return new UserAdapter(session, realm, model, entity);
    }

    /**
     * Вызывается, если пользователь удаляется от этого провайдера.
     * Если локальный пользователь связан с этим провайдером, этот метод будет вызываться до вызова метода removeUser()
     * локального хранилища. Если вы используете стратегию импорта и это локальный пользователь, связанный с этим
     * провайдером, этот метод будет вызываться до вызова метода removeUser() локального хранилища. Кроме того, вам
     * НЕ нужно удалять импортированного пользователя. Среда выполнения сделает это за вас.
     * @param realm ссылка на царство
     * @param user ссылка на удаляемого пользователя
     * @return true, если пользователь был удален, иначе false
     */
    @Override
    public boolean removeUser(RealmModel realm, UserModel user)
    {
        String persistenceId = StorageId.externalId(user.getId());
        UserEntity entity = em.find(UserEntity.class, Integer.parseInt(persistenceId));
        if (entity == null) {
            return false;
        }
        em.remove(entity);
        return true;
    }

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO Реализация интерфейса OnCache
     * --------------------------------------------------------------------------------------------------------------
     */
    @Override
    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
        String password = ((UserAdapter)delegate).getPassword();
        if (password != null) {
            user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
        }
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input)
    {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel rawCredential = (UserCredentialModel)input;
        String encodedPassword = getPassword(user);

        return encoder.matches(rawCredential.getValue(), encodedPassword);
    }

    public String getPassword(UserModel user) {
        String password = null;
        if (user instanceof CachedUserModel) {
            password = (String)((CachedUserModel)user).getCachedWith().get(PASSWORD_CACHE_KEY);
        } else if (user instanceof UserAdapter) {
            password = ((UserAdapter)user).getPassword();
        }
        return password;
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel)input;
        UserAdapter adapter = getUserAdapter(user);
        adapter.setPassword(encoder.encode(cred.getValue()));
        return true;
    }

    public UserAdapter getUserAdapter(UserModel user) {
        UserAdapter adapter = null;
        if (user instanceof CachedUserModel) {
            adapter = (UserAdapter)((CachedUserModel)user).getDelegateForUpdate();
        } else {
            adapter = (UserAdapter)user;
        }
        return adapter;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return;
        getUserAdapter(user).setPassword(null);
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        log.info(">>>> GET USERS COUNT >>>>");
        Object count = em.createNamedQuery("getUserCount").getSingleResult();
        return ((Number)count).intValue();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return null;
    }

//    @Override
//    public List<UserModel> getUsers(RealmModel realm) {
//        return getUsers(realm, -1, -1);
//    }
//
//    @Override
//    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
//
//        TypedQuery<UserEntity> query = em.createNamedQuery("getAllUsers", UserEntity.class);
//        if (firstResult != -1) {
//            query.setFirstResult(firstResult);
//        }
//        if (maxResults != -1) {
//            query.setMaxResults(maxResults);
//        }
//        List<UserEntity> results = query.getResultList();
//        List<UserModel> users = new LinkedList<>();
//        for (UserEntity entity : results) users.add(new UserAdapter(session, realm, model, entity));
//        return users;
//    }
//
//    @Override
//    public List<UserModel> searchForUser(String search, RealmModel realm) {
//        return searchForUser(search, realm, -1, -1);
//    }
//
//    @Override
//    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
//        TypedQuery<UserEntity> query = em.createNamedQuery("searchForUser", UserEntity.class);
//        query.setParameter("search", "%" + search.toLowerCase() + "%");
//        if (firstResult != -1) {
//            query.setFirstResult(firstResult);
//        }
//        if (maxResults != -1) {
//            query.setMaxResults(maxResults);
//        }
//        List<UserEntity> results = query.getResultList();
//        List<UserModel> users = new LinkedList<>();
//        for (UserEntity entity : results) users.add(new UserAdapter(session, realm, model, entity));
//        return users;
//    }


}
