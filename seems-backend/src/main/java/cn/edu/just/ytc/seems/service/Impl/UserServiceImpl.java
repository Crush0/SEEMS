package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.dto.LoginForm;
import cn.edu.just.ytc.seems.pojo.dto.RegisterFrom;
import cn.edu.just.ytc.seems.pojo.dto.UserDTO;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.UserService;
import cn.edu.just.ytc.seems.utils.MyDigestUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl extends BaseService implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public UserDTO login(LoginForm loginForm) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", loginForm.getUsername()));
        if (user == null) {
            throw new ServerException("用户名不存在");
        }
        if(!MyDigestUtils.matches(loginForm.getPassword(), user.getPassword())) {
            throw new ServerException("密码错误");
        }
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        UserShipRole.Role role = userShipRole == null? UserShipRole.Role.USER : userShipRole.getRole();
        UserShipRole.Status status =  userShipRole == null? UserShipRole.Status.DISABLED : userShipRole.getStatus();
        return UserDTO.of(user, role, status);
    }

    @Override
    public void register(RegisterFrom registerFrom) {
        if (registerFrom.getPassword().equals(registerFrom.getRepeatPassword())){
            if (userMapper.selectByUsername(registerFrom.getUsername()) != null) {
                throw new ServerException("用户名已存在");
            }
            User user = new User();
            user.setUsername(registerFrom.getUsername());
            String encryptedPassword = MyDigestUtils.strEncrypt(registerFrom.getPassword());
            user.setPassword(encryptedPassword);
            user.setCreateDate(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            user.setUpdateDate(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            userMapper.insert(user);
        } else {
            throw new ServerException("两次密码输入不一致");
        }
    }

    @Override
    public UserDTO getUserInfo() {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        UserShipRole.Role role = userShipRole == null? UserShipRole.Role.USER : userShipRole.getRole();
        UserShipRole.Status status =  userShipRole == null? UserShipRole.Status.WAITJOIN : userShipRole.getStatus();
        return UserDTO.of(user, role, status);
    }

    @Override
    public void resetPassword(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new ServerException("用户名不存在");
        }
        String newPassword = MyDigestUtils.strEncrypt("123456");
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(String base64) {
        User user = getUser();
        user.setAvatar(base64);
        userMapper.updateById(user);
    }

    @Override
    public void updateBasicInfo(String email, String phone) {
        User user = getUser();
        if (email!= null) {
            user.setEmail(email);
        }
        if (phone!= null) {
            user.setPhone(phone);
        }
        userMapper.updateById(user);
    }

    @Override
    public void editPassword(String oldPassword, String newPassword, String repeatPassword) {
        User user = getUser();
        if (!MyDigestUtils.matches(oldPassword, user.getPassword())) {
            throw new ServerException("旧密码错误");
        }
        if (!newPassword.equals(repeatPassword)) {
            throw new ServerException("两次密码输入不一致");
        }
        String encryptedPassword = MyDigestUtils.strEncrypt(newPassword);
        user.setPassword(encryptedPassword);
        userMapper.updateById(user);
    }
}
