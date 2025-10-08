package com.github.tartaricacid.netmusic.network.message.servermusic;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.tools.UploadMusicWhiteList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 处理服务器上传音乐的请求
 * @author hello_luckyhuang
 */
public class UploadMusicRequestTool {
    public enum RequestType {
        START,
        DATA,
        END,
        ERR
    }

    // 存储上传的文件信息
    private final static Map<String, FileUploadFile> fileMap = new ConcurrentHashMap<>();

    private static final int CHUNK_SIZE = 16384;
    private final String fileId;      // 文件ID
    private final RequestType type;   // 请求类型
    private final String fileName;    // 文件名
    private final long dataSize;      // 数据大小
    private final int sequence;       // 数据块序列
    private final byte[] data;        // 文件数据

    public UploadMusicRequestTool(String fileId, RequestType type, String fileName, long fileSize, int sequence, byte[] data) {
        this.fileId = fileId;
        this.type = type;
        this.fileName = fileName;
        this.dataSize = fileSize;
        this.sequence = sequence;
        this.data = data;
    }

    public static UploadMusicRequestTool decode(FriendlyByteBuf buf) {
        return new UploadMusicRequestTool(buf.readUtf(), buf.readEnum(RequestType.class), buf.readUtf(), buf.readLong(), buf.readInt(), buf.readByteArray());
    }

    public static void encode(UploadMusicRequestTool message, FriendlyByteBuf buf) {
        buf.writeUtf(message.fileId);
        buf.writeEnum(message.type);
        buf.writeUtf(message.fileName);
        buf.writeLong(message.dataSize);
        buf.writeInt(message.sequence);
        buf.writeByteArray(message.data);
    }

    public static void handle(UploadMusicRequestTool message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            final String fileId = message.fileId;
            CompletableFuture.runAsync(() -> {
                ServerPlayer player = context.getSender();
                switch (message.type) {
                    case START: {
                        // 验证权限
                        if (!UploadMusicWhiteList.loadWhiteList().getWhitelist().contains(player.getUUID().toString())) {
                            NetworkHandler.sendToClientPlayer(
                                    new UploadMusicResponseTool(
                                            message.fileId,
                                            UploadMusicResponseTool.ResponseType.NO_PERMISSION,
                                            "No permission"
                                    ), player
                            );
                            break;
                        }

                        // 开始接收
                        FileUploadFile uploadFile = new FileUploadFile(message.fileId, message.fileName, message.dataSize, message.sequence);
                        uploadFile.setReceivedSize(0);

                        // 创建文件
                        try {
                            File file = new File("saved_music/" + message.fileName);
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            uploadFile.setFile(file);
                            fileMap.put(message.fileId, uploadFile);

                            // 发送确认响应
                            NetworkHandler.sendToClientPlayer(
                                    new UploadMusicResponseTool(
                                            message.fileId,
                                            UploadMusicResponseTool.ResponseType.START_ACK,
                                            ""
                                    ), player
                            );
                            NetMusic.LOGGER.info("Start receive file: {}", message.fileName);
                        } catch (Exception e) {
                            NetMusic.LOGGER.error("Create file failed.", e);
                            handleErr(message.fileId);
                            NetworkHandler.sendToClientPlayer(
                                    new UploadMusicResponseTool(
                                            message.fileId,
                                            UploadMusicResponseTool.ResponseType.ERR,
                                            "create file failed"
                                    ), player
                            );
                        }
                        break;
                    }
                    case DATA: {
                        // 数据包
                        FileUploadFile uploadFile = fileMap.get(message.fileId);
                        if (uploadFile != null) {
                            try {
                                uploadFile.writeFile(message.data, message.sequence * (long) CHUNK_SIZE, message.sequence);
                                uploadFile.setReceivedSize(uploadFile.getReceivedSize() + message.data.length);

                                // 发送进度响应
                                if (uploadFile.getReceivedSize() % (CHUNK_SIZE * 12) == 0) { // 每约1MB发送一次进度
                                    double progress = (double) uploadFile.getReceivedSize() / uploadFile.getFileSize() * 100;
                                    NetworkHandler.sendToClientPlayer(
                                            new UploadMusicResponseTool(
                                                    message.fileId,
                                                    UploadMusicResponseTool.ResponseType.PROGRESS,
                                                    String.format("%.2f%%", progress)
                                            ), player
                                    );
                                }
                            } catch (Exception e) {
                                NetMusic.LOGGER.error("Write file failed", e);
                                handleErr(message.fileId);
                                NetworkHandler.sendToClientPlayer(
                                        new UploadMusicResponseTool(
                                                message.fileId,
                                                UploadMusicResponseTool.ResponseType.ERR,
                                                "write file failed"
                                        ), player
                                );
                            }
                        }
                        break;
                    }
                    case END: {
                        // 检查是否接收所有块
                        Set<Integer> table = fileMap.get(message.fileId).getChunkIncompleteTable();
                        if (!table.isEmpty()) {
                            // 发送补发请求
                            NetworkHandler.sendToClientPlayer(
                                    new UploadMusicResponseTool(
                                            message.fileId,
                                            UploadMusicResponseTool.ResponseType.MISSING_CHUNKS,
                                            table.stream()
                                                    .map(String::valueOf)
                                                    .collect(Collectors.joining(","))
                                    ), player
                            );
                        } else {
                            // 结束上传
                            FileUploadFile uploadFile = fileMap.remove(message.fileId);
                            if (uploadFile != null) {
                                try {
                                    NetMusic.LOGGER.info("File upload Done: {}", uploadFile.getFileName());
                                    uploadFile.cleanup();

                                    NetworkHandler.sendToClientPlayer(
                                            new UploadMusicResponseTool(
                                                    message.fileId,
                                                    UploadMusicResponseTool.ResponseType.END_ACK,
                                                    ""
                                            ), player
                                    );
                                } catch (Exception e) {
                                    NetMusic.LOGGER.error("Close file failed", e);
                                }
                            }
                        }
                        break;
                    }
                    case ERR: {
                        handleErr(message.fileId);
                        break;
                    }
                }
            }).exceptionally(e -> {
                NetMusic.LOGGER.error("Response Failed: ", e);
                handleErr(fileId);
                return null;
            });
        }
        context.setPacketHandled(true);
    }

    private static void handleErr(String fileId) {
        var file = fileMap.remove(fileId);
        if (file != null) {
            file.cleanup();
        }
    }

    /**
     * 文件上传状态管理类
     * 用于跟踪文件上传的进度和状态
     */
    static class FileUploadFile {
        private final String fileId;        // 文件唯一标识
        private String fileName;            // 文件名
        private File file;                  // 文件对象
        private long fileSize;              // 文件总大小
        private long receivedSize;          // 已接收大小
        private RandomAccessFile raf;       // 文件随机写入
        private final Set<Integer> chunkIncompleteTable;    // 文件块未完成清单
        // 构造函数
        public FileUploadFile(String fileId, String fileName, long fileSize, int chunkNum) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            chunkIncompleteTable = IntStream.rangeClosed(1, chunkNum).boxed().collect(Collectors.toSet());
        }

        // 写入函数
        public void writeFile(byte[] data, long position, int sequence) throws IOException {
            raf.seek(position);
            raf.write(data);
            chunkIncompleteTable.remove(sequence);
        }

        // 清理资源
        public void cleanup() {
            // 如果上传失败或取消，删除不完整的文件
            if (file != null && file.exists()) {
                file.delete();
            }
        }

        public String getFileName() {
            return fileName;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) throws FileNotFoundException {
            this.file = file;
            if (file != null) {
                this.fileName = file.getName();
                raf = new RandomAccessFile(file, "rw");
            }
        }

        public long getFileSize() {
            return fileSize;
        }

        public long getReceivedSize() {
            return receivedSize;
        }

        public Set<Integer> getChunkIncompleteTable() {
            return chunkIncompleteTable;
        }

        public void setReceivedSize(long receivedSize) {
            this.receivedSize = receivedSize;
        }

        public String getFileId() {
            return fileId;
        }
    }
}
