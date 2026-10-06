package com.group.xlibris.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

public class MaskingFields extends PatternLayout {
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
            "(?i)((?:email|phone)\\s*=\\s*)([^,\\s]+)"
    );

    @Override
    public String doLayout(ILoggingEvent event) {
        String formatted = super.doLayout(event);
        return SENSITIVE_FIELD.matcher(formatted).replaceAll("$1*****");
    }
}
