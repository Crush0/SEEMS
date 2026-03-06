package cn.edu.just.ytc.seems.annotation;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface HasPermission {
    UserShipRole.Role role() default UserShipRole.Role.USER;
}
