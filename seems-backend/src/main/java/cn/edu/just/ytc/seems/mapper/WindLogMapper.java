package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.WindLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

@Mapper
public interface WindLogMapper extends BaseMapper<WindLog> {
    @Select("SELECT * FROM wind_log WHERE ship_id = #{shipId} ORDER BY time DESC LIMIT 1")
    WindLog selectLeastWindLogByShipId(BigInteger shipId);
}
