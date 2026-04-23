package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.AlarmStrategy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 报警策略Mapper接口
 */
@Mapper
public interface AlarmStrategyMapper extends BaseMapper<AlarmStrategy> {

    /**
     * 查询船舶的有效策略（优先返回船舶特定策略，其次返回全局策略）
     *
     * @param shipId 船舶ID
     * @param alarmType 报警类型
     * @return 策略列表
     */
    @Select("SELECT * FROM alarm_strategy " +
            "WHERE ((ship_id = #{shipId} AND is_deleted = 0) OR (ship_id IS NULL AND is_deleted = 0)) " +
            "AND alarm_type = #{alarmType} " +
            "AND is_enabled = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY ship_id IS NULL, priority DESC " +
            "LIMIT 1")
    AlarmStrategy selectEffectiveStrategy(@Param("shipId") Long shipId, @Param("alarmType") String alarmType);

    /**
     * 查询所有启用的策略（用于缓存）
     *
     * @return 策略列表
     */
    @Select("SELECT * FROM alarm_strategy " +
            "WHERE is_enabled = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY priority DESC")
    List<AlarmStrategy> selectAllEnabled();
}
