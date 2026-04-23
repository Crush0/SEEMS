package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.entity.AlarmStrategyReceiver;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 报警策略接收人Mapper接口
 */
@Mapper
public interface AlarmStrategyReceiverMapper extends BaseMapper<AlarmStrategyReceiver> {

    /**
     * 根据策略ID查询接收人列表
     *
     * @param strategyId 策略ID
     * @return 接收人列表
     */
    @Select("SELECT * FROM alarm_strategy_receiver WHERE strategy_id = #{strategyId} AND is_deleted = 0")
    List<AlarmStrategyReceiver> selectByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 根据策略ID删除接收人
     *
     * @param strategyId 策略ID
     * @return 删除数量
     */
    @Delete("DELETE FROM alarm_strategy_receiver WHERE strategy_id = #{strategyId} AND is_deleted = 0")
    int deleteByStrategyId(@Param("strategyId") Long strategyId);
}
