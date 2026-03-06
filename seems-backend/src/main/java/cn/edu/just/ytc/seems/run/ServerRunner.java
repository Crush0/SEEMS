package cn.edu.just.ytc.seems.run;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerRunner implements CommandLineRunner {

    private final SocketIOServer socketIOServer;

    @Lazy
    public ServerRunner(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    /**
     *  项目启动时，自动启动 socket 服务，服务端开始工作
     **/
    @Override
    public void run(String... args)  {
        socketIOServer.start();
        log.info("socket.io server started !");
    }
}


