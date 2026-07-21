package cn.vastark.recaptcha.examples;

import cn.vastark.recaptcha.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方应用接入示例
 */
public class ThirdPartyApp {
    public static void main(String[] args) {
        Client client = new Client(new Config(
            "http://127.0.0.1",
            "replace-with-api-key",
            3000,
            FailureMode.STRICT_BLOCK,
            null
        ));

        Map<String, Object> extra = new HashMap<>();
        extra.put("device_fingerprint", "fp_example_1234567890abcdef");

        Envelope resp = client.generate(new GenerateRequest(
            "partner-site-a",
            "text",
            320,
            extra
        ));

        System.out.println("captcha_id: " + resp.getData().get("captcha_id"));
        System.out.println("verify_ticket: " + resp.getData().get("verify_ticket"));
    }
}
