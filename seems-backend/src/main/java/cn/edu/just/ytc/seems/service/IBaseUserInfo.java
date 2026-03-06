package cn.edu.just.ytc.seems.service;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.pojo.dto.UserRole;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.utils.UserHolder;

import java.math.BigInteger;

public interface IBaseUserInfo {

    default Boolean isLogin() {
        return UserHolder.getUser() != null;
    }

    default User getUser() {
        User user = UserHolder.getUser();
        if (user != null) {
            return user;
        }
        throw new ServerException("用户未登录");
    }

    default BigInteger getUserId() {
        if (UserHolder.getUser() != null) {
            return UserHolder.getUser().getId();
        }
        return null;
    }

}
