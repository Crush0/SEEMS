package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.PropellerData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

@Mapper
public interface PropellerMapper extends BaseMapper<PropellerData> {
    @Select("SELECT * FROM propeller_data pl " +
            "WHERE pl.position = 'right' " +
            "AND pl.ship_id = #{shipId} AND pl.time = (SELECT pl2.time FROM propeller_data pl2 WHERE pl2.position = 'right' ORDER BY pl2.time DESC LIMIT 1) LIMIT 1")
    PropellerData getLatestRightPropeller(BigInteger shipId);
    @Select("SELECT * FROM propeller_data pl " +
            "WHERE pl.position = 'left' " +
            "AND pl.ship_id = #{shipId} AND pl.time = (SELECT pl2.time FROM propeller_data pl2 WHERE pl2.position = 'left' ORDER BY pl2.time DESC LIMIT 1) LIMIT 1")
    PropellerData getLatestLeftPropeller(BigInteger shipId);
}
