package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'adminOne' for key 'idx_username'
        //获取异常信息
        String message=ex.getMessage();
        //异常信息包含 Duplicate entry 说明有重复的值
        if(message.contains("Duplicate entry")){
            //截取重复的key
            String[] split = message.split(" ");
            String username = split[2];
            //返回给前端信息 adminOne已存在
            String msg=username+ MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else {
            //其他SQL异常,返回未知错误
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
