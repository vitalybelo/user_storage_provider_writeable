package org.example.federation.users;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.adapter.UserAdapter;
import org.example.federation.users.encoder.KeycloakBCryptPasswordEncoder;
import org.example.federation.users.model.UserEntity;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.Collections;
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
    private static final boolean SIMULATION_DELETE_ACTION = true;

    CustomUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        this.em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
    }

    /**
     * Выполняет полное удаление роли из keycloak, синхронно роль удаляется из внешнего хранилища.
     * Изначально этот метод пытается найти роль во внешнем хранилище. Если роль найдена, тогда последовательно
     * удаляются все сопоставления роли сделанные для пользователей в хранилище. В результате в хранилище больше
     * не остается сопоставлений для этой роли. Затем сама роль удаляется из хранилища. Последнее действие,
     * удаление из keycloak.
     * @param realm рабочая область
     * @param role модель роли, которую необходимо полностью удалить из хранилища и keycloak
     */
    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

        UserRoleEntity roleEntity = new RoleStorage(session, model).findRoleByName(role.getName());
        if (roleEntity != null) {
            em.getTransaction().begin();
            if (!roleEntity.getUsersList().isEmpty()) {
                roleEntity.getUsersList().forEach(user -> user.getRoleList().remove(roleEntity));
            }
            em.remove(roleEntity);
            em.getTransaction().commit();
            log.info(">>>> удаление роли: \"{}\" из хранилища выполнено успешно", role.getName());
        } else {
            log.info(">>>> роли \"{}\" нет в хранилище. удаление записи не требуется", role.getName());
        }
        UserStorageProvider.super.preRemove(realm, role);
    }

    @Override
    public void close() {
    }

    /*
     * --------------------------------------------------------------------------------------------------------------
     * UserLookupProvider
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
    public UserModel getUserById(RealmModel realm, String id) {

        String persistenceId = StorageId.externalId(id);
        UserEntity user = em.find(UserEntity.class, Long.parseLong(persistenceId));

        if (user == null) {
            log.info(">>>> невозможно найти пользователя по id = {} >>>>", persistenceId);
            return null;
        }
        return new UserAdapter(session, realm, model, user);
    }

    /**
     * Точный поиск пользователя по его имени пользователя.
     * Возвращает пользователя с заданным именем пользователя, принадлежащего области
     * @param realm модель области
     * @param username имя пользователя, чувствительно к регистру
     * @return найденная модель пользователя или null, если такой пользователь не существует
     * @throws ModelDuplicateException при поиске в режиме без учета регистра и наличии большего количества
     * пользователей с именем пользователя, которое отличается только регистром.
     */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {

        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
        query.setParameter("username", username);
        List<UserEntity> userList = query.getResultList();

        if (userList.isEmpty()) {
            log.info(">>>> невозможно найти пользователя по имени_пользователя = {} >>>>", username);
            return null;
        }
        return new UserAdapter(session, realm, model, userList.get(0));
    }

    /**
     * Возвращает пользователя с данным адресом электронной почты, принадлежащим области
     * @param realm модель области
     * @param email email адрес для поиска
     * @return найденная модель пользователя или null, если такой пользователь не существует
     * @throws ModelDuplicateException когда есть больше пользователей с одним и тем же адресом электронной почты
     */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {

        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> userList = query.getResultList();

        if (userList.isEmpty()) {
            log.info(">>>> невозможно найти пользователя по email = {} >>>>", email);
            return null;
        }
        return new UserAdapter(session, realm, model, userList.get(0));
    }

    /*
     * --------------------------------------------------------------------------------------------------------------
     * UserRegistrationProvider
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
    public UserModel addUser(RealmModel realm, String username) {

        UserEntity userEntity = new UserEntity();
        userEntity.setStatus("ACTIVE");
        userEntity.setUsername(username);
        userEntity.setPassword(encoder.encode(username));
        userEntity.setCreated(System.currentTimeMillis());
        userEntity.setMaxIdleTime(10);
        userEntity.setMaxSessions(0);
        userEntity.setBlockingDate(null);
        userEntity.setBannerViewed(false);
        em.getTransaction().begin();
        em.persist(userEntity);
        em.getTransaction().commit();
        return new UserAdapter(session, realm, model, userEntity);
    }

    /**
     * Вызывается, если пользователь удаляется от этого провайдера.
     * Если локальный пользователь связан с этим провайдером, этот метод будет вызываться до вызова метода removeUser()
     * локального хранилища. Если вы используете стратегию импорта и это локальный пользователь, связанный с этим
     * провайдером, этот метод будет вызываться до вызова метода removeUser() локального хранилища. Кроме того, вам
     * НЕ нужно удалять импортированного пользователя. Среда выполнения сделает это за вас.
     * @param realm ссылка на realm
     * @param userModel ссылка на удаляемого пользователя
     * @return true, если пользователь был удален, иначе false
     */
    @Override
    public boolean removeUser(RealmModel realm, UserModel userModel) {

        // не удаляем пользователя из хранилища, только устанавливаем ему статус DELETE
        if (SIMULATION_DELETE_ACTION) {
            userModel.setEnabled(false);
            return true;
        }
        // удаляет пользователя из хранилища и keycloak (из хранилища удалению могут помешать внешние ключи)
        String persistenceId = StorageId.externalId(userModel.getId());
        UserEntity userEntity = em.find(UserEntity.class, Long.parseLong(persistenceId));
        if (userEntity == null) {
            return false;
        }
        em.getTransaction().begin();
        if (!userEntity.getRoleList().isEmpty()) {
            userEntity.getRoleList().forEach(role -> role.getUsersList().remove(userEntity));
        }
        em.remove(userEntity);
        em.getTransaction().commit();
        return true;
    }

    @Override
    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
        String password = ((UserAdapter)delegate).getPassword();
        if (password != null) {
            user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
        }
    }

    /*
     * --------------------------------------------------------------------------------------------------------------
     * CredentialInputValidator
     * CredentialInputUpdater
     * Внедрения этого интерфейса могут проверять CredentialInput, т. е. проверять пароль.
     * UserStorageProviders и CredentialProviders могут реализовать этот интерфейс.
     * Внедрения CredentialInputUpdater позволяет реализовать редактирование пароля
     * --------------------------------------------------------------------------------------------------------------
     */

    /**
     * Выполняет проверку на соответствие пароля политике паролей для рабочей области
     * @param realm рабочая область
     * @param user пользовательская модель юзера keycloak
     * @param credentialType - тип представления пароля
     * @return true - если соответствует, false если нет
     */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    /**
     * Проверяет, действительны ли учетные данные. Сверяет введенный пароль я паролем установленным для пользователя.
     * При наличии кастомных методов хеширования паролей, здесь следует установить проверку хеша сохраненного пароля
     * с введенным паролем при идентификации. Для этого используется метод сравнивающий сырой пароль с закодированным.
     * @param realm рабочая область
     * @param user пользовательская keycloak модель
     * @param input CredentialInput значение пароля введенного при идентификации
     * @return true - если пароль соответствует, false - нет
     */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel rawCredential = (UserCredentialModel)input;
        String encodedPassword = getPassword(user);

        return encoder.matches(rawCredential.getValue(), encodedPassword);
    }

    /**
     * Возвращает значение пароля из кэша или из пользовательской модели передаваемой на вход
     * @param user пользовательская keycloak модель
     * @return строковое значение сохраненного пароля пользователя
     */
    public String getPassword(UserModel user) {

        String password = null;
        if (user instanceof CachedUserModel) {
            password = (String)((CachedUserModel)user).getCachedWith().get(PASSWORD_CACHE_KEY);
        } else if (user instanceof UserAdapter) {
            password = ((UserAdapter)user).getPassword();
        }
        return password;
    }

    /**
     * Выполняет смену пароля для пользователя, ввод выполняется в консоли администратора в карточке пользователя.
     * Данный метод вызывается исключительно для того, чтобы провайдер смог внести изменения в нашу jdbc базу данных.
     * Если используется кастомное хеширования паролей, тогда здесь нужно закодировать пароль перед сохранением.
     * @param realm - рабочая область
     * @param user - пользовательская keycloak модель
     * @param input - CredentialInput значение введенного пароля для замены
     * @return true в случае успешной смены пароля, false если смена невозможна или провалена
     */
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel)input;
        UserAdapter adapter = getUserAdapter(user);
        adapter.setPasswordChangeDate(new Timestamp(System.currentTimeMillis()));
        adapter.setPassword(encoder.encode(cred.getValue()));
        return true;
    }

    /**
     * Возвращает указатель на адаптер пользовательской модели
     * @param user данные пользователя в пользовательской модели keycloak
     * @return экземпляр класса UserAdapter для переданной пользовательской модели
     */
    public UserAdapter getUserAdapter(UserModel user) {
        UserAdapter adapter;
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

    /*
     * --------------------------------------------------------------------------------------------------------------
     * UserQueryProvider
     * Это необязательный интерфейс возможностей, который предназначен для реализации любым UserStorageProvider,
     * поддерживающим сложные пользовательские запросы. Вы должны реализовать этот интерфейс, если хотите просматривать
     * и управлять пользователями из административной консоли.
     * !!! Обратите внимание, что все методы в этом интерфейсе должны ограничивать поиск только данными, доступными
     * в хранилище, которое представлено этим провайдером. Им не следует обращаться к другим поставщикам хранилищ для
     * получения дополнительной информации.
     * --------------------------------------------------------------------------------------------------------------
     */

    /**
     * Возвращает количество пользователей без учета какой-либо служебной учетной записи.
     * @param realm рабочая область
     * @return целочисленное значение общего количества пользователей федеративного хранилища
     */
    @Override
    public int getUsersCount(RealmModel realm) {
        Object count = em.createNamedQuery("getUserCount").getSingleResult();
        return ((Number)count).intValue();
    }

    /**
     * Читает из федеративного jdbc хранилища данные пользователей в модели UserEntity
     * @param firstResult сдвиг в списке для начала чтения
     * @param maxResults максимальное количество данных для чтения
     * @return коллекцию класса UserEntity, содержащую данные всех пользователей федеративного хранилища
     */
    public List<UserEntity> findAllUsers(int firstResult, int maxResults) {

        TypedQuery<UserEntity> query = em.createNamedQuery("getAllUsers", UserEntity.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    /**
     * Выполняет поиск в федеративном jdbc хранилище пользователей соответствующий маске запроса
     * @param search маска запроса для поиска
     *               <ul>
     *               <li>"*" - загрузит всех пользователей</li>
     *               <li>"#roles#{маска}" - загрузка и добавление ролей для пользователей, имена которых
     *               соответствуют маске поиска {маска}</li>
     *               </ul>
     * @param firstResult начальный сдвиг в списке
     * @param maxResults максимальное количество в списке
     * @return коллекцию класса UserEntity, содержащую данные пользователей из федеративного хранилища подходящих
     * по маске запроса
     */
    public List<UserEntity> findUsers(String search, int firstResult, int maxResults) {

        if (search.equalsIgnoreCase("*")) {
            // загрузка всех пользователей из внешнего хранилища
            return findAllUsers(firstResult, maxResults);
        }
        if (search.equalsIgnoreCase("***")) {
            // загрузка всех ролей и их атрибутов в рабочую область
            new RoleStorage(session, model).addAllRoles();
            return Collections.emptyList();
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

    /**
     * Ищет пользователей, чье имя пользователя, адрес почты, имя или фамилия содержат любую строку поиска.
     * Если возможно, реализации должны обрабатывать значения параметров как шаблоны частичного совпадения
     * (т. е. в терминах СУБД, используемых LIKE). Этот метод используется полем поиска консоли администратора.
     * @param realm рабочая область
     * @param search нечувствительный к регистру список поисковых слов, разделенных пробелами.
     * @return ненулевой поток пользователей, соответствующих строке поиска.
     */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        return findUsers(search, -1, -1).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    /**
     * Ищет пользователей, чье имя пользователя, адрес почты, имя или фамилия содержат любую строку поиска.
     * Если возможно, реализации должны обрабатывать значения параметров как шаблоны частичного совпадения
     * (т. е. в терминах СУБД, используемых LIKE). Этот метод используется полем поиска консоли администратора.
     * @param realm рабочая область
     * @param search нечувствительный к регистру список поисковых слов, разделенных пробелами.
     * @param firstResult первый результат для возврата. Игнорируется, если значение отрицательное, или null.
     * @param maxResults максимальное количество возвращаемых результатов. Игнорируется, если отрицательный или null.
     * @return ненулевой поток пользователей, соответствующих строке поиска.
     */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        return findUsers(search, firstResult, maxResults).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    /**
     * Ищет пользователя по параметру. Если возможно, реализации должны рассматривать значения параметров как
     * шаблоны частичного совпадения (т. е. в терминах RDMBS используйте LIKE).
     * @param realm рабочая область
     * @param params карта, содержащая параметры поиска
     * Допустимые параметры:
     * <ul>
     * <li>{@link UserModel#FIRST_NAME} — имя (строка без учета регистра)</li>
     * <li>{@link UserModel#LAST_NAME} — фамилия (строка без учета регистра)</li>
     * <li>{@link UserModel#EMAIL} – электронная почта (строка без учета регистра)</li>
     * <li>{@link UserModel#USERNAME} — имя пользователя (строка без учета регистра)</li>
     * <li>{@link UserModel#EMAIL_VERIFIED} — поиск только пользователей с подтвержденным/неподтвержденным адресом электронной почты (true/false)</li>
     * <li>{@link UserModel#ENABLED} — поиск только включенных/отключенных пользователей (true/false)</li>
     * <li>{@link UserModel#IDP_ALIAS} — поиск только пользователей с федеративным удостоверением.
     * из idp с настроенным псевдонимом (строка с учетом регистра)</li>
     * <li>{@link UserModel#IDP_USER_ID} – поиск пользователей с федеративным удостоверением с
     * данный идентификатор пользователя (строка с учетом регистра)</li>
     * </ul>
     *  Этот метод используется REST API при запросе пользователей.
     * @return ненулевой {@link Stream} пользователей, соответствующих параметрам поиска.
     */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        return findAllUsers(-1, -1).stream()
                .map(user -> new UserAdapter(session, realm, model, user));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return findAllUsers(firstResult, maxResults).stream()
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
