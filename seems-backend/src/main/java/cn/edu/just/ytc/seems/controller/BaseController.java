package cn.edu.just.ytc.seems.controller;

import cn.edu.just.ytc.seems.exception.ServerException;
import cn.edu.just.ytc.seems.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
@Slf4j
public abstract class BaseController {
    @ExceptionHandler({ServerException.class, AssertionError.class})
    public R exceptionHandler(ServerException e) {
        if (Arrays.asList(new Integer[]{50000}).contains(e.getCode())) {
            log.error(e.getMessage());
        }
        return R.error(e.getCode(), e.getMessage());
    }
}
