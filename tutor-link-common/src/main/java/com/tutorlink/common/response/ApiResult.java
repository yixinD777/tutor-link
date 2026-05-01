package com.tutorlink.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tutorlink.common.constant.ResultCode;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    private ApiResult() {}

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> ApiResult<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }

    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
