package org.example.federation.users.encoder;

public interface KeycloakPasswordEncoder {

    /**
     * Кодирует необработанный пароль.
     * Как правило, хороший алгоритм кодирования применяет хэш SHA-1 или выше в сочетании с 8-байтовым или более
     * случайным образом на основании сгенерированной соли.
     */
    String encode(CharSequence rawPassword);

    /**
     * Проверяет, что закодированный пароль, полученный из хранилища, совпадает с отправленным необработанным
     * паролем после того, как он также будет закодирован. Возвращает true, если пароли совпадают, и false, если нет.
     * Сам сохраненный пароль никогда не расшифровывается.
     *
     * @param rawPassword необработанный пароль для кодирования и сопоставления с encodedPassword
     * @param encodedPassword зашифрованный пароль из хранилища для сравнения с введенным rawPassword
     * @return true, если необработанный пароль после кодирования совпадает с закодированным паролем из хранилища
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);

}
