package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.GPSLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GPSLogMapper extends BaseMapper<GPSLog> {
    @Select("SELECT * FROM gps_log WHERE ship_id = #{shipId} ORDER BY time DESC LIMIT 1")
    GPSLog selectLeastGPSLogByShipId(@Param("shipId") BigInteger shipId);


    @Select("SELECT * FROM gps_log WHERE ship_id=#{shipId} and gps_log.time >= (" +
            "  SELECT DATE_FORMAT(MAX(time),\"%Y-%m-%d\")" +
            "  FROM gps_log" +
            "  WHERE time <= CURDATE() AND ship_id = #{shipId}" +
            ")" +
            "ORDER BY time;")
    List<GPSLog> getNearlyShipGPSLogByShipId(BigInteger shipId);

    @Select("SELECT * FROM gps_log WHERE ship_id=#{shipId} and gps_log.time >= #{startTime} and gps_log.time <= #{endTime} ORDER BY time;")
    List<GPSLog> getShipGPSLogByShipIdAndDateTimeBetween(BigInteger shipId, Timestamp startTime, Timestamp endTime);

    @Select("SELECT * FROM gps_log WHERE ship_id=#{shipId} and voyage_id = #{voyageId} ORDER BY time;")
    List<GPSLog> getShipGPSLogByShipIdAndVoyageId(BigInteger shipId, BigInteger voyageId);

    @Select("SELECT MAX(time) FROM gps_log WHERE time <= CURDATE() and ship_id = #{shipId};")
    LocalDateTime getNearlyShipGPSLogDate(BigInteger shipId);

}
