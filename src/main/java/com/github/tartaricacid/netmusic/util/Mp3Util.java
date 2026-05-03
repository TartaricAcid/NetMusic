package com.github.tartaricacid.netmusic.util;

import java.io.IOException;
import java.io.InputStream;

public class Mp3Util {
    /**
     * 跳过 ID3 标签
     *
     * @param inputStream 输入的音频流
     * @throws IOException IO 异常
     */
    public static void skipID3(InputStream inputStream) throws IOException {
        // 读取 ID3 标签头部
        inputStream.mark(10);
        byte[] header = new byte[10];
        int read = inputStream.read(header, 0, 10);
        if (read < 10) {
            inputStream.reset();
            return;
        }

        // 检查是否有 ID3 标签
        if (header[0] == 'I' && header[1] == 'D' && header[2] == '3') {
            // 计算元数据大小
            int size = (header[6] << 21) | (header[7] << 14) | (header[8] << 7) | header[9];

            // 跳过元数据
            int skipped = 0;
            int skip = 0;
            do {
                skip = (int) inputStream.skip(size - skipped);
                if (skip != 0) {
                    skipped += skip;
                }
            } while (skipped < size && skip != 0);
        } else {
            inputStream.reset();
        }
    }
}
