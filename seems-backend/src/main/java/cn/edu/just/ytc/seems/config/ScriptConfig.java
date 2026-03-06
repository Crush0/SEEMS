package cn.edu.just.ytc.seems.config;

import cn.edu.just.ytc.seems.executor.ScriptExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptConfig {
    @Value("${script.enable}")
    private boolean enable;

    @Value("${script.script-path}")
    private String scriptPath;

    @Value("${script.python-home}")
    private String pythonHome;

    @Value("${script.print-log}")
    private boolean printLog;

    @Value("${script.args}")
    private String args = "";

    @Bean
    public ScriptExecutor scriptExecutor() {
        String[] argsArray = args.split(" ");
        return new ScriptExecutor(enable, scriptPath, pythonHome, printLog, argsArray);
    }


}
