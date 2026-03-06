package cn.edu.just.ytc.seems.pojo.dto;

import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryPersonnelListRequest extends PersonnelRecord{
    private int current;
    private int pageSize;
}
