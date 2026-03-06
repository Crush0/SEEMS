package cn.edu.just.ytc.seems;

import com.tangzc.autotable.springboot.EnableAutoTable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@Slf4j
@EnableScheduling
@EnableAutoTable
@MapperScan("cn.edu.just.ytc.seems.mapper")
@SpringBootApplication
public class SeemsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeemsApplication.class, args);
    }
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }


}
