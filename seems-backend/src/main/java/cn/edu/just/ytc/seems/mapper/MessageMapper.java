package cn.edu.just.ytc.seems.mapper;

import cn.edu.just.ytc.seems.pojo.dto.MessageVO;
import cn.edu.just.ytc.seems.pojo.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询消息列表（包含发送者信息）- 使用分页
     *
     * @param page         分页对象
     * @param receiverId   接收者用户ID
     * @return 消息视图对象分页结果
     */
    @Select("SELECT m.id, m.sender_id, m.title, m.content, m.type, m.status, m.create_date, m.role, " +
            "u.username as sender_username, u.avatar as sender_avatar, " +
            "CASE WHEN m.receiver_id IS NULL THEN 1 ELSE 0 END as is_role_message " +
            "FROM message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE m.receiver_id = #{receiverId} " +
            "ORDER BY m.create_date DESC")
    IPage<MessageVO> selectMessageListWithSender(
            Page<MessageVO> page,
            @Param("receiverId") BigInteger receiverId
    );

    /**
     * 统计未读消息数量
     *
     * @param receiverId 接收者用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{receiverId} AND status = 0")
    Integer countUnreadMessages(@Param("receiverId") BigInteger receiverId);
}
