package cn.edu.just.ytc.seems.service.Impl;

import cn.edu.just.ytc.seems.pojo.entity.VoyageLog;
import cn.edu.just.ytc.seems.mapper.VoyageLogMapper;
import cn.edu.just.ytc.seems.service.VoyageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VoyageServiceImpl extends ServiceImpl<VoyageLogMapper, VoyageLog> implements VoyageService {
}