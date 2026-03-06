package cn.edu.just.ytc.seems.pojo.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.mpe.autotable.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Setter
@Getter
@Table(value = "wind_log")
@AllArgsConstructor
@NoArgsConstructor
public class WindLog extends BaseEntity{
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "船舶ID")
    private BigInteger shipId;
    @Schema(description = "时间")
    @Index
    private LocalDateTime time;
    @Schema(description = "航段ID")
    private BigInteger voyageId;
    @Schema(description = "风向")
    private Double windDirection;
    @Schema(description = "风速")
    private Double windSpeed;
    @Schema(description = "流向")
    private Double waterDirection;
    @Schema(description = "流速")
    private Double waterSpeed;
    @Schema(description = "波高")
    private Double waveHeight;
    @Schema(description = "波向")
    private Double waveDirection;

    @Index
    @Schema(description = "链接ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger linkId;
}
