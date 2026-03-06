package cn.edu.just.ytc.seems.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.mysql.MysqlColumnZerofill;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Table(value = "alert_log")
@AllArgsConstructor
@NoArgsConstructor
public class AlertLog extends BaseEntity{
    @Schema(description = "报警ID")
    @Index
    @MysqlColumnZerofill
    @ColumnType(length = 6)
    private Integer alertId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;

    @Schema(description = "报警时间")
    @Index
    private Date alertTime;

    @Schema(description = "确认时间")
    @Index
    private Date confirmTime;

    @Schema(description = "恢复时间")
    @Index
    private Date recoveryTime;

    @Schema(description = "报警内容")
    private String alertContent;
}
