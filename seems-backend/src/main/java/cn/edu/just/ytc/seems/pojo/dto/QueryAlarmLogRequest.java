package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询报警日志请求
 */
@Data
@Schema(description = "查询报警日志请求")
public class QueryAlarmLogRequest {

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "报警类型")
    private String alarmType;

    @Schema(description = "报警级别")
    private String alarmLevel;

    @Schema(description = "是否已处理")
    private Boolean isHandled;

    @Schema(description = "当前页码", required = true)
    private Integer current = 1;

    @Schema(description = "每页大小", required = true)
    private Integer pageSize = 10;
}
