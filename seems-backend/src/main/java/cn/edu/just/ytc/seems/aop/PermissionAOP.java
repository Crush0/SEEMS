package cn.edu.just.ytc.seems.aop;


import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.IBaseUserInfo;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAOP implements IBaseUserInfo {
    @Resource
    private UserRoleMapper userRoleMapper;

    private UserShipRole getUserShipRole() {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole == null) {
            throw new ServerException(50403, "用户没有船舶权限");
        }
        return userShipRole;
    }


    @Pointcut("@annotation(cn.edu.just.ytc.seems.annotation.HasPermission)")
    public void permissionPointCut() {

    }

    @Before("permissionPointCut() && @annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) {
        UserShipRole.Role role = hasPermission.role();
        if (isLogin()) {
            UserShipRole userShipRole = getUserShipRole();
            if (!userShipRole.getRole().moreThan(role) || userShipRole.getStatus() != UserShipRole.Status.NORMAL) {
                throw new ServerException(50403, "没有权限访问！");
            }
        } else {
            throw new ServerException(50401, "请先登录！");
        }
    }
}
