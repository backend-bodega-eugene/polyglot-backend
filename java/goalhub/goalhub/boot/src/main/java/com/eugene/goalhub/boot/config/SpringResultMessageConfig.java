package com.eugene.goalhub.boot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import response.ResultMessages;

import java.util.Locale;

@Component
public class SpringResultMessageConfig {

    private final MessageSource messageSource;

    public SpringResultMessageConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void init() {
        System.out.println("Result i18n resolver initialized");

        ResultMessages.setResolver(messageKey -> {

            Locale locale = LocaleContextHolder.getLocale();

            System.out.println("locale=" + locale);
            System.out.println("messageKey=" + messageKey);

            String result = messageSource.getMessage(
                    messageKey,
                    null,
                    messageKey,
                    locale
            );

            System.out.println("result=" + result);

            return result;
        });
    }
}