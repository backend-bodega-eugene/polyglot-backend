package com.app.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.app.controller.dto.ApplePayDTO;
import com.common.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

/**
 * @Desc : 苹果支付 参考地址：https://www.cnblogs.com/shoshana-kong/p/10991753.html
 * sendHttpsCoon 方法里面包含了增加的业务逻辑
 */
@Slf4j
@Api(value = "苹果支付验证", tags = {"苹果支付测试和正式验证"})
@RestController
@RequestMapping(value = "/app/applepay", method = {RequestMethod.POST})
public class ApplePayController {

    //购买凭证验证地址
    private static final String certificateUrl = "https://buy.itunes.apple.com/verifyReceipt";

    //测试的购买凭证验证地址
    private static final String certificateUrlTest = "https://sandbox.itunes.apple.com/verifyReceipt";

    /**
     * 重写X509TrustManager
     */
    private static TrustManager myX509TrustManager = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    };

    /**
     * 接收自己APP客户端发过来的购买凭证
     * @ApiParam(value = "是否是真实环境,布尔值") ,@ApiParam(value = "苹果传递前端支付成功的值")  @ApiParam(value = "用户ID")
     */
    @PostMapping("/setIapCertificate")
    @Transactional
    public String setIapCertificate(@RequestBody ApplePayDTO dto) {
        //log.info("IOS端发送的购买凭证。数据有 userId = {},receipt = {},chooseEnv = {}",userId,receipt,chooseEnv);
        if (StringUtils.isEmpty(dto.getUserId()) || StringUtils.isEmpty(dto.getReceipt())) {
            return "用户ID 或者 receipt为空";
        }
        String url = null;
        url = dto.getChooseEnv() == true ? certificateUrl : certificateUrlTest;
        final String certificateCode = dto.getReceipt();
        if (StringUtils.isNotEmpty(certificateCode)) {
            String s = sendHttpsCoon(url, certificateCode, dto.getUserId());
            return s;
        } else {
            return "receipt 为空!";
        }
    }

    /**
     * 发送请求 向苹果发起验证支付请求是否有效：本方法有认证方法进行调用
     *
     * @param url  支付的环境校验
     * @param code 接口传递的 receipt
     * @return 结果
     */
    private String sendHttpsCoon(String url, String code, String userId) {
        if (url.isEmpty()) {
            return null;
        }
        try {
            //设置SSLContext
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, new TrustManager[]{myX509TrustManager}, null);
            //打开连接
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            //设置套接工厂
            conn.setSSLSocketFactory(ssl.getSocketFactory());
            //加入数据
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/json");
            JSONObject obj = new JSONObject();
            obj.put("receipt-data", code);
            BufferedOutputStream buffOutStr = new BufferedOutputStream(conn.getOutputStream());
            buffOutStr.write(obj.toString().getBytes());
            buffOutStr.flush();
            buffOutStr.close();
            //获取输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb);
            // 错误的 sb对象是：{"status":21002},苹果官网写的错误也都是2XXXX
            // 具体含义可查：https://developer.apple.com/documentation/appstorereceipts/status
            // 所以 通过长度等于16，我们就可确定苹果支付成功是否有效
            if (sb.length() == 16) {
                return "支付失败,苹果说status异常";
            }
            // 将一个Json对象转成一个HashMap
            JSONObject alljsoncode = JSONUtil.parseObj(sb.toString());
            Object receipt = alljsoncode.get("receipt");
            HashMap hashMap = JSONUtil.toBean(receipt.toString(), HashMap.class);
            // 苹果给的订单号
            String original_transaction_id = (String) hashMap.get("original_transaction_id");
            //TODO 存储订单ID，并检查此订单ID是否存在，如果存在就证明已经增加使用时间(避免二次发货)
            if ("com.hefeixunliao.zhenliao12yuan".equals(hashMap.get("product_id"))) {
                //TODO 执行加日期操作
                log.info("用户ID：{},增加使用时间",userId);
            } else if ("com.hefeixunliao.zhenliao30yuan".equals(hashMap.get("product_id"))) {
                //TODO 执行加日期操作
                log.info("用户ID：{},增加使用时间",userId);
            } else {
                log.info("用户ID：{},向苹果发起验证支付请求没有商品id",userId);
                return "支付失败,当前没有商品id";
            }
            return "支付成功";
        } catch (Exception e) {
            log.error("向苹果发起验证支付请求是否有效出现异常：{}", e.getMessage());
            return "支付过程中,出现了异常!";
        }
    }

    /**
     * 注意：下面代码跟苹果支付业务无关。
     * 这里的code 是前端请求苹果，苹果给前端的一个密钥
     * （如果我们通过base64解密后，可获得signature、purchase-info、environment、pod、signing-status）
     * 这个密钥用于告诉Java服务器 想苹果服务器校验订单是否成功的参数
     */
//    @Test
//    public void jiemi() {
//        String code = "ewoJInNpZ25hdHVyZSIgPSAiQXhmVWRiYUx5T2I5bllOM3hINmQzMnBaOHI2THdmV3ZmZ1NK
//        N1o2QTM4dEY2SjNyUTZoRVZqQ3Rra
//        01wMnhmM1pwWnFQRmw3ZlRIdDVxNVpKZUF6UWh4NWQ1djJrR01uM3NKb3ZBWXNuWENxY3VqclBWU3A5WTFYUTZ
//        jeTlvbVNORWNYVWt0L1dkQXhsRmN6WDRZMTJzcktsMDc3WHJIdk5JMDd0VTZXajgzbVdDNE1HZmF0c2E2UEo1R
//        G5sT2lEOG96RlJ6a0NIQ3Y3bncvRm80dnFCaFpZRmlQSDZzeW1uN2lUQlhTcXlTdlJOTGJXLytUWktKZngxR1d
//        RV3BWdmJ5M0RtV3l4OTRaYkxGRllNODE0aTB2a1lnWDdPdVUwQWprTVFKOEJhNnJGc1hER0hYY2FCdnVhZ1NGa
//        k9iMWdJclZ2MDdmbTlBV3ltNE5KT0dVSHR0Z0FBQVdBTUlJRmZEQ0NCR1NnQXdJQkFnSUlEdXRYaCtlZUNZMHd
//        EUVlKS29aSWh2Y05BUUVGQlFBd2daWXhDekFKQmdOVkJBWVRBbFZUTVJNd0VRWURWUVFLREFwQmNIQnNaU0JKY
//        m1NdU1Td3dLZ1lEVlFRTERDTkJjSEJzWlNCWGIzSnNaSGRwWkdVZ1JHVjJaV3h2Y0dWeUlGSmxiR0YwYVc5dWN
//        6RkVNRUlHQTFVRUF3dzdRWEJ3YkdVZ1YyOXliR1IzYVdSbElFUmxkbVZzYjNCbGNpQlNaV3hoZEdsdmJuTWdRM
//        lZ5ZEdsbWFXTmhkR2x2YmlCQmRYUm9iM0pwZEhrd0hoY05NVFV4TVRFek1ESXhOVEE1V2hjTk1qTXdNakEzTWp
//        FME9EUTNXakNCaVRFM01EVUdBMVVFQXd3dVRXRmpJRUZ3Y0NCVGRHOXlaU0JoYm1RZ2FWUjFibVZ6SUZOMGIzS
//        mxJRkpsWTJWcGNIUWdVMmxuYm1sdVp6RXNNQ29HQTFVRUN3d2pRWEJ3YkdVZ1YyOXliR1IzYVdSbElFUmxkbVZ
//        zYjNCbGNpQlNaV3hoZEdsdmJuTXhFekFSQmdOVkJBb01Da0Z3Y0d4bElFbHVZeTR4Q3pBSkJnTlZCQVlUQWxWV
//        E1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBcGMrQi9TV2lnVnZXaCswajJqTWN
//        qdUlqd0tYRUpzczl4cC9zU2cxVmh2K2tBdGVYeWpsVWJYMS9zbFFZbmNRc1VuR09aSHVDem9tNlNkWUk1YlNJY
//        2M4L1cwWXV4c1FkdUFPcFdLSUVQaUY0MWR1MzBJNFNqWU5NV3lwb041UEM4cjBleE5LaERFcFlVcXNTNCszZEg
//        1Z1ZrRFV0d3N3U3lvMUlnZmRZZUZScjZJd3hOaDlLQmd4SFZQTTNrTGl5a29sOVg2U0ZTdUhBbk9DNnBMdUNsM
//        lAwSzVQQi9UNXZ5c0gxUEttUFVockFKUXAyRHQ3K21mNy93bXYxVzE2c2MxRkpDRmFKekVPUXpJNkJBdENnbDd
//        aY3NhRnBhWWVRRUdnbUpqbTRIUkJ6c0FwZHhYUFEzM1k3MkMzWmlCN2o3QWZQNG83UTAvb21WWUh2NGdOSkl3S
//        URBUUFCbzRJQjF6Q0NBZE13UHdZSUt3WUJCUVVIQVFFRU16QXhNQzhHQ0NzR0FRVUZCekFCaGlOb2RIUndPaTh
//        2YjJOemNDNWhjSEJzWlM1amIyMHZiMk56Y0RBekxYZDNaSEl3TkRBZEJnTlZIUTRFRmdRVWthU2MvTVIydDUrZ
//        2l2Uk45WTgyWGUwckJJVXdEQVlEVlIwVEFRSC9CQUl3QURBZkJnTlZIU01FR0RBV2dCU0lKeGNKcWJZWVlJdnM
//        2N3IyUjFuRlVsU2p0ekNDQVI0R0ExVWRJQVNDQVJVd2dnRVJNSUlCRFFZS0tvWklodmRqWkFVR0FUQ0IvakNCd
//        3dZSUt3WUJCUVVIQWdJd2diWU1nYk5TWld4cFlXNWpaU0J2YmlCMGFHbHpJR05sY25ScFptbGpZWFJsSUdKNUl
//        HRnVlU0J3WVhKMGVTQmhjM04xYldWeklHRmpZMlZ3ZEdGdVkyVWdiMllnZEdobElIUm9aVzRnWVhCd2JHbGpZV
//        0pzWlNCemRHRnVaR0Z5WkNCMFpYSnRjeUJoYm1RZ1kyOXVaR2wwYVc5dWN5QnZaaUIxYzJVc0lHTmxjblJwWm1
//        sallYUmxJSEJ2YkdsamVTQmhibVFnWTJWeWRHbG1hV05oZEdsdmJpQndjbUZqZEdsalpTQnpkR0YwWlcxbGJuU
//        npMakEyQmdnckJnRUZCUWNDQVJZcWFIUjBjRG92TDNkM2R5NWhjSEJzWlM1amIyMHZZMlZ5ZEdsbWFXTmhkR1Z
//        oZFhSb2IzSnBkSGt2TUE0R0ExVWREd0VCL3dRRUF3SUhnREFRQmdvcWhraUc5Mk5rQmdzQkJBSUZBREFOQmdrc
//        WhraUc5dzBCQVFVRkFBT0NBUUVBRGFZYjB5NDk0MXNyQjI1Q2xtelQ2SXhETUlKZjRGelJqYjY5RDcwYS9DV1M
//        yNHlGdzRCWjMrUGkxeTRGRkt3TjI3YTQvdncxTG56THJSZHJqbjhmNUhlNXNXZVZ0Qk5lcGhtR2R2aGFJSlhuW
//        TR3UGMvem83Y1lmcnBuNFpVaGNvT0FvT3NBUU55MjVvQVE1SDNPNXlBWDk4dDUvR2lvcWJpc0IvS0FnWE5ucmZ
//        TZW1NL2oxbU9DK1JOdXhUR2Y4YmdwUHllSUdxTktYODZlT2ExR2lXb1IxWmRFV0JHTGp3Vi8xQ0tuUGFObVNBT
//        W5CakxQNGpRQmt1bGhnd0h5dmozWEthYmxiS3RZZGFHNllRdlZNcHpjWm04dzdISG9aUS9PamJiOUlZQVlNTnB
//        JcjdONFl0UkhhTFNQUWp2eWdhWndYRzU2QWV6bEhSVEJoTDhjVHFBPT0iOwoJInB1cmNoYXNlLWluZm8iID0gI
//        mV3b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGNITjBJaUE5SUNJeU1ESXhMVEV4TFRJMklESXl
//        PalU1T2pJd0lFRnRaWEpwWTJFdlRHOXpYMEZ1WjJWc1pYTWlPd29KSW5WdWFYRjFaUzFwWkdWdWRHbG1hV1Z5S
//        WlBOUlDSTROelU1WmpNMVlUTTNNMk0wTVRabU5qazRPVFJrWkRRd05XRTFOemhoTURoalpqSTVOMlkwSWpzS0N
//        TSnZjbWxuYVc1aGJDMTBjbUZ1YzJGamRHbHZiaTFwWkNJZ1BTQWlNVEF3TURBd01Ea3hPVE13TWpFeU5DSTdDZ
//        2tpWW5aeWN5SWdQU0FpTVRBd0lqc0tDU0owY21GdWMyRmpkR2x2YmkxcFpDSWdQU0FpTVRBd01EQXdNRGt4T1R
//        Nd01qRXlOQ0k3Q2draWNYVmhiblJwZEhraUlEMGdJakVpT3dvSkltbHVMV0Z3Y0MxdmQyNWxjbk5vYVhBdGRIb
//        HdaU0lnUFNBaVVGVlNRMGhCVTBWRUlqc0tDU0p2Y21sbmFXNWhiQzF3ZFhKamFHRnpaUzFrWVhSbExXMXpJaUE
//        5SUNJeE5qTTNPVGsyTXpZd01qYzNJanNLQ1NKMWJtbHhkV1V0ZG1WdVpHOXlMV2xrWlc1MGFXWnBaWElpSUQwZ
//        0lqaEJSRVJHUlRjMUxURkVRVVl0TkRVek1TMDRNakV3TFVKRE9UZzNSa1l3TlRrNU9DSTdDZ2tpY0hKdlpIVmp
//        kQzFwWkNJZ1BTQWlZMjl0TG1obFptVnBlSFZ1YkdsaGJ5NTZhR1Z1YkdsaGJ6TXdlWFZoYmlJN0Nna2lhWFJsY
//        lMxcFpDSWdQU0FpTVRVNU5qWXpNRGcyT0NJN0Nna2lZbWxrSWlBOUlDSmpiMjB1YUdWbVpXbDRkVzVzYVdGdkx
//        ucG9aVzVzYVdGdklqc0tDU0pwY3kxcGJpMXBiblJ5YnkxdlptWmxjaTF3WlhKcGIyUWlJRDBnSW1aaGJITmxJa
//        nNLQ1NKd2RYSmphR0Z6WlMxa1lYUmxMVzF6SWlBOUlDSXhOak0zT1RrMk16WXdNamMzSWpzS0NTSndkWEpqYUd
//        GelpTMWtZWFJsSWlBOUlDSXlNREl4TFRFeExUSTNJREEyT2pVNU9qSXdJRVYwWXk5SFRWUWlPd29KSW1sekxYU
//        nlhV0ZzTFhCbGNtbHZaQ0lnUFNBaVptRnNjMlVpT3dvSkluQjFjbU5vWVhObExXUmhkR1V0Y0hOMElpQTlJQ0l
//        5TURJeExURXhMVEkySURJeU9qVTVPakl3SUVGdFpYSnBZMkV2VEc5elgwRnVaMlZzWlhNaU93b0pJbTl5YVdkc
//        GJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVaUlEMGdJakl3TWpFdE1URXRNamNnTURZNk5UazZNakFnUlhSakwwZE5
//        WQ0k3Q24wPSI7CgkiZW52aXJvbm1lbnQiID0gIlNhbmRib3giOwoJInBvZCIgPSAiMTAwIjsKCSJzaWduaW5nL
//        XN0YXR1cyIgPSAiMCI7Cn0=";
//        byte[] decode = Base64.decode(code.getBytes());
//        System.out.println(new String(decode));
//    }

}