package cn.edu.just.ytc.seems.run;

import cn.edu.just.ytc.seems.executor.ScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutorRunner implements CommandLineRunner {
    private final ScriptExecutor scriptExecutor;

    @Lazy
    public ExecutorRunner(ScriptExecutor scriptExecutor) {
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public void run(String... args) {
        log.info("Starting ExecutorRunner...");
        scriptExecutor.execute();
    }
}
