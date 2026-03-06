package cn.edu.just.ytc.seems.pojo.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class QueryPersonnelListResp {
    private BigInteger total;
    private List<UserRole> list;
}
