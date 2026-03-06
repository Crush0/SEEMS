package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.annotation.HasPermission;
import cn.edu.just.ytc.seems.pojo.dto.LoginForm;
import cn.edu.just.ytc.seems.pojo.dto.RegisterFrom;
import cn.edu.just.ytc.seems.pojo.dto.UserDTO;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.UserService;
import cn.edu.just.ytc.seems.utils.JwtUtil;
import cn.edu.just.ytc.seems.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Resource
    private UserService userService;

    /**
     * 登录 API
     * @param loginForm 登录表单数据
     * @return 登录成功返回用户信息和 token
     */
    @PostMapping("/login")
    public R login(@RequestBody LoginForm loginForm) {
        UserDTO userDTO = userService.login(loginForm);
        return R.ok(new HashMap<String, Object>(){{
            put("user", userDTO);
            put("token", JwtUtil.genAccessToken(userDTO.getUsername(), loginForm.isRememberMe()));
        }});
    }

    @PostMapping("/register")
    public R register(@RequestBody RegisterFrom registerFrom) {
        userService.register(registerFrom);
        return R.ok();
    }

    @PostMapping("/reset-password")
    @HasPermission(role = UserShipRole.Role.ADMIN)
    public R resetPassword(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        if (username == null || username.isEmpty()) {
            return R.error("用户名不能为空");
        }
        userService.resetPassword(username);
        return R.ok();
    }

    /**
     * 获取用户信息 API
     * @return 用户信息
     */
    @PostMapping("/info")
    public R info() {
        return R.ok(new HashMap<String, Object>(){{
            put("user", userService.getUserInfo());
        }});
    }

    @PostMapping("/update-avatar")
    public R updateAvatar(@RequestBody Map<String, String> map) {
        String base64 = map.get("avatar");
        if (base64 == null || base64.isEmpty()) {
            return R.error("头像不能为空");
        }
        userService.updateAvatar(base64);
        return R.ok();
    }

    @PostMapping("/update-basic-info")
    public R updateBasicInfo(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        String phone = map.get("phone");
        userService.updateBasicInfo(email, phone);
        return R.ok();
    }

    @PostMapping("/edit-password")
    public R editPassword(@RequestBody Map<String, String> map) {
        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");
        String repeatPassword = map.get("repeatPassword");
        assert oldPassword!= null && !oldPassword.isEmpty();
        assert newPassword!= null && !newPassword.isEmpty();
        assert repeatPassword!= null && !repeatPassword.isEmpty();
        userService.editPassword(oldPassword, newPassword, repeatPassword);
        return R.ok();
    }

    @PostMapping("/logout")
    public R logout() {
        return R.ok();
    }
}
