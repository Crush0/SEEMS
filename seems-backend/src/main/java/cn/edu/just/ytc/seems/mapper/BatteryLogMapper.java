package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.BatteryLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BatteryLogMapper extends BaseMapper<BatteryLog> {
    @Select("SELECT AVG(bl.soc) FROM battery_log bl " +
            "WHERE bl.position LIKE 'l%' " +
            "AND bl.ship_id = #{shipId} AND bl.time = (SELECT bl2.time FROM battery_log bl2 WHERE bl2.position LIKE 'l%' ORDER BY bl2.time DESC LIMIT 1) LIMIT 1")
    Double getLatestLeftSoc(@Param("shipId") BigInteger shipId);

    @Select("SELECT AVG(bl.soc) FROM battery_log bl " +
            "WHERE bl.position LIKE 'r%' " +
            "AND bl.ship_id = #{shipId} AND bl.time = (SELECT bl2.time FROM battery_log bl2 WHERE bl2.position LIKE 'r%' ORDER BY bl2.time DESC LIMIT 1) LIMIT 1")
    Double getLatestRightSoc(@Param("shipId") BigInteger shipId);

    @Select("""
            SELECT
                AVG(soc) AS soc,
                NULL AS voltage,
                NULL AS electricity,
                NULL AS power,
                NULL AS temperature,
                NULL AS position,
		        time
            FROM
                battery_log bl
            WHERE
                ship_id = #{shipId}
                AND time BETWEEN #{start} AND #{end}
            GROUP BY
        time ORDER BY time;
""")
    List<BatteryLog> findAvgBatteryLogByShipIdBetween(@Param("shipId") BigInteger shipId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
