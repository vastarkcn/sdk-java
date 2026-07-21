package cn.vastark.recaptcha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 二次校验请求
 */
public class VerifyRequest {
    private final String captchaId;
    private final String verifyTicket;
    private final List<Map<String, Integer>> clicks;
    private final Map<String, Object> extra;

    /**
     * @param captchaId    验证码 ID
     * @param verifyTicket 验证票据
     * @param clicks       点击坐标列表 [{"x": int, "y": int}, ...]
     */
    public VerifyRequest(String captchaId, String verifyTicket, List<Map<String, Integer>> clicks) {
        this(captchaId, verifyTicket, clicks, null);
    }

    /**
     * @param captchaId    验证码 ID
     * @param verifyTicket 验证票据
     * @param clicks       点击坐标列表 [{"x": int, "y": int}, ...]
     * @param extra        扩展字段
     */
    public VerifyRequest(String captchaId, String verifyTicket, List<Map<String, Integer>> clicks, Map<String, Object> extra) {
        this.captchaId = captchaId;
        this.verifyTicket = verifyTicket;
        this.clicks = clicks;
        this.extra = extra != null ? new HashMap<>(extra) : new HashMap<String, Object>();
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public String getVerifyTicket() {
        return verifyTicket;
    }

    public List<Map<String, Integer>> getClicks() {
        return clicks;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }
}
