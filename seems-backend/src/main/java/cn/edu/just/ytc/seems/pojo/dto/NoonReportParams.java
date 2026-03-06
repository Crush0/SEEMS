package cn.edu.just.ytc.seems.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoonReportParams implements Serializable {
    private Integer current;
    private Integer pageSize;
    private Date[] createdTime;
}
