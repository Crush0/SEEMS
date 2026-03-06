package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.NoonReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

@Mapper
public interface NoonReportMapper extends BaseMapper<NoonReport> {

    @Select("SELECT * FROM noon_report WHERE ship_id = #{shipId} ORDER BY time DESC LIMIT 1")
    NoonReport getLastNoonReport(@Param("shipId")BigInteger shipId);

}
