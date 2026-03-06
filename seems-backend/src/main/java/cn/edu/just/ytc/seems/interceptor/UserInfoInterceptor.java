package cn.edu.just.ytc.seems.interceptor;

import cn.edu.just.ytc.seems.exception.UserNotLoginException;
import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.utils.JwtUtil;
import cn.edu.just.ytc.seems.utils.UserHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Resource
    private UserMapper userMapper;

    /**
     * 请求执行前执行的，将用户信息放入ThreadLocal
     *
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        try {
            String token = request.getHeader("Authorization");
            assert token!= null;
            String realToken = token.replace("Bearer ", "");
            Jws<Claims> jws = JwtUtil.parseClaim(realToken);
            String username = jws.getPayload().get("username", String.class);
            UserHolder.setUser(userMapper.selectByUsername(username));
        } catch (Exception e) {
            throw new UserNotLoginException("用户未登录", e);
        }
        return true;
    }

    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     *
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        UserHolder.clear();
    }
}

