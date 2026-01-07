package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "data_migration", autoResultMap = true)
public class DataMigration {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "path")
    private String path;

    @TableField(value = "type")
    private String type;

    @TableField(value = "depends", typeHandler = JacksonTypeHandler.class)
    private List<String> depends;

    @TableField(value = "migration_time", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime migrationTime;

    // TODO 添加upload的上传者，本地数据可以写成‘-’或者‘root’
}
