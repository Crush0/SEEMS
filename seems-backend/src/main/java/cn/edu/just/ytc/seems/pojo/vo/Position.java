package cn.edu.just.ytc.seems.pojo.vo;

//import com.baidu.bjf.remoting.protobuf.annotation.Ignore;
//import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@ProtobufClass
public class Position implements Serializable {
    @Serial
//    @Ignore
    private static final long serialVersionUID = 1L;
    // 纬度
    private Double latitude;
    // 经度
    private Double longitude;
}
