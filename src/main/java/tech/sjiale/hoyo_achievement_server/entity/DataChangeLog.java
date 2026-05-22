package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeAction;
import tech.sjiale.hoyo_achievement_server.entity.nume.ChangeEntityType;

@Data
@TableName("data_change_log")
public class DataChangeLog {
    @TableId(value = "version", type = IdType.AUTO)
    private Long version;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "entity_type")
    private ChangeEntityType entityType;

    @TableField(value = "entity_id")
    private String entityId;

    @TableField(value = "action")
    private ChangeAction action;
}
