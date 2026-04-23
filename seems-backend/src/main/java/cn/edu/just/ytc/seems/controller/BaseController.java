package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.UserService;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

import static cn.edu.just.ytc.seems.utils.UserHolder.getUser;

@ControllerAdvice
@Slf4j
public abstract class BaseController {

    @Resource
    private UserRoleMapper userRoleMapper;

    @ExceptionHandler({ServerException.class, AssertionError.class})
    public R exceptionHandler(ServerException e) {
        if (Arrays.asList(new Integer[]{50000}).contains(e.getCode())) {
            log.error(e.getMessage());
        }
        return R.error(e.getCode(), e.getMessage());
    }

    public UserShipRole getUserShipRole() {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole == null) {
            throw new ServerException("用户没有船舶权限");
        }
        return userShipRole;
    }
}
