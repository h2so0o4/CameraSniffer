package com.wificamera.sniffer.common.model;

public class LoginJson {

    /**
     * code : 0
     * data : {"uid":"7","jwtToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aWZpY2FtZXJhIiwicGFzc3dvcmQiOiIwMDExMDQiLCJpZCI6NywiZXhwIjoxNTg4NDgzNzcwLCJpYXQiOjE1ODg0NzM3NzAsImp0aSI6ImE2ZTI2MGM1LTAwZmQtNDMzZi04ZTA5LWU0MTBjNzM1MDA3NCIsInVzZXJuYW1lIjoibHRxIn0.g_VkqhHoZyuVuswjUe1rJeSaOjWVtT3imIQAuXoZPE0"}
     * success : true
     */

    private int code;
    private DataBean data;
    private boolean success;
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class DataBean {
        /**
         * uid : 7
         * jwtToken : eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aWZpY2FtZXJhIiwicGFzc3dvcmQiOiIwMDExMDQiLCJpZCI6NywiZXhwIjoxNTg4NDgzNzcwLCJpYXQiOjE1ODg0NzM3NzAsImp0aSI6ImE2ZTI2MGM1LTAwZmQtNDMzZi04ZTA5LWU0MTBjNzM1MDA3NCIsInVzZXJuYW1lIjoibHRxIn0.g_VkqhHoZyuVuswjUe1rJeSaOjWVtT3imIQAuXoZPE0
         */

        private String uid;
        private String jwtToken;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getJwtToken() {
            return jwtToken;
        }

        public void setJwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
        }
    }
}
