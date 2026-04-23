package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.AlarmLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报警日志Mapper接口
 */
@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {

    /**
     * 查询未处理的报警数量
     *
     * @param shipId 船舶ID
     * @return 未处理报警数量
     */
    @Select("SELECT COUNT(*) FROM alarm_log WHERE ship_id = #{shipId} AND is_handled = 0 AND is_deleted = 0")
    Integer countUnhandledAlarms(@Param("shipId") Long shipId);

    /**
     * 查询指定时间范围内的报警
     *
     * @param shipId 船舶ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 报警列表
     */
    @Select("SELECT * FROM alarm_log WHERE ship_id = #{shipId} " +
            "AND create_date BETWEEN #{startTime} AND #{endTime} " +
            "AND is_deleted = 0 ORDER BY create_date DESC")
    List<AlarmLog> selectAlarmsByTimeRange(
            @Param("shipId") Long shipId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近的严重报警
     *
     * @param shipId 船舶ID
     * @param limit 数量限制
     * @return 严重报警列表
     */
    @Select("SELECT * FROM alarm_log WHERE ship_id = #{shipId} " +
            "AND alarm_level = 'critical' AND is_deleted = 0 " +
            "ORDER BY create_date DESC LIMIT #{limit}")
    List<AlarmLog> selectRecentCriticalAlarms(
            @Param("shipId") Long shipId,
            @Param("limit") int limit
    );
}
