package cn.edu.just.ytc.seems.utils;

import cn.edu.just.ytc.seems.pojo.entity.User;

// 利用LocalThread变量保存用户信息
public class UserHolder {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();
    public static User getUser() {
        return userHolder.get();
    }
    public static void setUser(User user) {
        userHolder.set(user);
    }
    public static void clear() {
        userHolder.remove();
    }
}
