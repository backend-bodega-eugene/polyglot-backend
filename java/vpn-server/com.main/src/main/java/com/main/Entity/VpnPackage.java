package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vpn_package")
public class VpnPackage extends AbstractEntity {
    @Schema(description = "套餐类型")
    private Integer mempackageType;
    @Schema(description = "套餐金额")
    private BigDecimal mempackageAmount;
    @Schema(description = "套餐天数")
    private int mempackageQuantity;
    @Schema(description = "套餐货币,目前默认是usdt")
    private String mempackageCurrency;
    @Schema(description = "套餐描述")
    private String mempackageDescription;
    @Schema(description = "套餐标题")
    private String mempackageTitle;
    @Schema(description = "套餐状态 ,是否可用")
    private Integer mempackageStatus;
    @Schema(description = "下单计数(购买次数的意思 暂时不用)")
    private Integer mempackageCount;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "套餐创建时间")
    private LocalDateTime create_time;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "套餐图片")
    private String mempackageImages;
}
