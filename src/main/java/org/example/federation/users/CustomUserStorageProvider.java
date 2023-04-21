package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.adapter.UserAdapter;
import org.example.federation.users.encoder.KeycloakBCryptPasswordEncoder;
import org.example.federation.users.model.UserEntity;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class CustomUserStorageProvider implements
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

    CustomUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    @Override
    public void preRemove(RealmModel realm) {}

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {}

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {}

    @Override
    public void close() {}

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO :: Реализация интерфейса UserLookupProvider
     * Это необязательный интерфейс возможностей, который предназначен для реализации любым UserStorageProvider,
     * поддерживающим базовые пользовательские запросы. Вы должны реализовать этот интерфейс, если хотите иметь
     * возможность входить в keycloak, используя пользователей из вашего хранилища.
     * !!! Обратите внимание, что все методы в этом интерфейсе должны ограничивать поиск только данными, доступными
     * в хранилище, которое представлено этим провайдером. Им не следует обращаться к другим поставщикам хранилищ для
     * получения дополнительной информации. Необязательный интерфейс возможностей, реализованный UserStorageProviders.
     * --------------------------------------------------------------------------------------------------------------
     */

    /**
     * Возвращает пользователя с заданным ID идентификатором, принадлежащего области
     * @param realm модель области
     * @param id id пользователя
     * @return найденная модель пользователя или null, если такой пользователь не существует
     */
    @Override
    public UserModel getUserById(RealmModel realm, String id)
    {
        String persistenceId = StorageId.externalId(id);
        UserEntity entity = em.find(UserEntity.class, Integer.parseInt(persistenceId));

        if (entity == null) {
            log.info(">>>> невозможно найти пользователя по id = {} >>>>", persistenceId);
            return null;
        }
        return new UserAdapter(session, realm, model, entity);
    }

    /**
     * Точный поиск пользователя по его имени пользователя.
     * Возвращает пользователя с заданным именем пользователя, принадлежащего области
     * @param realm модель области
     * @param username имя пользователя, чувствительно к регистру
     * @return найденная модель пользователя или null, если такой пользователь не существует
     * @Throws ModelDuplicateException — при поиске в режиме без учета регистра и наличии большего количества
     * пользователей с именем пользователя, которое отличается только регистром.
     */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username)
    {
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
        query.setParameter("username", username);
        List<UserEntity> result = query.getResultList();

        if (result.isEmpty()) {
            log.info(">>>> невозможно найти пользователя по имени_пользователя = {} >>>>", username);
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

    /**
     * Возвращает пользователя с данным адресом электронной почты, принадлежащим области
     * @param realm модель области
     * @param email email адрес для поиска
     * @return найденная модель пользователя или null, если такой пользователь не существует
     * @Throws ModelDuplicateException — когда есть больше пользователей с одним и тем же адресом электронной почты
     */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email)
    {
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();

        if (result.isEmpty()) {
            log.info(">>>> невозможно найти пользователя по email = {} >>>>", email);
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO :: Реализация интерфейса UserRegistrationProvider
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
        entity.setCreated(System.currentTimeMillis());
        entity.setMaxIdleTime(10);
        entity.setMaxSessions(0);
        entity.setBlockingDate(null);
        entity.setBannerViewed(false);
        em.persist(entity);
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
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

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

    /**
     * --------------------------------------------------------------------------------------------------------------
     * TODO :: Реализация интерфейса UserQueryProvider
     * Это необязательный интерфейс возможностей, который предназначен для реализации любым UserStorageProvider,
     * поддерживающим сложные пользовательские запросы. Вы должны реализовать этот интерфейс, если хотите просматривать
     * и управлять пользователями из административной консоли.
     * !!! Обратите внимание, что все методы в этом интерфейсе должны ограничивать поиск только данными, доступными
     * в хранилище, которое представлено этим провайдером. Им не следует обращаться к другим поставщикам хранилищ для
     * получения дополнительной информации.
     * --------------------------------------------------------------------------------------------------------------
     */

    /**
     *
     * @param realm the realm
     * @return
     */
    @Override
    public int getUsersCount(RealmModel realm) {
        Object count = em.createNamedQuery("getUserCount").getSingleResult();
        return ((Number)count).intValue();
    }

    public List<UserEntity> getAllUsers() {
        return getAllUsers(-1, -1);
    }

    public List<UserEntity> getAllUsers(int firstResult, int maxResults) {

        TypedQuery<UserEntity> query = em.createNamedQuery("getAllUsers", UserEntity.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    public List<UserEntity> findUsers(String search, int firstResult, int maxResults) {

        if (search.equalsIgnoreCase("*")) {
            return getAllUsers(firstResult, maxResults);
        }
        TypedQuery<UserEntity> query = em.createNamedQuery("searchForUser", UserEntity.class);
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        return findUsers(search, -1, -1).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        return getAllUsers().stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        return findUsers(search, firstResult, maxResults).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return getAllUsers(firstResult, maxResults).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }


    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return Stream.empty();
    }



}
