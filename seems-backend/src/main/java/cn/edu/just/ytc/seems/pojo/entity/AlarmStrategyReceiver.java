package cn.edu.just.ytc.seems.pojo.entity;

import cn.edu.just.ytc.seems.pojo.enums.ReceiverType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.Index;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报警策略接收人关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_strategy_receiver")
@Schema(description = "报警策略接收人关联")
public class AlarmStrategyReceiver extends BaseEntity {

    @Schema(description = "策略ID")
    @TableField(value = "strategy_id")
    @Index(name = "idx_strategy")
    private Long strategyId;

    @Schema(description = "接收人类型")
    @TableField(value = "receiver_type")
    private ReceiverType receiverType;

    @Schema(description = "接收人ID（USER类型时使用）")
    @TableField(value = "receiver_id")
    @Index(name = "idx_receiver")
    private Long receiverId;

    @Schema(description = "接收人角色（ROLE类型时使用）")
    @TableField(value = "receiver_role")
    private String receiverRole;
}
