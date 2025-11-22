package tech.sjiale.hoyo_achievement_server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("zzz_branch")
public class ZzzBranch {
    private Integer achievement_id;
    private Integer branch_id;
}
