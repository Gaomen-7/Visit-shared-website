package com.travelshare.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 上传成功返回的可直接引用 URL（相对路径，如 {@code /uploads/xxx.png}）。
 */
@Data
@AllArgsConstructor
public class FileUploadVO {

    private String url;
}
