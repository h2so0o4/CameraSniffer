package com.wificamera.sniffer.common.model;

import java.util.List;

public class LocJson {

    /**
     * code : 0
     * data : {"total":2,"list":[{"id":24,"uid":7,"longitude":113.879705,"latitude":35.302494,"state":0,"createTime":"2020-05-07T16:18:07.000+0000","updateTime":"2020-05-07T16:18:07.000+0000"},{"id":25,"uid":7,"longitude":123,"latitude":123,"state":0,"createTime":"2020-05-08T02:39:32.000+0000","updateTime":"2020-05-08T02:39:32.000+0000","remark":"test"}],"pageNum":1,"pageSize":50,"size":2,"startRow":1,"endRow":2,"pages":1,"prePage":0,"nextPage":0,"isFirstPage":true,"isLastPage":true,"hasPreviousPage":false,"hasNextPage":false,"navigatePages":8,"navigatepageNums":[1],"navigateFirstPage":1,"navigateLastPage":1}
     * success : true
     */

    private int code;
    private DataBean data;
    private boolean success;

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
         * total : 2
         * list : [{"id":24,"uid":7,"longitude":113.879705,"latitude":35.302494,"state":0,"createTime":"2020-05-07T16:18:07.000+0000","updateTime":"2020-05-07T16:18:07.000+0000"},{"id":25,"uid":7,"longitude":123,"latitude":123,"state":0,"createTime":"2020-05-08T02:39:32.000+0000","updateTime":"2020-05-08T02:39:32.000+0000","remark":"test"}]
         * pageNum : 1
         * pageSize : 50
         * size : 2
         * startRow : 1
         * endRow : 2
         * pages : 1
         * prePage : 0
         * nextPage : 0
         * isFirstPage : true
         * isLastPage : true
         * hasPreviousPage : false
         * hasNextPage : false
         * navigatePages : 8
         * navigatepageNums : [1]
         * navigateFirstPage : 1
         * navigateLastPage : 1
         */

        private int total;
        private List<ListBean> list;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : 24
             * uid : 7
             * longitude : 113.879705
             * latitude : 35.302494
             * state : 0
             * createTime : 2020-05-07T16:18:07.000+0000
             * updateTime : 2020-05-07T16:18:07.000+0000
             * remark : test
             */

            private int id;
            private int uid;
            private double longitude;
            private double latitude;
            private String remark;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getUid() {
                return uid;
            }

            public void setUid(int uid) {
                this.uid = uid;
            }

            public double getLongitude() {
                return longitude;
            }

            public void setLongitude(double longitude) {
                this.longitude = longitude;
            }

            public double getLatitude() {
                return latitude;
            }

            public void setLatitude(double latitude) {
                this.latitude = latitude;
            }

            public String getRemark(){
                return remark;
            }
        }
    }
}
