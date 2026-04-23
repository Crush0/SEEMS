package cn.edu.just.ytc.seems.pojo.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * 查询消息列表响应DTO
 */
@Data
public class QueryMessageListResp {
    private BigInteger total;
    private List<MessageVO> list;
}
