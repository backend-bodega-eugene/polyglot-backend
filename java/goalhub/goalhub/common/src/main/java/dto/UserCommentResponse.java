package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentResponse {

    private Long id;

    private Long userId;

    private String contact;

    private String message;

    private String replyContent;

    private LocalDateTime replyTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}