package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询报警策略请求
 */
@Data
@Schema(description = "查询报警策略请求")
public class QueryAlarmStrategyRequest {

    @Schema(description = "船舶ID")
    private Long shipId;

    @Schema(description = "报警类型")
    private String alarmType;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "当前页码", required = true)
    private Integer current = 1;

    @Schema(description = "每页大小", required = true)
    private Integer pageSize = 10;
}
