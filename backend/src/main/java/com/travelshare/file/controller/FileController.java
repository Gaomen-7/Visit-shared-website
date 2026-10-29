package com.travelshare.file.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.travelshare.common.Result;
import com.travelshare.file.service.FileService;
import com.travelshare.file.vo.FileUploadVO;

/**
 * 文件上传（登录档，multipart 单文件）。后续攻略封面 / 目的地图片 / 轮播图直接复用。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(new FileUploadVO(fileService.upload(file)));
    }
}
