package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import cn.edu.just.ytc.seems.pojo.entity.GPSLog;
import cn.edu.just.ytc.seems.pojo.entity.PropellerData;
import lombok.Data;

@Data
public class RealtimeRedisMsg {
    private GPSLog gps_log;
    private BatteryLog left_battery_log;
    private BatteryLog right_battery_log;
    private PropellerData left_propeller_data;
    private PropellerData right_propeller_data;
}
