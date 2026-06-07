package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppWithdrawOrderPageRequest {

    private String orderNo;

    private String currencyCode;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer pageIndex;

    private Integer pageSize;
}