package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "E:\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "2432sdsdAD@#@#sfdsf23");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 1);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzY1NjcyMjd9.XiCDvfn-FJdh510xr8GPinU-uu238iBF8r-bYGxXybX-Ddxj7F9EtZYZc3T_2yqNYfw9xwGrw6dPpsFAj-PykE966PirjJG0zh-76o20m7pNeSdcsr-ab36PJtwnuVF3jKxJGoHyVjFoLam8mi4_NJThxY_UidEzdps4D7PiE8T4kxcnXO-DdluVE5c-YzHN-30NJPANSgFD40e9TFuGvRlzgsdqjkH8AGMI99ltzWfZZxESQIdrUpSEt9ngNkGb_fsjLGsY2ErwxPZxbu2_naihtw34-yYW3GYgXbFea1RQKMGGnyY2ClKgeR6s5qVnOjPW6u8AUCxSZlZ1NEasXw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}