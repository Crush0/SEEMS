package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.enums.ReceiverType;
import cn.edu.just.ytc.seems.pojo.enums.TriggerCondition;
import cn.edu.just.ytc.seems.pojo.enums.TriggerTiming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 报警策略视图对象
 */
@Data
@Schema(description = "报警策略视图对象")
public class AlarmStrategyVO {

    @Schema(description = "策略ID")
    private Long id;

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "船舶名称")
    private String shipName;

    @Schema(description = "报警类型")
    private String alarmType;

    @Schema(description = "报警类型名称")
    private String alarmTypeName;

    @Schema(description = "报警级别")
    private String alarmLevel;

    @Schema(description = "报警级别名称")
    private String alarmLevelName;

    @Schema(description = "触发条件")
    private TriggerCondition triggerCondition;

    @Schema(description = "阈值1")
    private Double thresholdValue;

    @Schema(description = "阈值2")
    private Double thresholdValue2;

    @Schema(description = "触发时机")
    private TriggerTiming triggerTiming;

    @Schema(description = "持续时间（秒）")
    private Integer durationSeconds;

    @Schema(description = "是否发送消息")
    private Boolean enableNotification;

    @Schema(description = "标题模板")
    private String titleTemplate;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建时间")
    private Date createDate;

    @Schema(description = "更新时间")
    private Date updateDate;

    @Schema(description = "接收人列表")
    private List<ReceiverInfo> receivers;

    /**
     * 接收人信息
     */
    @Data
    @Schema(description = "接收人信息")
    public static class ReceiverInfo {

        @Schema(description = "接收人ID")
        private Long id;

        @Schema(description = "接收人类型")
        private ReceiverType receiverType;

        @Schema(description = "接收人ID")
        private Long receiverId;

        @Schema(description = "接收人角色")
        private String receiverRole;

        @Schema(description = "接收人名称")
        private String receiverName;
    }
}
