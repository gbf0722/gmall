package com.atguigu.gmall.order.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101400687511";

    // 商户私钥，您的PKCS8格式RSA2私钥
                                           //MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEXhfkox3cQNqUM9hgH3MXQzPAEceM0hjSdGSy0xIZ5+Sjg/KzGt8ydXeNNY4mwNDAQrL9kJkuBExhC0xFyYcbmGKj66aV9/FJEBGGRJ8NNIIYxGMUY2PSdCHjqpMuPJ31BSFbMmqBASmSzTSVTKCdQiBC1xkhTIB65r1/DLzUiSTCBEjuH9aWCGzeWCi5qsA7W3iHTyk07u6Hl0/J22C3iCtkynFjk5PdZ9hZcZOC2E00Z9x511l4GoTt+q1t1FLei3z0Eq0fJU/FUtfFtkAK9I9lKcamipn7EcAbAFbSj/jtzGKzk5mWCaOgWEg1x+Fv2k3ja05qRhyqUM5UOhPvAgMBAAECggEAPIeOo4YX9I1Ppu/iZWpcRen8MzuuJe45XZvOJJwyrdgGDOQcytxh7oAa8F7qJzyK+TBHC37hHVLn3oS9Fzil6O+RQ+ZR4khkdQW9Dco5dV9qkorTyBH/JtDmYmaxMTaVAOOcskhmvaFbcn8WKaPF2sPPXuFf44NJSWpUg1G+0+1K8xYJeI7ZQBHNeuAoizFRBsKlJffkKFOYxBSCyXuAOuykP4rzOPAy4zpeCAhlBB0rSDlbvv28klt5f8QWVtgCGBmdh3x1o8cFcS+bFviHLopLdQc3kij4MRGs7pjmApeDcCNLUP5mGOTMZgXiFDgwzzQd8Yik0lu3p4lpdPisYQKBgQDiwXPNwzVi2gDcWUHnhetAaCl7AcV2phH6tsfUtPM9sd8vuM3F8x7+zAJocOCFNuqusyJgmRHJzBFurHUpdZd45RtRjkPD7wnwftY3j5s2qfGv7lRUtkSyLNccTJkWxtH7cDaaaf2ZVfiotAUC8F+C1J9fvqz/IAS6D0d+m16CvwKBgQCVcFSCqZ50QZpUy7vS5OwUN/7dw+MDZAJZZKxDOXta12wnJCxp8F7i/pNf6gPhD79B6cMfS4trILrD3WB1bnd+cbbZu9hQklg7sgpujzQfqASkItmbyIwi2UIUgCrImL4815rK8QyDnh3MqQt5DYXHq+wVuOtS6XN3vssU8uIq0QKBgQDMhoDVTwWQK+Iq3bc8BOa0KT+A25An/JKq+MI1cSEUvKL6gE3aBfgnCgwB+A4LGgWDnb+cMXSB97ahhJwI0S/Ws494eEFXMPxeF4lQhA8TtyVv8h/OaLh9nURJGi3bBWnQyN33WTKMY6btQ7JhfD4p224FBg8z7sVUrQNnhuwwMQKBgDb4t13JX4yMhWUj/Ne7dkyJvKt/X4bjxylzKxsfjg0AqbxMRjpCPeVqmHPz9bFYxZZNk4VzmLmztIBQcyc/l51f30+BFjWtX9+1wrig3R+4At0OJNZ1k3dOTEyWSSa5aWGgPkThHyVbiMK8fIfvPYMR7yqed5JsZhlqifIO1rvBAoGAJ681QDRRq43xxroLiUHtKMZtR0PAWKHhwY1BK2D6LClQWDulQOrWo6p+t2hixoUIYJOw6qXLx77C0dKyCiiNWBlsFJ/B/V38tMwzRiDMhO7vZa/jLwv1OZdDxa8Uco96GXZ9WeD4OXtoBLJC7f4TVv6i97Ahyh5yYbkGGEbN3ug=
                                          // MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEXhfkox3cQNqUM9hgH3MXQzPAEceM0hjSdGSy0xIZ5+Sjg/KzGt8ydXeNNY4mwNDAQrL9kJkuBExhC0xFyYcbmGKj66aV9/FJEBGGRJ8NNIIYxGMUY2PSdCHjqpMuPJ31BSFbMmqBASmSzTSVTKCdQiBC1xkhTIB65r1/DLzUiSTCBEjuH9aWCGzeWCi5qsA7W3iHTyk07u6Hl0/J22C3iCtkynFjk5PdZ9hZcZOC2E00Z9x511l4GoTt+q1t1FLei3z0Eq0fJU/FUtfFtkAK9I9lKcamipn7EcAbAFbSj/jtzGKzk5mWCaOgWEg1x+Fv2k3ja05qRhyqUM5UOhPvAgMBAAECggEAPIeOo4YX9I1Ppu/iZWpcRen8MzuuJe45XZvOJJwyrdgGDOQcytxh7oAa8F7qJzyK+TBHC37hHVLn3oS9Fzil6O+RQ+ZR4khkdQW9Dco5dV9qkorTyBH/JtDmYmaxMTaVAOOcskhmvaFbcn8WKaPF2sPPXuFf44NJSWpUg1G+0+1K8xYJeI7ZQBHNeuAoizFRBsKlJffkKFOYxBSCyXuAOuykP4rzOPAy4zpeCAhlBB0rSDlbvv28klt5f8QWVtgCGBmdh3x1o8cFcS+bFviHLopLdQc3kij4MRGs7pjmApeDcCNLUP5mGOTMZgXiFDgwzzQd8Yik0lu3p4lpdPisYQKBgQDiwXPNwzVi2gDcWUHnhetAaCl7AcV2phH6tsfUtPM9sd8vuM3F8x7+zAJocOCFNuqusyJgmRHJzBFurHUpdZd45RtRjkPD7wnwftY3j5s2qfGv7lRUtkSyLNccTJkWxtH7cDaaaf2ZVfiotAUC8F+C1J9fvqz/IAS6D0d+m16CvwKBgQCVcFSCqZ50QZpUy7vS5OwUN/7dw+MDZAJZZKxDOXta12wnJCxp8F7i/pNf6gPhD79B6cMfS4trILrD3WB1bnd+cbbZu9hQklg7sgpujzQfqASkItmbyIwi2UIUgCrImL4815rK8QyDnh3MqQt5DYXHq+wVuOtS6XN3vssU8uIq0QKBgQDMhoDVTwWQK+Iq3bc8BOa0KT+A25An/JKq+MI1cSEUvKL6gE3aBfgnCgwB+A4LGgWDnb+cMXSB97ahhJwI0S/Ws494eEFXMPxeF4lQhA8TtyVv8h/OaLh9nURJGi3bBWnQyN33WTKMY6btQ7JhfD4p224FBg8z7sVUrQNnhuwwMQKBgDb4t13JX4yMhWUj/Ne7dkyJvKt/X4bjxylzKxsfjg0AqbxMRjpCPeVqmHPz9bFYxZZNk4VzmLmztIBQcyc/l51f30+BFjWtX9+1wrig3R+4At0OJNZ1k3dOTEyWSSa5aWGgPkThHyVbiMK8fIfvPYMR7yqed5JsZhlqifIO1rvBAoGAJ681QDRRq43xxroLiUHtKMZtR0PAWKHhwY1BK2D6LClQWDulQOrWo6p+t2hixoUIYJOw6qXLx77C0dKyCiiNWBlsFJ/B/V38tMwzRiDMhO7vZa/jLwv1OZdDxa8Uco96GXZ9WeD4OXtoBLJC7f4TVv6i97Ahyh5yYbkGGEbN3ug=
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCSdGBMGZnTCCo1pAeoaVZIKambZU1+Wtvo6MlVeAgcog9WJ2zYwToASOFnLDtxybu5D5J8hrfW/esJBf0p8XvjtpdP8xza/qZW+4+8aDrV0bOsU5/bBZT4nStNP4E05dZasBQC/bHQ4rAoyshGUpghFryxfh7RI6rnuR5gxcs9AN44VNNhZmFHu6iftqcDMA2tyJbR0R0H+SUn1W7Ct7CiqwTLcdSKGGFoiItQP2G9VqoQTni2vE5C8XdtSdeuvRFA7HGja8e7MHB7MoTn59Z6KDJ7tcaPdd9rm55VYcOOvve8wU1i0GYaDTVp4e5JzBIHw9nD7LqRAzZOvnEskMVhAgMBAAECggEATTHqUDJuUj3Lgdqj7rEXOZI8RBxxbFiX8XxCIzrRi8YgL1k4lkfDOsMvjCJUcvXSbQk4PUsFO907NdZiSuluU+i6ePm8C/KN2uteKAEIggu2mO67I8cdbq3pVR5UDH3ZLpQA+FKgl2nlUJsSVX7TkR1a+HNx6urR+rIowPmA1vYCCQuhacJtnS6zeENoS7nl6VfY1PRTrs35fjFqVdlVpHB4C1uNVCcQFkEL+YDC9nA5VXSvaZdKcKUZZmxkS5dDyQVPwuZgkFg6vnPSfe88Xukk5VWv3Br0hxC9OiRLLG6/liiFYLMcHOgz4cocd++vSywON7WUlN87wfDLktjywQKBgQDDt9L46U9mKM06LLwcN5nGoC3fWi52zwtst7woXDxvwB3nXgCFcO2UEgglw5exfzhYkJpXu1T2pL8Otns4nf/u8WsmizfluL/WYqZKk9eStOfYXkQk2NVY1X1+kk6TpUPyeI3nPWTyoRM6isJA5igEPXjsequN5MU5alt0u1DGiwKBgQC/kC0dTfe90j3e7Hcon3YhzAzEpxX1DOzykTfsHLOb90GcGEe+cOEEnOz+T/g52l/kIc7j7fSIh6vfUhhpxEq3HjrLr5NPDBkDOJK+DOgcTJ8QX6XiUpbqpbr3ArzTibfg0/hh7YI9o946WlYdi2Q1Mdd8Qf5TmyOo7vcaS79NQwKBgQCMfnfG0Ix7lEyGNzittMgBiliDndP+22+jEl3SRyvnOGz++j67i64v/aW1nitVcjK7eMoiskoyZ17zK9FWei0PlGPLnmJ42F91gQlIPcqg+JX5funB3ZmBFHfqlRIs8JTPCPUsiqjddv07qAH73nvZV1tnvkNKL2/YYF3YdOJuMwKBgE7G9LaTZKksN4NgTRRol0UIy8tnRvFSUnLiY1P27anbAaE2nvJjKzYhbpiavGLWOg7wFEvJhIFNAdJByYZdUbiCdv1ig7rDJ9oaPsZnOmDA5bzvCKHNAwEWDJAeERcREIwlv4RODvxYUkA+/CEEPL5fc9VGslcheoWzV+z5b39PAoGABXdh5+0oOPYXWUVsDMnEZIw0G02T3AeoibHYqWyxEauXAb+KxMNz5T+9xFI9ldv4dXs6svm+DPcSwfVvkF4HJql/MU97L+2rVssoFZCWv9NjBRIjg558j34o0l89tQfGdGPatc11LO70xXWKy559b49Me94g53ADCHXjvocqXdw=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgAtRj4WA7uAsZXuhOhJtroZ9II/Da4GHEnjcW4+GSxNeIxTtkI8m3hHOyGqZVcITbAY97nleumzqTA+MsFBtYM5PagUcnFx4Cdt6HcWNib+w+3t5iNkNNdkkGE+xQgKZfm/wGPHaXgUm192/EMMosd4ZQK6nsKcdwXYBAzGN5+5Yqvf0eSXElytMXtvG0VEZVsc/GZOUIrWXy7ADHlW206dvzLUxGH+MCrcvEUv9U1/9n/iW3nxZtpRY1ip+cRRyaNs7kkrUnnvBRDpGkvTtYwofArwdiZEQdYewSS5my8s/0O7PVjbKssQPED5akO+t5Y7OiKKSKs726aOBllmoCwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
