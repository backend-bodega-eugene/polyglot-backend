package response;

/**
 * Result 响应消息解析器。
 */
public interface ResultMessageResolver {

    /**
     * 根据消息键解析响应消息。
     *
     * @param messageKey 消息键
     * @return 解析后的响应消息
     */
    String resolve(String messageKey);
}
