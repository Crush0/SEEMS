package cn.edu.just.ytc.seems.analyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class SpeedStats {
    @JsonIgnore
    double totalSpeed = 0.0;
    double maxSpeed = Double.MIN_VALUE;
    @JsonIgnore
    public int count = 0;
    double avgSpeed = 0.0;

    public void update(double speed) {
        totalSpeed += speed;
        if (speed > maxSpeed) {
            maxSpeed = speed;
        }
        count++;
    }

    public void getAverageSpeed() {
        avgSpeed = (count == 0 ? 0.0 : totalSpeed / count);
    }
}
