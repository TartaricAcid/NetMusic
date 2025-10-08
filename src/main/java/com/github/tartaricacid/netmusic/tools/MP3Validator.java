package com.github.tartaricacid.netmusic.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MP3Validator {
    /**
     * 通过文件格式标签验证文件是否为MP3文件
     * @param file 要验证的文件
     * @return true如果是MP3文件，false如果不是
     */
    public static boolean isMP3File(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        // MP3文件可能的特征字节
        // MP3文件通常以FF FB, FF F3, FF F2, 49 44 33 (ID3)等开头
        byte[] buffer = new byte[3];

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(buffer);
            if (bytesRead < 3) {
                return false;
            }

            // 检查ID3标签 (ID3v2)
            if (buffer[0] == 0x49 && buffer[1] == 0x44 && buffer[2] == 0x33) {
                return true;
            }

            // 检查MPEG帧头
            if ((buffer[0] & 0xFF) == 0xFF && ((buffer[1] & 0xE0) == 0xE0)) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}