package com.github.tartaricacid.netmusic.api;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

/**
 * @author 内个球
 */
public class EncryptUtils {
    private static final String EAPI_KEY = "e82ckenh8dichen8";
    private static final String EAPI_DIGEST_SEP = "36cd479b6b5";

    public static String encryptedParam(String text) throws Exception {
        if (text == null) {
            return "params=null&encSecKey=null";
        }
        String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7" +
                         "b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280" +
                         "104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932" +
                         "575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b" +
                         "3ece0462db0a22b8e7";
        String nonce = "0CoJUm6Qyw8W8jud";
        String pubKey = "010001";
        String secKey = getRandomString();
        String encText = aesEncrypt(aesEncrypt(text, nonce), secKey);
        String encSecKey = rsaEncrypt(secKey, pubKey, modulus);
        return "params=" + URLEncoder.encode(encText, "UTF-8") + "&encSecKey=" + URLEncoder.encode(encSecKey, "UTF-8");
    }

    private static String aesEncrypt(String text, String key) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec("0102030405060708".getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        // commons-codec 在专用服务端不存在, 这里改用 JDK 自带的 Base64
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String rsaEncrypt(String text, String pubKey, String modulus) {
        text = new StringBuilder(text).reverse().toString();
        BigInteger val = new BigInteger(1, text.getBytes());
        BigInteger exp = new BigInteger(pubKey, 16);
        BigInteger mod = new BigInteger(modulus, 16);
        StringBuilder hexString = new StringBuilder(val.modPow(exp, mod).toString(16));
        if (hexString.length() >= 256) {
            return hexString.substring(hexString.length() - 256);
        } else {
            while (hexString.length() < 256) {
                hexString.insert(0, "0");
            }
            return hexString.toString();
        }
    }

    private static String getRandomString() {
        StringBuilder stringBuffer = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 16; i++) {
            stringBuffer.append(Integer.toHexString(r.nextInt(16)));
        }
        return stringBuffer.toString();
    }

    /**
     * eapi 加密（PC/移动端API加密）
     * <p>
     * 流程:
     * 1. 拼接签名消息: nobody{url}use{json}md5forencrypt
     * 2. MD5 签名
     * 3. 拼接: {url}-{EAPI_DIGEST_SEP}-{json}-{EAPI_DIGEST_SEP}-{md5}
     * 4. AES-ECB 加密 → 十六进制大写
     *
     * @param url  API路径，如 /api/song/enhance/player/url
     * @param json 请求体的JSON字符串
     * @return 加密后的请求参数，格式为 params={hex}
     */
    public static String eapiEncrypt(String url, String json) throws Exception {
        // 1. 拼接签名消息并计算MD5
        String message = "nobody" + url + "use" + json + "md5forencrypt";
        String digest = md5Hex(message);

        // 2. 拼接payload
        String payload = url + "-" + EAPI_DIGEST_SEP + "-" + json + "-" + EAPI_DIGEST_SEP + "-" + digest;

        // 3. AES-ECB加密
        byte[] encrypted = aesEcbEncrypt(payload.getBytes(StandardCharsets.UTF_8), EAPI_KEY.getBytes(StandardCharsets.UTF_8));

        // 4. 转为十六进制大写
        String params = bytesToHex(encrypted).toUpperCase();

        return "params=" + URLEncoder.encode(params, "UTF-8");
    }

    /**
     * AES-ECB 加密（PKCS5Padding）
     */
    private static byte[] aesEcbEncrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    /**
     * MD5 哈希，返回32位小写十六进制字符串
     */
    private static String md5Hex(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    /**
     * 字节数组转十六进制字符串（小写）
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
