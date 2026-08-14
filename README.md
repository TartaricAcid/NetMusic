# Net Music Mod 网络音乐机

A Minecraft mod that allows you to play music from network sources.

一个允许你在 Minecraft 中播放网络音乐的模组。

## Support 支持情况

<table>
  <thead>
    <tr>
      <th align="center">Minecraft Version</th>
      <th align="center">Mod Loader</th>
      <th align="center">Status 状态</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center"><b>26.2</b></td>
      <td align="center">Fabric</td>
      <td align="center">✅ Active 适配中（由 v1.5.1-fabric+mc26.1 移植）</td>
    </tr>
    <tr>
      <td rowspan="2" align="center"><b>1.21.1</b></td>
      <td align="center">NeoForge</td>
      <td align="center">✅ Active 更新中</td>
    </tr>
    <tr>
      <td align="center">Fabric</td>
      <td align="center">✅ Active 更新中</td>
    </tr>
    <tr>
      <td rowspan="2" align="center"><b>1.20.1</b></td>
      <td align="center">Forge</td>
      <td align="center">✅ Active 更新中</td>
    </tr>
    <tr>
      <td align="center">Fabric</td>
      <td align="center">✅ Active 更新中</td>
    </tr>
    <tr>
      <td rowspan="2" align="center">1.19.2</td>
      <td align="center">Forge</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td align="center">Fabric</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td rowspan="2" align="center">1.18.2</td>
      <td align="center">Forge</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td align="center">Fabric</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td rowspan="2" align="center">1.16.5</td>
      <td align="center">Forge</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td align="center">Fabric</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
    <tr>
      <td align="center">1.12.2</td>
      <td align="center">Forge</td>
      <td align="center">✖ Stopped 停止更新</td>
    </tr>
  </tbody>
</table>

## 26.2 Fabric 适配说明（v1.5.1-fabric+mc26.2）

本分支基于上游 `26.1-fabric` 分支的 **v1.5.1-fabric+mc26.1**（对应 Minecraft 26.1.2）适配至 **Minecraft 26.2**（Fabric Loader 0.19.3，Java 25+）。

### 修改内容

- **GUI API 变更**：`Minecraft.setScreen` → `setScreenAndShow`；移除 `extractDeferredSubtitles` 调用
- **HUD / 消息 API 变更**：`Gui.setOverlayMessage` / `Gui.setNowPlaying` 已迁移至 `Gui.hud`（`Hud` 类）。恢复 actionbar 显示：播放歌名使用 `setNowPlaying`（保留彩色闪烁效果），大喇叭与播放错误提示使用 `setOverlayMessage`
- **消息发送变更**：`Player.displayClientMessage` 已移除，改用 `sendSystemMessage`（VIP 提示、404 错误、音乐列表命令反馈等保持聊天栏）
- **注解变更**：`@MethodsReturnNonnullByDefault` 在 26.2 已移除，删除各 `package-info.java` 中的相关注解
- **Forge Config API Port**：26.2.1 需使用 Fabric 构建（Modrinth Maven 上的 26.2.1 为 NeoForge 构建，不含 Fabric v5 API），改为从本地 `libs/` 引用 `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar`
- **依赖版本**：`minecraft 26.2`、`fabric-loader 0.19.3`、`fabric-api 0.157.0+26.2`、`modmenu 20.0.1`、`cloth-config 26.2.155+fabric`
- **构建工具**：Loom 1.17-SNAPSHOT、Gradle 9.5.1

### 构建说明

`libs/` 目录需包含 `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar`（从 Modrinth 下载的 Fabric 构建，NeoForge 版不适用）。其余依赖（javasound-aac、jflac-codec）已随仓库提供。

## License 许可证
This project uses dual licensing

本项目采用双重许可证

### Source Code 源代码
The source code is licensed under the **BSD 3-Clause License**.

源代码采用 **BSD 3-Clause License（BSD 3 条款许可证）**。
See [LICENSE](LICENSE) file for details.

详见 [LICENSE](LICENSE) 文件。

### Assets and Resources 资源文件

All assets and resources (textures, models, sounds, etc.) are licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**.

所有资源文件（纹理、模型、音效等）采用 **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**（知识共享 署名-非商业性使用-相同方式共享 4.0 国际许可证）。
The file `error.ogg` comes from <https://freesound.org/people/ecfike/sounds/135125/> and is licensed under CC0 1.0 Universal (CC0 1.0).

其中 `error.ogg` 来自于 <https://freesound.org/people/ecfike/sounds/135125/>，授权协议为 CC0 1.0 Universal (CC0 1.0)。
See [LICENSE-ASSETS](LICENSE-ASSETS) file for details.

详见 [LICENSE-ASSETS](LICENSE-ASSETS) 文件。

## Acknowledgments 鸣谢

Thanks to IMG for helping to make the Fabric update

感谢 IMG 帮忙制作的 Fabric 更新

<a href="https://github.com/TartaricAcid/NetMusic/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=TartaricAcid/NetMusic" />
</a>

## 开发者文档
<https://github.com/TartaricAcid/NetMusic/wiki>
