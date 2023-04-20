package org.example.federation.users.encoder;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Реализация PasswordEncoder, использующая функцию строгого хеширования BCrypt.
 * Клиенты могут дополнительно указать «strength» (также известную как обходы журнала в BCrypt) и экземпляр SecureRandom.
 * Чем больше параметр прочности, тем больше работы придется проделать (в геометрической прогрессии) для хеширования
 * паролей. Значение по умолчанию — 10.
 *
 * @author Dave Syer
 *
 */
public class KeycloakBCryptPasswordEncoder implements KeycloakPasswordEncoder
{
    private final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");
    private final Log logger = LogFactory.getLog(getClass());

    private final int strength;

    private final SecureRandom random;

    public KeycloakBCryptPasswordEncoder() {
        this(-1);
    }

    /**
     * @param strength the log rounds to use
     */
    public KeycloakBCryptPasswordEncoder(int strength) {
        this(strength, null);
    }

    /**
     * @param strength the log rounds to use
     * @param random the secure random instance to use
     *
     */
    public KeycloakBCryptPasswordEncoder(int strength, SecureRandom random) {
        this.strength = strength;
        this.random = random;
    }

    public String encode(CharSequence rawPassword) {
        String salt;
        if (strength > 0) {
            if (random != null) {
                salt = KeycloakBCrypt.gensalt(strength, random);
            }
            else {
                salt = KeycloakBCrypt.gensalt(strength);
            }
        }
        else {
            salt = KeycloakBCrypt.gensalt();
        }
        return KeycloakBCrypt.hashpw(rawPassword.toString(), salt);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            logger.warn("Empty encoded password");
            return false;
        }

        if (!BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            logger.warn("Encoded password does not look like BCrypt");
            return false;
        }

        return KeycloakBCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}
