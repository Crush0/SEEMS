package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.mapper.UserMapper;
import cn.edu.just.ytc.seems.mapper.UserRoleMapper;
import cn.edu.just.ytc.seems.pojo.dto.PersonnelRecord;
import cn.edu.just.ytc.seems.pojo.dto.QueryPersonnelListRequest;
import cn.edu.just.ytc.seems.pojo.dto.QueryPersonnelListResp;
import cn.edu.just.ytc.seems.pojo.dto.UserRole;
import cn.edu.just.ytc.seems.pojo.entity.User;
import cn.edu.just.ytc.seems.pojo.entity.UserShipRole;
import cn.edu.just.ytc.seems.service.PersonnelService;
import cn.edu.just.ytc.seems.utils.MyDigestUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Service

@Slf4j
public class PersonnelServiceImpl implements PersonnelService {
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserMapper userMapper;

    private UserShipRole getUserShipRole() {
        User user = getUser();
        UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(user.getId());
        if (userShipRole == null) {
            throw new ServerException("用户没有船舶权限");
        }
        return userShipRole;
    }
    @Override
    public QueryPersonnelListResp queryUserRole(QueryPersonnelListRequest queryRequest) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        User loginUser = getUser();
        UserShipRole loginUserRole = userRoleMapper.getUserShipRoleByUserId(loginUser.getId());
        if (loginUserRole == null) {
            throw new ServerException("登录用户没有角色信息");
        }
        queryWrapper.eq("ship_id", loginUserRole.getShipId());
        if (queryRequest.getName()!= null) {
            User user = userMapper.selectByUsername(queryRequest.getName());
            if (user != null) {
                queryWrapper.eq("user_id", user.getId());
            }
        }
        if (queryRequest.getRole() != null) {
            queryWrapper.eq("role", queryRequest.getRole());
        }
        if (queryRequest.getEmail() != null && StringUtils.hasText(queryRequest.getEmail())) {
            queryWrapper.eq("email", queryRequest.getEmail());
        }
        if (queryRequest.getPhone() != null && StringUtils.hasText(queryRequest.getPhone())) {
            queryWrapper.eq("phone", queryRequest.getPhone());
        }
        if (queryRequest.getStatus() != null) {
            queryWrapper.eq("status", queryRequest.getStatus());
        }
        IPage<UserRole> userRoleIPage = userRoleMapper.getUserRolesByUserId(Page.of(queryRequest.getCurrent(), queryRequest.getPageSize()), queryWrapper);
        List<UserRole> userRoleList = userRoleIPage.getRecords();
        QueryPersonnelListResp queryPersonnelListResp = new QueryPersonnelListResp();
        queryPersonnelListResp.setTotal(BigInteger.valueOf(userRoleIPage.getTotal()));
        queryPersonnelListResp.setList(userRoleList);
        return queryPersonnelListResp;
    }

    @Override
    public void savePersonnel(PersonnelRecord personnelRecord) {
        UserShipRole loginUserRole = getUserShipRole();
        User toEditUser = userMapper.selectByUsername(personnelRecord.getName());
        if (toEditUser == null) {
            if (loginUserRole.getRole() != UserShipRole.Role.ADMIN) {
                throw new ServerException("非管理员用户不能创建用户");
            }
            toEditUser = new User();
            toEditUser.setUsername(personnelRecord.getName());
            toEditUser.setEmail(personnelRecord.getEmail());
            toEditUser.setPhone(personnelRecord.getPhone());
            String encryptedPassword = MyDigestUtils.strEncrypt("123456");
            toEditUser.setPassword(encryptedPassword);
            userMapper.insert(toEditUser);
            toEditUser = userMapper.selectByUsername(toEditUser.getUsername());
            UserShipRole userShipRole = new UserShipRole();
            userShipRole.setShipId(loginUserRole.getShipId());
            userShipRole.setUserId(toEditUser.getId());
            userShipRole.setRole(personnelRecord.getRole());
            userShipRole.setStatus(personnelRecord.getStatus());
            userRoleMapper.insert(userShipRole);
        } else {
            UserShipRole userShipRole = userRoleMapper.getUserShipRoleByUserId(toEditUser.getId());
            if (userShipRole == null) {
                throw new ServerException("用户没有船舶权限");
            }
            if(!Objects.equals(userShipRole.getShipId(), loginUserRole.getShipId())) {
                throw new ServerException("用户不属于当前船舶");
            }
//            if (userShipRole.getRole() == UserShipRole.Role.ADMIN && personnelRecord.getRole() != UserShipRole.Role.ADMIN) {
//                throw new ServerException("管理员的账户状态不能编辑");
//            }
            if (loginUserRole.getRole() != UserShipRole.Role.ADMIN && personnelRecord.getRole() == UserShipRole.Role.ADMIN) {
                throw new ServerException("非管理员的账户状态不能编辑为管理员");
            }
            if (loginUserRole.getRole() != UserShipRole.Role.ADMIN && !Objects.equals(toEditUser.getId(), loginUserRole.getUserId())) {
                throw new ServerException("非管理员的账户只能编辑自己的信息");
            }
            toEditUser.setEmail(personnelRecord.getEmail());
            toEditUser.setPhone(personnelRecord.getPhone());
            if (loginUserRole.getRole() == UserShipRole.Role.ADMIN) {
                if(personnelRecord.getRole() != null){
                    userShipRole.setRole(personnelRecord.getRole());
                }
                if (personnelRecord.getStatus() != null) {
                    userShipRole.setStatus(personnelRecord.getStatus());
                }
            }
            userRoleMapper.updateById(userShipRole);
            userMapper.updateById(toEditUser);
        }

    }
}
