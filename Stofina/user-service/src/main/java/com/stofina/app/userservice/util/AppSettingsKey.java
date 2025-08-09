package com.stofina.app.userservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppSettingsKey {
    public static final String RESET_PASSWORD_URL_FORMAT = "%s/reset-password?token=%s";
    public static final String ACTIVATE_PASSWORD_URL_FORMAT = "%s/create-password?token=%s";


}