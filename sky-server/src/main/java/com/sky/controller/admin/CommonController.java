package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}",file);

        try {
            //1.获得原始文件名
            String originalFilename = file.getOriginalFilename();

            //2.截取后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            //3.构造新文件名(UUID 防止有重名文件覆盖)
            String objectName = UUID.randomUUID().toString() + extension;

            //4.调用工具类方法 文件上传 并获得文件路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            //5.上传成功
            return Result.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败:{}",e);
        }

        //上传失败
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
