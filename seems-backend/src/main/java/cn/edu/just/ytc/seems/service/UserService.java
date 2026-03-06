package cn.edu.just.ytc.seems.service;


import cn.edu.just.ytc.seems.pojo.dto.LoginForm;
import cn.edu.just.ytc.seems.pojo.dto.RegisterFrom;
import cn.edu.just.ytc.seems.pojo.dto.UserDTO;

public interface UserService extends IBaseUserInfo{
    UserDTO login(LoginForm loginForm);
    void register(RegisterFrom registerFrom);
    UserDTO getUserInfo();

    void resetPassword(String username);

    void updateAvatar(String base64);

    void updateBasicInfo(String email, String phone);

    void editPassword(String oldPassword, String newPassword, String repeatPassword);
}
