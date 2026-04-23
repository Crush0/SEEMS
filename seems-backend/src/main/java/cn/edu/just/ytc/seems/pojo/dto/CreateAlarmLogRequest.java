package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建报警日志请求
 */
@Data
@Schema(description = "创建报警日志请求")
public class CreateAlarmLogRequest {

    @Schema(description = "船舶ID", required = true)
    private Long shipId;

    @Schema(description = "报警类型", required = true)
    private String alarmType;

    @Schema(description = "报警级别", required = true)
    private String alarmLevel;

    @Schema(description = "报警标题", required = true)
    private String title;

    @Schema(description = "报警内容", required = true)
    private String content;

    @Schema(description = "关联数据ID")
    private Long relatedDataId;
}
