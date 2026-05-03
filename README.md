## 开发者文档

### 扩展音频流处理 (IAudioStreamHandler)

Net Music 提供了 `IAudioStreamHandler` 接口，用于抽象“根据 URL 获取 `AudioInputStream`”的逻辑。该扩展点允许其他模组接管*
*特殊网络流**的解析与加载过程。

### 适用场景

如果你的模组需要让 Net Music 播放以下类型的音频源，请实现此接口：

* **特殊网络请求**：需附加自定义 HTTP 头、Cookie 或签名参数。
* **复杂解析逻辑**：需经过鉴权、换源、跳转或解密后才能获取真实流地址。
* **特殊流格式**：特定直播流或分片流（如 M3U8）。

---

### 接口定义

**文件路径**：`src/main/java/com/github/tartaricacid/netmusic/client/api/IAudioStreamHandler.java`

```java
public interface IAudioStreamHandler {
    boolean canHandle(URL url);

    AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException;

    default int getPriority() {
        return 0;
    }
}
```

| 方法                   | 说明                                |
|:---------------------|:----------------------------------|
| `canHandle(URL url)` | 判断当前处理器是否应接管该 URL。                |
| `handle(URL url)`    | 建立连接并返回可正常解码的 `AudioInputStream`。 |
| `getPriority()`      | 处理器优先级。数值越大，越早被匹配验证。              |

---

### 注册方式

Net Music 会在客户端启动时触发 `AudioStreamHandlerEvent`。你需要在**客户端环境**下监听此事件并注册你的 Handler：

```java
public class YourModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AudioStreamHandlerEvent.CALLBACK.register(event -> 
                event.registerHandler(new YourAudioStreamHandler()));
    }
}
```

---

### 示例：注入鉴权请求头

以下示例展示了如何接管特定域名的音频地址，并在请求时补充 `Authorization` 鉴权头：

```java
public class ExampleProtectedHttpHandler implements IAudioStreamHandler {
    @Override
    public boolean canHandle(URL url) {
        // 精确匹配：仅处理特定域名和路径的 HTTPS 请求
        return "https".equalsIgnoreCase(url.getProtocol())
               && "example.com".equalsIgnoreCase(url.getHost())
               && url.getPath().startsWith("/protected-audio/");
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", "Bearer <token>");
        connection.setRequestProperty("User-Agent", "Your Mod");

        BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
        return AudioSystem.getAudioInputStream(stream);
    }

    @Override
    public int getPriority() {
        return 50; // 设定较高优先级以优先匹配
    }
}
```

---

### 优先级与匹配流程

播放音频时，Net Music 会按 **优先级从高到低** 遍历所有已注册的 Handler。第一个 `canHandle(url)` 返回 `true` 的处理器将独占处理该
URL。若全都不匹配，则抛出 `UnsupportedAudioFileException`。

**内置 Handler 优先级参考表：**

| 内置处理器                | 优先级   | 定位                     |
|:---------------------|:------|:-----------------------|
| `M3u8Handler`        | `100` | m3u8 aac 广播流格式解析       |
| `NetEaseHttpHandler` | `10`  | 添加特殊网易云音乐 header 的音源解析 |
| `LocalFileHandler`   | `0`   | 本地文件兜底                 |
| `DirectHttpHandler`  | `0`   | 通用 HTTP/HTTPS 直链读取兜底   |

**优先级设定建议：**

* **专用型**处理器（如针对特定域名），优先级应**高于**内置逻辑（>10）。
* **通用/兜底型**处理器，优先级建议保持极低。

---

### ⚠️ 开发注意事项

1. **严格限定客户端**：此接口为纯客户端扩展，服务端切勿注册。
2. **拒绝模糊匹配**：如果无法处理某 URL，务必在 `canHandle` 直接返回 `false`，**不要**在 `handle` 方法中做兼容或兜底。
3. **保持条件精确**：`canHandle` 逻辑需尽可能严格，避免“误抢”本属于其他处理器的 URL。
4. **隔离网络行为**：如果需要注入 Header 等信息，请仅在当前 Handler 内部的 `URLConnection` 上操作，严禁修改全局网络配置。
5. **异步处理**：`handle` 方法会异步执行，你可以在这里执行耗时的网络请求等内容。