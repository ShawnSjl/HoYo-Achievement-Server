package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("data_migration")
public class DataMigration {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String path;
    private String type;

    @TableField(value = "depends", typeHandler = JacksonTypeHandler.class)
    private List<String> depends;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime migration_time;
}
