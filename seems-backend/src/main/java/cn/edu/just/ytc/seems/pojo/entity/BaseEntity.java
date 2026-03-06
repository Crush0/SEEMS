package cn.edu.just.ytc.seems.pojo.entity;

//import com.baidu.bjf.remoting.protobuf.annotation.Ignore;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.mpe.annotation.InsertFillTime;
import com.tangzc.mpe.annotation.UpdateFillTime;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Schema(description = "基础实体类")
public abstract class BaseEntity implements Serializable {
    @Serial
//    @Ignore
    private static final long serialVersionUID = 1L;

//    @Ignore
    @Schema(description = "主键ID")
    @ColumnId(value = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    protected BigInteger id;

//    @Ignore
    @Schema(description = "创建时间")
    @JsonIgnore
    @InsertFillTime
    @TableField(fill = FieldFill.INSERT)
    @Index
    protected Date createDate;

//    @Ignore
    @Schema(description = "更新时间")
    @JsonIgnore
    @UpdateFillTime
    @TableField(fill = FieldFill.UPDATE)
    protected Date updateDate;

//    @Ignore
    @Schema(description = "是否删除")
    @JsonIgnore
    @TableLogic(value = "0", delval = "1")
    protected boolean isDeleted = false;
}
