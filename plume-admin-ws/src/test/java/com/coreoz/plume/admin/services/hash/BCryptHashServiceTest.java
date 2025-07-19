package com.coreoz.plume.admin.services.hash;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;

public class BCryptHashServiceTest {

    @Test
    public void null_password_should_raise_an_error() {
        BCryptHashService hashService = new BCryptHashService();

        try {
            hashService.hashPassword(null);
            Assertions.fail("should raise an error");
        } catch (IllegalArgumentException e) {
            // as excepted, the password should not be null
        }
    }

    @Test
    public void non_null_password_should_be_hashed() {
        BCryptHashService hashService = new BCryptHashService();

        String hashedPassword = hashService.hashPassword("Test");
        Assertions.assertThat(Strings.isNullOrEmpty(hashedPassword)).isFalse();
        Assertions.assertThat(hashService.checkPassword("Test", hashedPassword)).isTrue();
    }
}
