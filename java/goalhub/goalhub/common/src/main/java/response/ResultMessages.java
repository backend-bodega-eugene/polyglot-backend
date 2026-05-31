package response;

/**
 * Result 响应消息解析工具。
 */
public final class ResultMessages {

    /**
     * 当前消息解析器，默认直接返回消息键。
     */
    private static ResultMessageResolver resolver = messageKey -> messageKey;

    /**
     * 工具类私有构造器。
     */
    private ResultMessages() {
    }

    /**
     * 设置 Result 响应消息解析器。
     *
     * @param resolver 消息解析器
     */
    public static void setResolver(ResultMessageResolver resolver) {
        if (resolver != null) {
            ResultMessages.resolver = resolver;
        }
    }

    /**
     * 根据消息键获取响应消息。
     *
     * @param messageKey 消息键
     * @return 解析后的响应消息
     */
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
