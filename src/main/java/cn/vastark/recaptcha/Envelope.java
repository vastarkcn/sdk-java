package cn.vastark.recaptcha;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 统一响应信封
 */
public class Envelope {
    private int code;
    private String message;
    private Map<String, Object> data;

    public Envelope() {
    }

    public Envelope(int code, String message, Map<String, Object> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
