package com.github.tartaricacid.netmusic.api.search;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.google.common.net.HttpHeaders;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.BiConsumer;

public class NeteaseMusicSearch {
    public static void searchFirstSong(String searchText, BiConsumer<HttpResponse<String>, Throwable> callback) {
        Proxy proxy = NetWorker.getProxyFromConfig();
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        ProxySelector selector = ProxySelector.of(addr);

        URI uri = URI.create(WebApi.getSearchUrl(searchText, WebApi.TYPE_SONG, 1));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .proxy(selector)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(30))
                .header(HttpHeaders.REFERER, NetEaseMusic.getReferer())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent())
                .uri(uri).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete(callback);
    }
}
