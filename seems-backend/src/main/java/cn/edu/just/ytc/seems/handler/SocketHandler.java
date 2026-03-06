package cn.edu.just.ytc.seems.handler;


import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.pojo.dto.RealTimeData;
import cn.edu.just.ytc.seems.service.ShipLogService;
import cn.edu.just.ytc.seems.utils.SocketUtil;
import cn.edu.just.ytc.seems.utils.UserHolder;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketHandler{

    @Resource
    private UserMapper userMapper;

    @Resource
    private ShipLogService shipLogService;

//    private Codec<RealTimeData> realTimeDataTypeCodec;

    @PostConstruct
    public void init() {
//        realTimeDataTypeCodec = ProtobufProxy
//                .create(RealTimeData.class);
        log.info("SocketHandler init...");

    }

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String username = UserHolder.getUser().getUsername();
        SocketUtil.connectMap.put(username, client);
//        log.debug("客户端userId: "+ UserHolder.getUser().getUsername()+ "已连接，客户端ID为：" + client.getSessionId());
    }

    @OnEvent("ping")
    public void onPing(AckRequest ack) {
        ack.sendAckData("pong");
    }

    private void sendRealTimeMessage(String username) {
        UserHolder.setUser(userMapper.selectByUsername(username));
        RealTimeData realTimeData = shipLogService.getRealTimeData();
        SocketUtil.sendToOne(username,realTimeData);
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void sendRealTimeMessageTask() {
        SocketUtil.connectMap.forEach((username, client) -> sendRealTimeMessage(username));
    }

}


