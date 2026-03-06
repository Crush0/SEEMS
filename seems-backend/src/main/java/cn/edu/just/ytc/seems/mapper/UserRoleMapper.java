package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.dto.UserRole;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.pojo.vo.Position;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserShipRole> {
    @Select("SELECT * FROM user_ship_role WHERE user_id = #{id} LIMIT 1")
    UserShipRole getUserShipRoleByUserId(BigInteger id);

//    @Results(id="UserRole", value = {
//            @Result(property = "id",column = "ship_id"),
//            @Result(property = "shipSpeed", column = "ship_speed"),
//            @Result(property = "leftBatteryCapacity", column = "left_battery_capacity"),
//            @Result(property = "rightBatteryCapacity", column = "right_battery_capacity"),
//            @Result(property = "position", column = "position", javaType = Position.class, typeHandler = JacksonTypeHandler.class ),
//            @Result(property = "workStatus", column = "work_status"),
//            @Result(property = "logTime", column = "log_time")
//    })
    @Select("SELECT u.id, u.username as name, u.email, u.phone, ur.role, ur.status, ur.create_date as create_time FROM user_ship_role ur LEFT JOIN user u on ur.user_id = u.id" +
            " ${ew.customSqlSegment}")
    IPage<UserRole> getUserRolesByUserId(Page<UserRole> page, @Param(Constants.WRAPPER) QueryWrapper<UserRole> queryWrapper);
}
