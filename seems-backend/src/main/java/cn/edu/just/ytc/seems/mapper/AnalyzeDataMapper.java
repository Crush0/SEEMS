package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.AnalyzeData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface AnalyzeDataMapper extends BaseMapper<AnalyzeData> {
    @Select("""
            -- 创建查询，获取指定时间段内指定 ship_id 的每小时最新记录
                                                                     SELECT *
                                                                     FROM analyze_data AS ad
                                                                     WHERE ad.ship_id = #{ship_id} -- 替换为具体的 ship_id
                                                                     AND ad.analyze_time BETWEEN #{start} AND #{end}
                                                                     AND ad.analyze_time = (
                                                                         SELECT MAX(ad_inner.analyze_time)
                                                                         FROM analyze_data AS ad_inner
                                                                         WHERE ad_inner.ship_id = ad.ship_id
                                                                         AND DATE_FORMAT(ad_inner.analyze_time, '%Y-%m-%d %H:00:00') = DATE_FORMAT(ad.analyze_time, '%Y-%m-%d %H:00:00')
                                                                     )
                                                                     ORDER BY ad.analyze_time;
            """)
    List<AnalyzeData> getNewestData(@Param("ship_id")BigInteger shipId, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Select("""
            -- 创建查询，获取指定 ship_id 的最后一小时记录
                                                                    SELECT *
                                                                                              FROM analyze_data
                                                                                              WHERE ship_id = #{ship_id} -- 替换 ? 为具体的船只ID
                                                                                              AND analyze_time = (
                                                                                                  SELECT MAX(analyze_time)
                                                                                                  FROM analyze_data
                                                                                                  WHERE ship_id = #{ship_id} -- 替换 ? 为具体的船只ID
                                                                                                   AND analyze_time < DATE_FORMAT(NOW(),"%Y-%m-%d %H:00:00") -- 最近一小时内的记录
                                                                                              ) LIMIT 1;
""")
    AnalyzeData getLastHourData(@Param("ship_id")BigInteger shipId);

    @Select("""
            -- 创建查询，获取指定 ship_id 的最后一小时记录
                                                                    SELECT *
                                                                                              FROM analyze_data
                                                                                              WHERE ship_id = #{ship_id} -- 替换 ? 为具体的船只ID
                                                                                              AND analyze_time = (
                                                                                                  SELECT MAX(analyze_time)
                                                                                                  FROM analyze_data
                                                                                                  WHERE ship_id = #{ship_id} -- 替换 ? 为具体的船只ID
                                                                                                   AND analyze_time < DATE_FORMAT(#{date},"%Y-%m-%d %H:00:00") -- 最近一小时内的记录
                                                                                              ) LIMIT 1;
""")
    AnalyzeData getLastHourDataByDate(@Param("ship_id")BigInteger shipId, @Param("date") Timestamp date);
}
