package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentResponse {

    private Long id;

    private String type;

    private String title;

    private String summary;

    private String coverUrl;

    private String contentHtml;

    private String status;

    private Integer sort;

    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}