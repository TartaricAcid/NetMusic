package com.github.tartaricacid.netmusic.api;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author 内个球
 */
public class NetWorker {
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .proxy(new NetWorker.ConfigProxySelector())
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static String get(String url, Map<String, String> requestPropertyData) throws IOException {
        HttpRequest request = createRequestBuilder(url, requestPropertyData).GET().build();
        return send(request, HttpResponse.BodyHandlers.ofString(UTF_8)).body();
    }

    public static String post(String url, String param, Map<String, String> requestPropertyData) throws IOException {
        HttpRequest request = createRequestBuilder(url, requestPropertyData)
                .POST(BodyPublishers.ofString(param, UTF_8)).build();
        return send(request, HttpResponse.BodyHandlers.ofString(UTF_8)).body();
    }

    public static Proxy getProxyFromConfig() {
        Proxy.Type proxyType = GeneralConfig.PROXY_TYPE.get();
        String proxyAddress = GeneralConfig.PROXY_ADDRESS.get();
        if (proxyType == Proxy.Type.DIRECT || StringUtils.isBlank(proxyAddress)) {
            return Proxy.NO_PROXY;
        }
        String[] split = proxyAddress.split(":", 2);
        if (split.length != 2) {
            return Proxy.NO_PROXY;
        }
        try {
            return new Proxy(proxyType, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        } catch (RuntimeException e) {
            NetMusic.LOGGER.error("Invalid proxy address config: {}", proxyAddress, e);
            return Proxy.NO_PROXY;
        }
    }

    private static HttpRequest.Builder createRequestBuilder(String url, Map<String, String> requestPropertyData) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        requestPropertyData.forEach(builder::header);
        return builder;
    }

    public static <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException {
        try {
            return HTTP_CLIENT.send(request, bodyHandler);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while sending HTTP request", e);
        }
    }

    public static class ConfigProxySelector extends ProxySelector {
        @Override
        public List<Proxy> select(URI uri) {
            Proxy proxy = NetWorker.getProxyFromConfig();
            return List.of(Objects.requireNonNullElse(proxy, Proxy.NO_PROXY));
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, java.io.IOException ioe) {
            NetMusic.LOGGER.warn("Failed to connect to proxy for URI: {} via Address: {}", uri, sa, ioe);
        }
    }
}
