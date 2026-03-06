package cn.edu.just.ytc.seems.pojo.dto;


import cn.edu.just.ytc.seems.pojo.entity.GPSLog;
import cn.edu.just.ytc.seems.pojo.entity.PropellerData;
import cn.edu.just.ytc.seems.pojo.vo.Position;
//import com.baidu.bjf.remoting.protobuf.annotation.Ignore;
//import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@ProtobufClass
public class RealTimeData implements Serializable {
//    @Ignore
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "当前时间戳")
    private Date time;
    private Double direction;
    private Position position;
    private Double speed;
    private Double leftBatteryCapacity;
    private Double rightBatteryCapacity;
    private PropellerData leftPropeller;
    private PropellerData rightPropeller;
    private Double sailRange;
    private Double sailDuration;
    private Double powerDissipation;
    private List<Boolean> leftBatteryAlarm;
    private List<Boolean> rightBatteryAlarm;
    private Double totalSailRange;
    private Double windSpeed;
    private Double windDirection;
    private GPSLog.WorkStatus workStatus;
}
