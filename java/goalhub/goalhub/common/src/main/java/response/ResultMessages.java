package response;

public final class ResultMessages {

    private static ResultMessageResolver resolver = messageKey -> messageKey;

    private ResultMessages() {
    }

    public static void setResolver(ResultMessageResolver resolver) {
        if (resolver != null) {
            ResultMessages.resolver = resolver;
        }
    }

    public static String getMessage(String messageKey) {
        if (messageKey == null || messageKey.isBlank()) {
            return "";
        }

        try {
            return resolver.resolve(messageKey);
        } catch (Exception e) {
            return messageKey;
        }
    }
}