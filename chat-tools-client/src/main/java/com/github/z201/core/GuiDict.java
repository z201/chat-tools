package com.github.z201.core;

public interface GuiDict {

    String CONFIG_FILE_PATH = "/config/config.yml";

    enum State {

        启动连接(0, "联网成功"),
        断开连接(1, "断开连接");

        public final int code;
        public final String desc;

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        State(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

}
