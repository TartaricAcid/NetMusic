package com.github.tartaricacid.netmusic.api.search;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.google.common.net.HttpHeaders;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class NeteaseMusicSearch {
    private static final HttpClient SEARCH_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .proxy(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    Proxy proxy = NetWorker.getProxyFromConfig();
                    return List.of(Objects.requireNonNullElse(proxy, Proxy.NO_PROXY));
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, java.io.IOException ioe) {
                }
            }).build();

    public static CompletableFuture<HttpResponse<String>> searchSongs(String keywords) {
        URI uri = URI.create(WebApi.getSearchUrl(keywords, WebApi.TYPE_SONG, 5));
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(30))
                .header(HttpHeaders.REFERER, NetEaseMusic.getReferer())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent())
                .uri(uri).GET().build();

        return SEARCH_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
