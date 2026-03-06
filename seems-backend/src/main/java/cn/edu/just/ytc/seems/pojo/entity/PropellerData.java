package cn.edu.just.ytc.seems.pojo.entity;

//import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tangzc.autotable.annotation.Index;
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
@Table(value = "propeller_data")
@AllArgsConstructor
@NoArgsConstructor
public class PropellerData extends BaseEntity {
    private Date time;
    public double rpm;
    public double degrees;
    public double power;
    public String position;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public BigInteger shipId;
    public PropellerWorkStatus status;

    @Schema(description = "航次ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger voyageId;

    @Index
    @Schema(description = "链接ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger linkId;

    @Getter
    public enum PropellerWorkStatus {
        RUNNING("RUNNING", "运行中"),
        STOPPED("STOPPED", "停止"),
        ERROR("ERROR", "故障"),
        UNKNOWN("UNKNOWN", "未知");
        @EnumValue
        @JsonValue
        private final String code;
        private final String value;
        PropellerWorkStatus(String code, String value) {
            this.code = code;
            this.value = value;
        }
    }
}
