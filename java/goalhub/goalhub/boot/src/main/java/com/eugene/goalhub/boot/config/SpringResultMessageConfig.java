package com.eugene.goalhub.boot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import response.ResultMessages;

import java.util.Locale;

/**
 * Result 响应消息国际化配置。
 */
@Component
public class SpringResultMessageConfig {

    /**
     * Spring 国际化消息源。
     */
    private final MessageSource messageSource;

    /**
     * 创建 Result 响应消息配置。
     *
     * @param messageSource Spring 国际化消息源
     */
    public SpringResultMessageConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 初始化 Result 消息解析器。
     */
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
