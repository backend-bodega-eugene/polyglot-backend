package com.mybatisplus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class AbstractEntity {
    @TableId(value = "id", type = IdType.AUTO)
    protected Long id;
//    @TableField(value = "UPDATE_TIME", fill = FieldFill.UPDATE)
//    protected LocalDateTime updateTime;
//    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
//    protected LocalDateTime createTime;
}