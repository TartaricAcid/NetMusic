package com.github.tartaricacid.netmusic.network.message.servermusic;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.tools.MP3Validator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 处理服务器上传音乐的响应
 * @author hello_luckyhuang
 */
public class UploadMusicResponseTool {
    public enum ResponseType {
        START_ACK,
        END_ACK,
        MISSING_CHUNKS,
        PROGRESS,
        NO_PERMISSION,
        ERR
    }

    private final static Map<String, UploadFile> uploadFileMap = new ConcurrentHashMap<>();

    private static final int CHUNK_SIZE = 16384;
    private final String fileId;      // 文件ID
    private final ResponseType type;           // 请求类型
    private final String msg;    // 文件名

    public UploadMusicResponseTool(String fileId, ResponseType type, String msg) {
        this.fileId = fileId;
        this.type = type;
        this.msg = msg;
    }

    public static UploadMusicResponseTool decode(FriendlyByteBuf buf) {
        return new UploadMusicResponseTool(buf.readUtf(), buf.readEnum(ResponseType.class), buf.readUtf());
    }

    public static void encode(UploadMusicResponseTool message, FriendlyByteBuf buf) {
        buf.writeUtf(message.fileId);
        buf.writeEnum(message.type);
        buf.writeUtf(message.msg);
    }

    public static void uploadNewMusic(URL fileUrl, UploadMusicCallBack callBack) {
        try {
            String id = UUID.randomUUID().toString().substring(0, 8);
            File file = new File(fileUrl.toURI());
            if (file.exists()) {
                if (file.length() >= 1024 * 1024 * 10) {
                    callBack.callBack(UploadMusicCallBack.State.ERR, "gui.netmusic.computer.upload.too_large");
                    return;
                } else if (!MP3Validator.isMP3File(file)) {
                    callBack.callBack(UploadMusicCallBack.State.ERR, "gui.netmusic.computer.upload.no_mp3_file");
                    return;
                }
                NetworkHandler.CHANNEL.sendToServer(new UploadMusicRequestTool(
                        id,
                        UploadMusicRequestTool.RequestType.START,
                        file.getName(),
                        file.length(),
                        (int) (file.length() / CHUNK_SIZE),
                        new byte[0])
                );
            } else {
                callBack.callBack(UploadMusicCallBack.State.ERR, "gui.netmusic.computer.upload.file_not_found");
                return;
            }
            uploadFileMap.put(id, new UploadFile(fileUrl, callBack));
        } catch (URISyntaxException e) {
            NetMusic.LOGGER.error(e);
        }
    }

    public static void handle(UploadMusicResponseTool message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        String fileId = message.fileId;
        if (context.getDirection().getReceptionSide().isClient()) {
            CompletableFuture.runAsync(() -> onHandle(message)).exceptionally(e -> {
                NetMusic.LOGGER.error("Upload Failed: ", e);
                var uf = uploadFileMap.get(fileId);
                if (uf != null)
                    uf.callBack.callBack(UploadMusicCallBack.State.ERR, "");
                handleErr(fileId);
                return null;
            });
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(UploadMusicResponseTool message) {
        var uploadFile = uploadFileMap.get(message.fileId);
        var url = uploadFile.url;
        var callBack = uploadFile.callBack;
        // 检查文件是否正在发送
        if (url != null) {
            switch (message.type) {
                case START_ACK: {
                    try (FileInputStream fis = new FileInputStream(url.getFile())) {
                        byte[] buffer = new byte[CHUNK_SIZE]; // 80KB缓冲区
                        int bytesRead;
                        long totalSent = 0;

                        while ((bytesRead = fis.read(buffer)) != -1) {
                            NetworkHandler.CHANNEL.sendToServer(
                                    new UploadMusicRequestTool(
                                            message.fileId,
                                            UploadMusicRequestTool.RequestType.DATA,
                                            "",
                                            bytesRead,
                                            (int) (totalSent / CHUNK_SIZE),
                                            Arrays.copyOf(buffer, bytesRead))
                            );

                            totalSent += bytesRead;

                            // 控制发送速率，避免占用过多网络资源
                            Thread.sleep(1);
                        }
                        sendComplete(message.fileId);
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("Send file data failed", e);
                        callBack.callBack(UploadMusicCallBack.State.ERR, "");
                        handleErr(message.fileId);
                    }
                    break;
                }
                case MISSING_CHUNKS: {
                    try (RandomAccessFile raf = new RandomAccessFile(url.getFile(), "r")) {
                        int[] sequences = Arrays.stream(message.msg.split(",")).mapToInt(Integer::parseInt).toArray();
                        for (int sequence : sequences) {
                            raf.seek(sequence * (long) CHUNK_SIZE);
                            byte[] buffer = new byte[CHUNK_SIZE];
                            int bytesRead;

                            if ((bytesRead = raf.read(buffer)) > 0) {
                                NetworkHandler.CHANNEL.sendToServer(
                                        new UploadMusicRequestTool(
                                                message.fileId,
                                                UploadMusicRequestTool.RequestType.DATA,
                                                "",
                                                bytesRead,
                                                sequence,
                                                Arrays.copyOf(buffer, bytesRead))
                                );
                            }
                            Thread.sleep(1);
                        }
                        Thread.sleep(50);
                        sendComplete(message.fileId);
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("Send music error.");
                        callBack.callBack(UploadMusicCallBack.State.ERR, "");
                        handleErr(message.fileId);
                    }
                    break;
                }
                case PROGRESS: {
                    callBack.callBack(UploadMusicCallBack.State.PROGRESS, message.msg);
                    break;
                }
                case END_ACK: {
                    callBack.callBack(UploadMusicCallBack.State.END, message.msg);
                    uploadFileMap.remove(message.fileId);
                    break;
                }
                case NO_PERMISSION: {
                    callBack.callBack(UploadMusicCallBack.State.NO_PERMISSION, message.msg);
                    uploadFileMap.remove(message.fileId);
                    break;
                }
                case ERR: {
                    callBack.callBack(UploadMusicCallBack.State.ERR, message.msg);
                    uploadFileMap.remove(message.fileId);
                    break;
                }
            }
        }
    }

    private static void sendComplete(String fileId) {
        // 发送结束标记
        NetworkHandler.CHANNEL.sendToServer(
                new UploadMusicRequestTool(
                        fileId,
                        UploadMusicRequestTool.RequestType.END,
                        "",
                        0,
                        0,
                        new byte[0])
        );
    }
    private static void handleErr(String fileId) {
        uploadFileMap.remove(fileId);
        NetworkHandler.CHANNEL.sendToServer(
                new UploadMusicRequestTool(
                        fileId,
                        UploadMusicRequestTool.RequestType.ERR,
                        "",
                        0,
                        0,
                        new byte[0]
                )
        );
    }

    private record UploadFile(URL url, UploadMusicCallBack callBack) {
    }
}
