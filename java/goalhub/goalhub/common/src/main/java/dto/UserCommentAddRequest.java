package dto;

import lombok.Data;

@Data
public class UserCommentAddRequest {

    private String contact;

    private String message;
}