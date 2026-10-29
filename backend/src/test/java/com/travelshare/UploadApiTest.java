package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

class UploadApiTest extends IntegrationTestBase {

    private static final Path TEST_UPLOAD_DIR = Paths.get("target", "test-uploads").toAbsolutePath();

    @Test
    void anonymousUploadIsRejectedWith401() throws Exception {
        assertErrorEnvelope(upload(jpegBytes(), "a.jpg", "image/jpeg", null), 401);
    }

    @Test
    void jpgPngWebpUploadsSucceedAndUrlIsReadable() throws Exception {
        String token = registerAndLogin("uploaduser", "123456");

        for (String[] each : new String[][] {
                { "photo.jpg", "image/jpeg", "jpg" },
                { "photo.png", "image/png", "png" },
                { "photo.webp", "image/webp", "webp" } }) {
            byte[] content = switch (each[2]) {
                case "png" -> pngBytes();
                case "webp" -> webpBytes();
                default -> jpegBytes();
            };
            ResponseEntity<String> upload = upload(content, each[0], each[1], token);
            assertThat(upload.getStatusCodeValue()).isEqualTo(200);
            String url = body(upload).path("data").path("url").asText();
            assertThat(url).startsWith("/uploads/").endsWith("." + each[2]);

            ResponseEntity<byte[]> image = rest.exchange(url, HttpMethod.GET, null, byte[].class);
            assertThat(image.getStatusCodeValue()).isEqualTo(200);
            assertThat(image.getBody()).isNotEmpty();
        }
    }

    @Test
    void oversizedFileIsRejectedWith400() throws Exception {
        String token = registerAndLogin("bigfileuser", "123456");
        byte[] huge = new byte[5 * 1024 * 1024 + 1];
        huge[0] = (byte) 0xFF;
        huge[1] = (byte) 0xD8;
        huge[2] = (byte) 0xFF;

        assertErrorEnvelope(upload(huge, "huge.jpg", "image/jpeg", token), 400);
    }

    @Test
    void renamedTextFileIsRejectedEvenWithImageExtensionAndContentType() throws Exception {
        String token = registerAndLogin("fakeimageuser", "123456");

        ResponseEntity<String> renamed = upload(
                "this is not an image".getBytes(StandardCharsets.UTF_8), "fake.jpg", "image/jpeg", token);
        assertErrorEnvelope(renamed, 400);
        assertThat(body(renamed).path("message").asText()).contains("不是有效的图片");
    }

    @Test
    void nonImageExtensionIsRejected() throws Exception {
        String token = registerAndLogin("txtuser", "123456");
        assertErrorEnvelope(upload("hello".getBytes(StandardCharsets.UTF_8), "note.txt", "text/plain", token), 400);
    }

    @Test
    void missingFilePartIsRejectedWith400() throws Exception {
        String token = registerAndLogin("nofileuser", "123456");

        MultiValueMap<String, Object> emptyForm = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        assertErrorEnvelope(rest.exchange("/api/file/upload", HttpMethod.POST,
                new HttpEntity<>(emptyForm, headers), String.class), 400);
    }

    @Test
    void twoUploadsOfSameFileGetDifferentNamesAndLandInTestDir() throws Exception {
        String token = registerAndLogin("twiceuser", "123456");

        String first = body(upload(jpegBytes(), "same.jpg", "image/jpeg", token)).path("data").path("url").asText();
        String second = body(upload(jpegBytes(), "same.jpg", "image/jpeg", token)).path("data").path("url").asText();

        assertThat(first).isNotEqualTo(second);
        assertThat(Files.exists(TEST_UPLOAD_DIR.resolve(first.substring("/uploads/".length())))).isTrue();
        assertThat(Files.exists(TEST_UPLOAD_DIR.resolve(second.substring("/uploads/".length())))).isTrue();
    }

    private ResponseEntity<String> upload(byte[] content, String filename, String contentType, String token) {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new HttpEntity<>(resource, partHeaders));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange("/api/file/upload", HttpMethod.POST, new HttpEntity<>(form, headers), String.class);
    }

    private byte[] jpegBytes() {
        byte[] bytes = new byte[64];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        return bytes;
    }

    private byte[] pngBytes() {
        byte[] bytes = new byte[64];
        bytes[0] = (byte) 0x89;
        bytes[1] = 0x50;
        bytes[2] = 0x4E;
        bytes[3] = 0x47;
        bytes[4] = 0x0D;
        bytes[5] = 0x0A;
        bytes[6] = 0x1A;
        bytes[7] = 0x0A;
        return bytes;
    }

    private byte[] webpBytes() {
        byte[] bytes = new byte[64];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, bytes, 8, 4);
        return bytes;
    }
}
