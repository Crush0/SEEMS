package cn.edu.just.ytc.seems.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应类，用于封装服务器返回的数据。
 * 提供了一种方便的方式来返回成功或错误响应，包括状态码、消息和数据。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R {
    private int code; // 响应的状态码
    private String msg; // 与响应相关的消息
    private Object data; // 响应的数据负载

    /**
     * 生成默认消息和空数据的成功响应。
     * @return 一个状态码为20000，消息为"success"，数据为null的R实例。
     */
    public static R ok() {
        return new R(20000, "success", null);
    }

    /**
     * 生成包含数据但默认消息的成功响应。
     * @param data 要包含在响应中的数据。
     * @return 一个状态码为20000，消息为"success"，数据为提供的数据的R实例。
     */
    public static R ok(Object data) {
        return new R(20000, "success", data);
    }

    /**
     * 生成包含自定义消息但无数据的成功响应。
     * @param message 要包含在响应中的自定义消息。
     * @return 一个状态码为20000，消息为自定义消息，数据为null的R实例。
     */
    public static R ok(String message) {
        return new R(20000, message, null);
    }

    /**
     * 生成包含自定义消息和数据的成功响应。
     * @param message 要包含在响应中的自定义消息。
     * @param data 要包含在响应中的数据。
     * @return 一个状态码为20000，消息为自定义消息，数据为提供的数据的R实例。
     */
    public static R ok(String message, Object data) {
        return new R(20000, message, data);
    }

    /**
     * 生成默认消息和空数据的错误响应。
     * @return 一个状态码为50000，消息为"error"，数据为null的R实例。
     */
    public static R error() {
        return new R(50000, "error", null);
    }

    /**
     * 生成包含自定义消息但无数据的错误响应。
     * @param message 要包含在响应中的自定义消息。
     * @return 一个状态码为50000，消息为自定义消息，数据为null的R实例。
     */
    public static R error(String message) {
        return new R(50000, message, null);
    }

    /**
     * 生成包含自定义状态码和消息但无数据的错误响应。
     * @param status 要包含在响应中的自定义状态码。
     * @param message 要包含在响应中的自定义消息。
     * @return 一个状态码为提供的状态码，消息为自定义消息，数据为null的R实例。
     */
    public static R error(int status, String message) {
        return new R(status, message, null);
    }
}
