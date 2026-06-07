package dto;

import lombok.Data;

@Data
public class UserCommentReplyRequest {

    private Long id;

    private String replyContent;
}