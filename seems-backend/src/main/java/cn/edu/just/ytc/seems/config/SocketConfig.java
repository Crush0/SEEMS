package cn.edu.just.ytc.seems.config;

import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.utils.JwtUtil;
import cn.edu.just.ytc.seems.utils.UserHolder;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SocketConfig {

//    @Value("${socketio.host}")
//    private String host;

    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.bossCount}")
    private int bossCount;

    @Value("${socketio.workCount}")
    private int workCount;

    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout}")
    private int pingTimeout;

    @Value("${socketio.pingInterval}")
    private int pingInterval;

    private final UserMapper userMapper;

    @Lazy
    public SocketConfig(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setPort(port);
        com.corundumstudio.socketio.SocketConfig socketConfig=new com.corundumstudio.socketio.SocketConfig();
        socketConfig.setReuseAddress(true);

        configuration.setSocketConfig(socketConfig);
        configuration.setOrigin(":*:");
        configuration.setTransports(Transport.POLLING, Transport.WEBSOCKET);
        configuration.setBossThreads(bossCount);
        configuration.setWorkerThreads(workCount);
        configuration.setAllowCustomRequests(allowCustomRequests);
        configuration.setUpgradeTimeout(upgradeTimeout);
        configuration.setPingTimeout(pingTimeout);
        configuration.setPingInterval(pingInterval);
        configuration.setAuthorizationListener((auth) -> {
            try {
                String token = auth.getSingleUrlParam("token");
                String realToken = token.replace("Bearer ", "");
                Jws<Claims> jws = JwtUtil.parseClaim(realToken);
                String username = jws.getPayload().get("username", String.class);
                User user = userMapper.selectByUsername(username);
                UserHolder.setUser(user);
                return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
            } catch (Exception e) {
//                log.error("Authorization error", e);
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }
        });
        //设置 sessionId 随机
        configuration.setRandomSession(true);
        return new SocketIOServer(configuration);
    }

    /**
     *  Spring加载 SocketIOServer
     **/
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer ) {
        return new SpringAnnotationScanner(socketIOServer);
    }
}


