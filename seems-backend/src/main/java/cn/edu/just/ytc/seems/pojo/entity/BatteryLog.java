package cn.edu.just.ytc.seems.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Table(value = "battery_log")
@AllArgsConstructor
@NoArgsConstructor
public class BatteryLog extends BaseEntity{
    @JsonIgnore
    @Schema(description = "船舶ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;
    @Schema(description = "时间")
    @Index
    private Date time;
    @Schema(description = "航次ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger voyageId;
    @Schema(description = "电量")
    private BigDecimal soc;
    @Schema(description = "电压")
    private BigDecimal voltage;
    @Schema(description = "电流")
    private BigDecimal electricity;
    @Schema(description = "电功率")
    private BigDecimal power;
    @Schema(description = "环境温度")
    private BigDecimal temperature;
    @Schema(description = "电池位置 (l_0, l_1, ...)")
    private String position = "unknown";
    @Schema(description = "是否报警")
    private boolean isAlarm = false;

    @Index
    @Schema(description = "链接ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger linkId;
}
