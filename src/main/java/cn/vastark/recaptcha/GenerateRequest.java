package cn.vastark.recaptcha;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成验证码请求
 */
public class GenerateRequest {
    private final String siteId;
    private final String type;
    private final int width;
    private final Map<String, Object> extra;

    /**
     * @param siteId 站点 ID
     */
    public GenerateRequest(String siteId) {
        this(siteId, "", 0, null);
    }

    /**
     * @param siteId 站点 ID
     * @param type   验证码类型（可选，如 "text"）
     * @param width  宽度（可选）
     * @param extra  扩展字段
     */
    public GenerateRequest(String siteId, String type, int width, Map<String, Object> extra) {
        this.siteId = siteId;
        this.type = type != null ? type : "";
        this.width = width;
        this.extra = extra != null ? new HashMap<>(extra) : new HashMap<String, Object>();
    }

    public String getSiteId() {
        return siteId;
    }

    public String getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }
}
