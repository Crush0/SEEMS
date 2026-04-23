package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 报警日志视图对象
 */
@Data
@Schema(description = "报警日志视图对象")
public class AlarmLogVO {

    @Schema(description = "报警ID")
    private BigInteger id;

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

    @Schema(description = "报警标题")
    private String title;

    @Schema(description = "报警内容")
    private String content;

    @Schema(description = "是否已处理")
    private Boolean isHandled;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人用户名")
    private String handlerUsername;

    @Schema(description = "关联数据ID")
    private Long relatedDataId;

    @Schema(description = "创建时间")
    private LocalDateTime createDate;

    @Schema(description = "更新时间")
    private LocalDateTime updateDate;
}
