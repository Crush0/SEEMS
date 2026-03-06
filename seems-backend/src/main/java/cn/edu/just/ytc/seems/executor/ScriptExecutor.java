package cn.edu.just.ytc.seems.executor;

import cn.edu.just.ytc.seems.service.AnalyzeService;
import cn.edu.just.ytc.seems.utils.SpringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Slf4j
public class ScriptExecutor {

    private final boolean enable;
    private final String scriptPath;
    private final String pythonHome;
    private final boolean printLog;
    private final String[] args;

    private Thread inputStreamThread;

    private Thread errorStreamThread;

    public void readStream(BufferedReader reader, String streamName)  {
        String line;
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (printLog) {
                log.info("{} - {}",streamName, line);
            }
            if (line.contains("航次结束")) {
                // 使用正则表达式获取航次ID 【ID】
                String regex = "【(\\d+)】";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                BigInteger shipId = null;
                BigInteger voyageId = null;
                if (matcher.find()) {
                    shipId = new BigInteger(matcher.group(1));
                }
                if (matcher.find()) {
                    voyageId = new BigInteger(matcher.group(1));
                }
                if (ObjectUtils.allNotNull(shipId, voyageId)) {
                    AnalyzeService analyzeService = SpringUtils.getBean(AnalyzeService.class);
                    analyzeService.analyzeVoyageEnergyConsumptionByShipIdAndVoyageId(shipId, voyageId, true);
                    analyzeService.analyzeVoyageSailDataByShipIdAndVoyageId(shipId, voyageId, true);
                }
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCharset() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win")? "GBK" : "UTF-8";
    }


    @Async
    public void execute() {
        log.info("Executing enabled: {}" , enable);
        try {
            if (enable) {
                log.info("Executing script: {}", scriptPath);
                Process proc = Runtime.getRuntime().exec(pythonHome + " " + scriptPath, args);
//                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), getCharset()));
//                inputStreamThread = new Thread(() -> {
//                    this.readStream(in, "InputStream");
//                });
//                inputStreamThread.start();

                BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream(), getCharset()));
                errorStreamThread = new Thread(() -> this.readStream(err, "ProcessStream"));
                errorStreamThread.start();
                proc.waitFor();
            } else {
                log.warn("Script execution is disabled");
            }
        } catch (Exception e) {
            log.error("Error executing script: {}", e.getMessage());
        }
    }
}
