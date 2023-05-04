package org.example.federation.users.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.federation.users.CustomRoleStorage;
import org.example.federation.users.model.UserEntity;
import org.example.federation.users.model.UserRoleEntity;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage
{

    private static final String ENABLED_TRUE = "ACTIVE";
    private static final String ENABLED_FALSE = "DELETED";

    private static final String ATTRIBUTE_PHONE = "номер телефона";
    private static final String ATTRIBUTE_MIDDLE_NAME = "отчество";
    private static final String ATTRIBUTE_DEPARTMENT = "подразделение";
    private static final String ATTRIBUTE_POSITION = "должность";
    private static final String ATTRIBUTE_IP_ADDRESS = "IP адрес";
    private static final String ATTRIBUTE_BANNER_VIEWED = "показ баннера безопасности";

    protected UserEntity entity;
    protected String keycloakId;
    protected KeycloakSession session;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {

        super(session, realm, model);
        this.session = session;
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getAccountId()));
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    public String getPassword() {
        return entity.getPassword();
    }

    public void setPassword(String password) {
        entity.setPassword(password);
    }

    @Override
    public String getUsername() {
        return entity.getUsername();
    }

    @Override
    public void setUsername(String username) {
        entity.setUsername(username);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public String getFirstName() {
        return entity.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        entity.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return entity.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        entity.setLastName(lastName);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new LegacyUserCredentialManager(session, realm, this);
    }

    @Override
    public boolean isEnabled() {
        return entity.getStatus().equals(ENABLED_TRUE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            entity.setStatus(ENABLED_TRUE);
        } else {
            entity.setStatus(ENABLED_FALSE);
        }
        super.setEnabled(enabled);
    }

    @Override
    public boolean isEmailVerified() {
        return super.isEmailVerified();
    }

    @Override
    public void setEmailVerified(boolean verified) {
        super.setEmailVerified(verified);
    }

    @Override
    public Long getCreatedTimestamp() {
        Long timestamp = entity.getCreated();
        if (timestamp == null) {
            entity.setCreated(System.currentTimeMillis());
        }
        return entity.getCreated();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        entity.setCreated(Objects.requireNonNullElseGet(timestamp, System::currentTimeMillis));
    }

    public void setPasswordChangeDate(Timestamp now) {
        entity.setPasswordChangeDate(now);
    }

    /**
     * Устанавливает кастомный аттрибут для пользователя.
     * Метод вызывается после того, как в консоли администратора при редактировании аттрибутов пользователя была
     * нажата кнопка "сохранить". Метод проверяет соответствует ли имя аттрибута реальному полю из кастомного хранилища.
     * Если соответствует, выполняется изменения значения аттрибута в классе UserEntity и синхронно в jdbc хранилище.
     * @param name название аттрибута
     * @param value значение аттрибута
     * @return true если ни один из кастомных аттрибутов не был установлен и требуется установка методом из super
     */
    public boolean isCustomAttributeMissedSetup(String name, String value) {
        switch (name) {
            case ATTRIBUTE_PHONE: entity.setPhone(value); return false;
            case ATTRIBUTE_MIDDLE_NAME: entity.setMiddleName(value); return false;
            case ATTRIBUTE_DEPARTMENT: entity.setDepartment(value); return false;
            case ATTRIBUTE_POSITION: entity.setPosition(value); return false;
            case ATTRIBUTE_IP_ADDRESS: entity.setIpAddress(value); return false;
            case ATTRIBUTE_BANNER_VIEWED: entity.setBannerViewed(Boolean.parseBoolean(value)); return false;
            default:
                return true;
        }
    }

    /**
     * Установить одно значение указанного атрибута. Это метод вызывает другой метод для проверки имени на соответствие
     * - isCustomAttributeMissedSetup(), который непосредственно меняет значение аттрибута в классе UserEntity и
     * сохраняет изменения в jdbc хранилище, если указанное имя атрибута соответствует полю из внешнего хранилища
     * @param name имя аттрибута
     * @param value новое значение аттрибута
     */
    @Override
    public void setSingleAttribute(String name, String value) {
        if (isCustomAttributeMissedSetup(name, value))
            super.setSingleAttribute(name, value);
    }

    /**
     * Удаляет одно значение указанного атрибута. Это метод вызывает другой метод для проверки имени на соответствие
     * - isCustomAttributeMissedSetup(), который непосредственно меняет значение аттрибута в классе UserEntity на null
     * и сохраняет изменения в jdbc хранилище, если указанное имя атрибута соответствует полю из внешнего хранилища
     * @param name имя аттрибута
     */
    @Override
    public void removeAttribute(String name) {
        if (isCustomAttributeMissedSetup(name, null)) {
            super.removeAttribute(name);
        }
    }

    /**
     * Инициализирует аттрибуты пользователя в модели keycloak.
     * Метод используется для последовательной инициализации всех аттрибутов пользователя из карты атрибутов
     * @param name - имя атрибута
     * @param values - список значения, из которого используется первый элемент
     */
    @Override
    public void setAttribute(String name, List<String> values) {

        if (values.isEmpty()) return;
        switch (name) {
            case UserModel.LAST_NAME: entity.setLastName(values.get(0)); break;
            case UserModel.FIRST_NAME: entity.setFirstName(values.get(0)); break;
            case UserModel.EMAIL: entity.setEmail(values.get(0)); break;
            default:
                if (isCustomAttributeMissedSetup(name, values.get(0))) {
                    setSingleAttribute(name, values.get(0));  
                }
                break;
        }
    }

    /**
     * Возвращает значение атрибута. Метод необходим для отображения атрибутов в консоли keycloak
     * @param name имя атрибута
     * @return null, если нет никакого значения указанного атрибута или первого значения в противном случае.
     * Не генерировать исключение, если есть еще значения атрибута
     */
    @Override
    public String getFirstAttribute(String name) {

        switch (name) {
            case ATTRIBUTE_PHONE: return entity.getPhone();
            case ATTRIBUTE_MIDDLE_NAME: return entity.getMiddleName();
            case ATTRIBUTE_DEPARTMENT: return entity.getDepartment();
            case ATTRIBUTE_POSITION: return entity.getPosition();
            case ATTRIBUTE_IP_ADDRESS: return entity.getIpAddress();
            case ATTRIBUTE_BANNER_VIEWED: return String.valueOf(entity.isBannerViewed());
            default:
                return super.getFirstAttribute(name);
        }
    }

    /**
     * Это метод возвращает весь список атрибутов пользователя для отображения в консоли keycloak.
     * Здесь необходимо указать 1 обязательный атрибут - username, 3 необязательных стандартных атрибутов keycloak
     * и обязательно весь список кастомных атрибутов пользователя, если хотите чтобы они были добавлены в карточку
     * пользователя в административной консоли keycloak
     * @return MultivaluedHashMap карту со списком всех атрибутов и их значений
     */
    @Override
    public Map<String, List<String>> getAttributes() {

        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();

        // Добавляем стандартные атрибуты пользовательской модели Keycloak
        attributes.add(UserModel.USERNAME, entity.getUsername());
        attributes.add(UserModel.FIRST_NAME, entity.getFirstName());
        attributes.add(UserModel.LAST_NAME, entity.getLastName());
        attributes.add(UserModel.EMAIL, entity.getEmail());

        // Добавляем кастомные атрибуты к пользовательской модели Keycloak
        attributes.add(ATTRIBUTE_PHONE, entity.getPhone());
        attributes.add(ATTRIBUTE_MIDDLE_NAME, entity.getMiddleName());
        attributes.add(ATTRIBUTE_DEPARTMENT, entity.getDepartment());
        attributes.add(ATTRIBUTE_POSITION, entity.getPosition());
        attributes.add(ATTRIBUTE_IP_ADDRESS, entity.getIpAddress());
        attributes.add(ATTRIBUTE_BANNER_VIEWED, String.valueOf(entity.isBannerViewed()));

        return attributes;
    }

    /**
     * Получает все значения, связанные с указанным именем атрибута.
     * @param name имя атрибута
     * @return список всех значений для имени атрибута
     */
    @Override
    public Stream<String> getAttributeStream(String name) {
        Map<String, List<String>> attributes = getAttributes();
        return (attributes.containsKey(name)) ? attributes.get(name).stream() : Stream.empty();
    }

    /**
     * Создает внутреннее сопоставление ролей для текущего пользователя.
     * Метод используется в том случае, если нужно быстро добавить сопоставление ролей для федеративных пользователей,
     * которыми потом вы не планирует управлять всеми средствами административной консоли keycloak (режим read-only).
     * @return Set набор ролей в виде экземпляров класса UserRoleModel (имплементация от UserModel)
     */
    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
//        if (entity.getRoleList() != null) {
//            return entity.getRoleList().stream()
//                    .map(roleName -> new UserRoleModel(roleName, realm)).collect(Collectors.toSet());
//        }
        return Set.of();
    }

    /**
     * Несмотря на то, что он Deprecated, этот метод по сути является главным для получения ролей пользователя.
     * Сначала метод получает список сопоставления ролей из федеративного хранилища getFederatedRoleMappings().
     * В этом списке содержатся роли из области, которые уже назначены текущему пользователю. Далее, вызывается
     * кастомный (пользовательский) метод, который проверяет список ролей сопоставленных пользователю во внешнем
     * хранилище со списком ролей, которые назначены этому же пользователю в keycloak. Метод обнаруживает роли,
     * которые необходимо добавить в список сопоставлений keycloak, и добавляет их.
     * Затем добавляются роли по умолчанию (отключается переопределением метода appendDefaultRolesToRoleMappings().
     * И в конце вызывает метод getRoleMappingsInternal() для добавления ролей от провайдера. Мы должны обязательно
     * переопределить метод getRoleMappingsInternal(), чтобы добавить в сопоставление роли от вашего провайдера,
     * но только в том случае, если мы не планируем самостоятельно управлять федеративными ролями в keycloak.
     * @return список всех назначенных ролей для пользователя в keycloak.
     */
    @Override
    public Set<RoleModel> getRoleMappings() {

        // следующее действие - стандартная реализация метода getRoleMappings()
        Set<RoleModel> set = new HashSet<>(getFederatedRoleMappings());

        // здесь начинается кастомный метод сопоставления списка ролей хранилища и списка ролей keycloak
        // ---------------------------------------------------------------------------------------------
        log.info(">>>> GET_ROLE_MAPPING :: проверка сопоставление ролей для: {}", entity.getUsername());
        for (UserRoleEntity role : entity.getRoleList())
        {
            String entityRoleName = role.getName();
            RoleModel realmRole = realm.getRole(role.getName());
            if (realmRole == null) {
                realmRole = realm.addRole(entityRoleName);
                realmRole.setDescription(role.getDescription());

                // TODO - заменить на алгоритм добавления аттрибутов ролей или убрать совсем

                realmRole.setSingleAttribute("A1", "value 1");
                realmRole.setSingleAttribute("A2", "value 2");

            }
            Optional<RoleModel> optional =
                    set.stream().filter(r-> r.getName().equals(entityRoleName)).findFirst();

            if (optional.isEmpty()) {
                log.info(">>>>> Добавлена роль {} (пользователя: {}) ", entityRoleName, entity.getUsername());
                grantRole(realmRole);
                set.add(realmRole);
            }
        }
        // ------------------------------------------------------------------------------------------------
        // здесь заканчивается кастомный метод сопоставления списка ролей хранилища и списка ролей keycloak

        // следующие действия - стандартная реализация метода getRoleMappings()
        // добавление стандартных и композитных ролей из области если они там есть
        if (appendDefaultRolesToRoleMappings()) {
            set.addAll(realm.getDefaultRole().getCompositesStream().collect(Collectors.toSet()));
        }
        // добавление внутренних ролей (простое добавление ролей федеративных пользователей)
        set.addAll(getRoleMappingsInternal());
        return set;
    }

    /**
     * Возвращает поток ролей области, которые непосредственно установлены для этого объекта.
     * <br>По сути выполняется<br>
     * getRoleMappings().stream()<br>
     * .filter(RoleUtils::isRealmRole).collect(Collectors.toSet());<br>
     * @return поток ролей области, которые непосредственно установлены для этого объекта
     */
    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return super.getRealmRoleMappingsStream();
    }

    /**
     * Должны ли группы области по умолчанию добавляться к вызову getGroups()?
     * Если ваш поставщик хранилища не управляет сопоставлениями групп, рекомендуется возвращать true.
     */
    @Override
    protected boolean appendDefaultGroups() {
        return true;
    }

    /**
     * Следует ли добавлять роли области по умолчанию к вызову getRoleMappings()?
     * Если ваш поставщик хранилища не управляет всеми сопоставлениями ролей, рекомендуется возвращать true.
     * @return true если надо добавлять в сопоставление ролей - роли по умолчанию, false не надо добавлять
     */
    @Override
    protected boolean appendDefaultRolesToRoleMappings() {
        return true;
    }

    /**
     * Удаляет сопоставления роли для текущего пользователя.
     * Первоначально выполняется проверка, состоит ли роль в списке уже назначенных ролей для этого пользователя.
     * Если совпадение найдено, роль удаляется из списка, и связанного с ним списка пользователей в сущности ролей.
     * Синхронно удаляется сопоставление из внешнего хранилища (таблица privfastsm.account_role).
     * После этого удаляется сопоставления внутри keycloak.
     * @param role модель роли для удаления
     */
    @Override
    public void deleteRoleMapping(RoleModel role) {

        Optional<UserRoleEntity> optional = entity.getRoleList().stream()
                .filter(r-> r.getName().equals(role.getName())).findFirst();

        optional.ifPresent(userRole -> entity.removeUserRole(userRole));
        log.info(">>>> сопоставление роли \"{}\" удалено (пользователь: {})", role.getName(), entity.getUsername());
        super.deleteRoleMapping(role);
    }

    /**
     * Выполняет добавление в сопоставление ролей для текущего пользователя. Метод выполняет поиск во внешнем
     * хранилище роли с подобным названием. Если аналогичная роль будет найдена во внешнем хранилище, она добавляется
     * в отдельный список сопоставлений (таблица privfastsm.account_role). Если подобной роли не существует во внешнем
     * хранилище, создаем эту роль в там. После всего этого назначаем роль пользователю.
     * @param role модель роли которую нужно назначить текущему пользователю
     */
    @Override
    public void grantRole(RoleModel role) {

        CustomRoleStorage roleStorage = new CustomRoleStorage(session);
        UserRoleEntity userRoleEntity = roleStorage.findRoleByName(role.getName());

        if (userRoleEntity == null) {
            userRoleEntity = roleStorage.saveRole(role);
        }
        log.info(">>>> GRANT ROLE >>>> добавлено сопоставление роли \"{}\" (для пользователя: {})",
                role.getName(), entity.getUsername());
        entity.addUserRole(userRoleEntity);
        super.grantRole(role);
    }




}
