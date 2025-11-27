package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("zzz_branch")
public class ZzzBranch {
    @TableField(value = "achievement_id")
    private Integer achievementId;

    @TableField(value = "branch_id")
    private Integer branchId;
}
