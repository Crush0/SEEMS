package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.enums.TriggerCondition;
import cn.edu.just.ytc.seems.pojo.enums.TriggerTiming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 创建报警策略请求
 */
@Data
@Schema(description = "创建报警策略请求")
public class CreateAlarmStrategyRequest {

    @Schema(description = "船舶ID（NULL表示全局策略）")
    private Long shipId;

    @Schema(description = "报警类型", required = true)
    @NotBlank(message = "报警类型不能为空")
    private String alarmType;

    @Schema(description = "报警级别", required = true)
    @NotBlank(message = "报警级别不能为空")
    private String alarmLevel;

    @Schema(description = "触发条件", required = true)
    @NotNull(message = "触发条件不能为空")
    private TriggerCondition triggerCondition;

    @Schema(description = "阈值1", required = true)
    @NotNull(message = "阈值1不能为空")
    private Double thresholdValue;

    @Schema(description = "阈值2（BETWEEN条件时必填）")
    private Double thresholdValue2;

    @Schema(description = "触发时机", required = true)
    @NotNull(message = "触发时机不能为空")
    private TriggerTiming triggerTiming;

    @Schema(description = "持续时间（秒）")
    @Min(value = 1, message = "持续时间至少为1秒")
    private Integer durationSeconds;

    @Schema(description = "是否发送消息", required = true)
    @NotNull(message = "是否发送消息不能为空")
    private Boolean enableNotification;

    @Schema(description = "标题模板", required = true)
    @NotBlank(message = "标题模板不能为空")
    private String titleTemplate;

    @Schema(description = "内容模板", required = true)
    @NotBlank(message = "内容模板不能为空")
    private String contentTemplate;

    @Schema(description = "优先级（默认0）")
    private Integer priority;

    @Schema(description = "接收人列表", required = true)
    @NotEmpty(message = "接收人列表不能为空")
    private List<ReceiverConfig> receivers;
}
