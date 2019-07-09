package com.example.evideo.Bean;

import com.google.gson.annotations.SerializedName;

public class PostVideoResponse {

    @SerializedName("result")
    private Object result;
    @SerializedName("url")
    private String url;
    @SerializedName("success")
    private boolean success;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
