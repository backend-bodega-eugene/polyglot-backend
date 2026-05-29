//package com.eugene.goalhub.boot.config;
//
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.context.MessageSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.i18n.LocaleContextHolder;
//import org.springframework.context.support.ResourceBundleMessageSource;
//import response.ResultMessageResolver;
//import response.ResultMessages;
//
//import java.util.Locale;
//
//@AutoConfiguration
//public class GoalhubBootAutoConfiguration {
//
//    @Bean
//    public MessageSource messageSource() {
//        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
//        source.setBasename("i18n/messages");
//        source.setDefaultEncoding("UTF-8");
//        source.setFallbackToSystemLocale(false);
//        source.setUseCodeAsDefaultMessage(true);
//        return source;
//    }
//
//    @Bean
//    public ResultMessageResolver resultMessageResolver(MessageSource messageSource) {
//        System.out.println("Result i18n resolver initialized");
//        ResultMessageResolver resolver = messageKey -> {
//
//            Locale locale = LocaleContextHolder.getLocale();
//            System.out.println("locale=" + locale);
//            System.out.println("messageKey=" + messageKey);
//            String message= messageSource.
//                    getMessage(messageKey, null, messageKey, locale);
//            System.out.println("result=" + message);
//            return message;
//        };
//
//        ResultMessages.setResolver(resolver);
//        return resolver;
//    }
//}