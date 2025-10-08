package com.github.tartaricacid.netmusic.client.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricData {
    private final Map<Integer, Line> lyric = new HashMap<>();
    private int maxTick = 0;

    public LyricData(String lyricStr) {
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+\\.\\d+)\\](.*)");
        String[] lines = lyricStr.split("\n");

        String[] lrcs = new String[lines.length];
        int[] times = new int[lines.length];

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                try {
                    int min = Integer.parseInt(matcher.group(1));
                    double sec = Double.parseDouble(matcher.group(2));
                    int tick = (int) ((min * 60 + sec) * 20);
                    String lyrics = matcher.group(3).trim();

                    if (!lyrics.equals("暂无歌词")) {
                        lrcs[i] = lyrics;
                        times[i] = tick;
                    }
                    if (tick > maxTick) maxTick = tick;
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }

        for (int i = 0; i < lines.length; i++) {
            if (i == lines.length - 1)
                lyric.put(times[i], new Line(lrcs[i], -1));
            else
                lyric.put(times[i], new Line(lrcs[i], times[i+1]));
        }
    }

    public Line getLyric(int tick) {
        if (tick < maxTick)
            return lyric.get(tick);
        return null;
    }

    public record Line(String text, int nextLineTick) {
    }
}
