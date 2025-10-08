package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.servermusic.CommonMessage;
import com.github.tartaricacid.netmusic.network.message.servermusic.MsgBoxMessage;
import com.github.tartaricacid.netmusic.network.message.servermusic.RequestMusicFromServerMessage;
import com.github.tartaricacid.netmusic.network.message.servermusic.SendMusicDataMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tartaricacid.netmusic.network.message.servermusic.SendMusicDataMessage.MESSAGEBOX;

public class MCServerAudioStream extends InputStream {
    private CountDownLatch latch = null;
    private final String streamId;
    private final String musicName;
    private long position;
    private long fileSize;
    private byte[] currentBuffer;
    private int bufferPosition;
    private static final int CHUNK_SIZE = 16384;

    public MCServerAudioStream(URL url) {
        streamId = UUID.randomUUID().toString().substring(0, 8);
        MESSAGEBOX.put(streamId, this::onResponseReceived);

        String[] parts = url.toString().split("[/\\\\]");
        this.musicName = parts[parts.length-1];
        this.position = 0;

        // 向服务器获取文件大小
        getFileSizeFromServer(musicName);
        latch = new CountDownLatch(1);
        boolean received;
        try {
            received = latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (!received) {
            fileSize = 0;
            NetMusic.LOGGER.error("Get file size failed. Connect time out.");
            return;
        }

        this.currentBuffer = new byte[0];
        this.bufferPosition = 0;
    }

    @Override
    public int read() throws IOException {
        if (position >= fileSize) {
            return -1;
        }

        if (bufferPosition >= currentBuffer.length) {
            loadNextChunk();
        }

        if (currentBuffer == null) {
            return -1;
        }

        if (currentBuffer.length == 0) {
            return -1;
        }

        int data = currentBuffer[bufferPosition] & 0xFF;
        bufferPosition++;
        position++;

        return data;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        if (position >= fileSize) {
            return -1;
        }

        int totalRead = 0;
        while (len > 0 && position < fileSize) {
            if (bufferPosition >= currentBuffer.length) {
                loadNextChunk();
            }

            if (currentBuffer == null) {
                return -1;
            }

            int available = currentBuffer.length - bufferPosition;
            int toRead = Math.min(len, available);

            System.arraycopy(currentBuffer, bufferPosition, b, off, toRead);

            bufferPosition += toRead;
            position += toRead;
            off += toRead;
            len -= toRead;
            totalRead += toRead;
        }

        return totalRead;
    }

    private void loadNextChunk() throws IOException {
        try {
            int chunkSize = (int) Math.min(CHUNK_SIZE, fileSize - position);
            NetworkHandler.CHANNEL.sendToServer(new RequestMusicFromServerMessage(
                    streamId,
                    musicName,
                    position,
                    chunkSize
            ));
            latch = new CountDownLatch(1);

            boolean received;
            // 阻塞等待，最多等待5s
            received = latch.await(5, TimeUnit.SECONDS);
            if (!received) {
                currentBuffer = null;
                NetMusic.LOGGER.error("Get music failed. Connect time out.");
                return;
            }

            bufferPosition = 0;
        } catch (Exception e) {
            currentBuffer = null;
            NetMusic.LOGGER.error("Read remote music failed.");
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long newPosition = Math.min(position + n, fileSize);
        long skipped = newPosition - position;
        position = newPosition;
        currentBuffer = new byte[0]; // 清空当前缓冲区
        bufferPosition = 0;
        return skipped;
    }

    @Override
    public void close() {
        MESSAGEBOX.remove(streamId);
    }

    public void cacheData(String url) throws IOException, InterruptedException {
        long size = getFileSize();
        int off = 0;
        while (true) {
            byte[] b = new byte[16384];
            int read = read(b, 0, b.length);
            if (read != -1) {
                MusicPlayManager.musicDataCache.write(url, b, off);
                off += read;
                float progress = (float) off / size;
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("message.netmusic.music_player.loading_music", String.format("%.2f%%", progress*100)), false);
            } else {
                break;
            }
            Thread.sleep(2);
        }
    }

    private void getFileSizeFromServer(String fileName) {
        NetworkHandler.CHANNEL.sendToServer(new CommonMessage(
                streamId,
                CommonMessage.MsgType.ASK,
                "getFileSize/"+fileName,
                0
        ));
    }

    public long getFileSize() {
        return fileSize;
    }

    public void onResponseReceived(MsgBoxMessage message) {
        if (latch != null) {
            if (message instanceof CommonMessage msg) {
                if (msg.msgType == CommonMessage.MsgType.INFO && msg.msg.equals("file size")) {
                    this.fileSize = msg.data;
                } else if (msg.msgType == CommonMessage.MsgType.ERR) {
                    NetMusic.LOGGER.error("Get music from server failed. Cause by: {}.", msg.msg);
                }
            } else if (message instanceof SendMusicDataMessage msg) {
                this.currentBuffer = msg.getData();
            }
            latch.countDown(); // 释放阻塞
        }
    }
}
