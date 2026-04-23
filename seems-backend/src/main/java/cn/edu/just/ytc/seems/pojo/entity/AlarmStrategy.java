package cn.edu.just.ytc.seems.pojo.entity;

import cn.edu.just.ytc.seems.pojo.enums.TriggerCondition;
import cn.edu.just.ytc.seems.pojo.enums.TriggerTiming;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.Index;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 报警策略配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_strategy")
@Schema(description = "报警策略配置")
public class AlarmStrategy extends BaseEntity {

    @Schema(description = "船舶ID（NULL表示全局策略）")
    @TableField(value = "ship_id")
    @Index(name = "idx_ship_alarm")
    private Long shipId;

    @Schema(description = "报警类型")
    @TableField(value = "alarm_type")
    private String alarmType;

    @Schema(description = "报警级别")
    @TableField(value = "alarm_level")
    private String alarmLevel;

    @Schema(description = "触发条件")
    @TableField(value = "trigger_condition")
    private TriggerCondition triggerCondition;

    @Schema(description = "阈值1")
    @TableField(value = "threshold_value")
    private BigDecimal thresholdValue;

    @Schema(description = "阈值2（BETWEEN条件用）")
    @TableField(value = "threshold_value2")
    private BigDecimal thresholdValue2;

    @Schema(description = "触发时机")
    @TableField(value = "trigger_timing")
    private TriggerTiming triggerTiming;

    @Schema(description = "持续时间（秒）")
    @TableField(value = "duration_seconds")
    private Integer durationSeconds;

    @Schema(description = "是否发送消息")
    @TableField(value = "enable_notification")
    private Boolean enableNotification;

    @Schema(description = "标题模板")
    @TableField(value = "title_template")
    private String titleTemplate;

    @Schema(description = "内容模板")
    @TableField(value = "content_template")
    private String contentTemplate;

    @Schema(description = "是否启用")
    @TableField(value = "is_enabled")
    @Index(name = "idx_enabled")
    private Boolean isEnabled;

    @Schema(description = "优先级")
    @TableField(value = "priority")
    private Integer priority;

    @TableField(exist = false)
    @Schema(description = "接收人列表")
    private java.util.List<AlarmStrategyReceiver> receivers;
}
