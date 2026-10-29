package com.travelshare.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.travelshare.common.BizException;

/**
 * 本模块最小形态的单图上传：只接收 jpg/png/webp，≤5MB，本地磁盘存原图。
 *
 * <p>校验两层：扩展名 + 文件头（magic bytes）。只看后缀或只看客户端声明的内容类型都挡不住改名攻击。
 */
@Service
public class FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path uploadDir;

    public FileService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(400, "图片大小不能超过 5MB");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(400, "仅支持 jpg / png / webp 格式的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BizException(400, "上传的文件不是图片");
        }
        if (!matchesMagic(file, extension)) {
            throw new BizException(400, "文件内容不是有效的图片");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = uploadDir.resolve(filename);
        try {
            Files.createDirectories(uploadDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
        } catch (IOException e) {
            throw new BizException(500, "图片保存失败");
        }
        // 只存相对 URL：换机器 / 换磁盘目录都不会让存量数据失效。
        return "/uploads/" + filename;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean matchesMagic(MultipartFile file, String extension) {
        byte[] header = new byte[12];
        int read = 0;
        try (InputStream in = file.getInputStream()) {
            while (read < header.length) {
                int n = in.read(header, read, header.length - read);
                if (n < 0) {
                    break;
                }
                read += n;
            }
        } catch (IOException e) {
            return false;
        }
        if (read < 4) {
            return false;
        }
        switch (extension) {
            case "jpg":
            case "jpeg":
                return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
            case "png":
                return read >= 8
                        && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                        && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A;
            case "webp":
                return read >= 12 && ascii(header, 0, 4).equals("RIFF") && ascii(header, 8, 4).equals("WEBP");
            default:
                return false;
        }
    }

    private String ascii(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = offset; i < offset + length; i++) {
            sb.append((char) (bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}
