package cn.edu.just.ytc.seems.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * 查询报警日志响应
 */
@Data
@Schema(description = "查询报警日志响应")
public class QueryAlarmLogResp {

    @Schema(description = "报警日志列表")
    private List<AlarmLogVO> list;

    @Schema(description = "总数")
    private BigInteger total;
}
