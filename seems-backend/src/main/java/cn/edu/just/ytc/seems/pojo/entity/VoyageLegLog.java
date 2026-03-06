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

/**
 * @description: 航段实体类
 */
@Setter
@Getter
@Table(value = "voyage_leg_log")
@AllArgsConstructor
@NoArgsConstructor
public class VoyageLegLog extends BaseEntity {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger shipId;
    @Schema(description = "航次ID")
    private BigInteger voyageId;
    @Schema(description = "添加时间")
    @Index
    private LocalDateTime addTime;
    @Schema(description = "下个目标")
    private String nextGoal;
}
