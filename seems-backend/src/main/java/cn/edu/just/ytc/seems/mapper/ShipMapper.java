package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.ShipBaseInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface ShipMapper extends BaseMapper<ShipBaseInfo> {

    @Select("SELECT * FROM ship_info WHERE ship_origin_number = #{shipNumber} LIMIT 1")
    ShipBaseInfo selectByShipNumber(@Param("shipNumber") String shipNumber);

    @Select("SELECT id FROM ship_info")
    List<BigInteger> selectAllShipId();
}
