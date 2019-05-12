package com.github.z201.core;

import com.github.z201.config.ConfigYaml;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @author z201.coding@gmail.com
 */
public class GuiContext {

    public static volatile GuiContext instance = null;

    private GuiContext() {

    }

    public static GuiContext getInstance() {
        if (null == instance) {
            synchronized (GuiContext.class) {
                if (null == instance) {
                    instance = new GuiContext();
                    instance.initConfigYaml();
                }
            }
        }
        return instance;
    }

    private ConfigYaml autoConfigYaml;

    private final void initConfigYaml() {
        Yaml yaml = new Yaml();
        InputStream keyPath = GuiContext.class.getResourceAsStream(GuiDict.CONFIG_FILE_PATH);
        // loadAs 将yaml 文档转换成 javabeans
        autoConfigYaml = yaml.loadAs(keyPath, ConfigYaml.class);
    }

    // 监控程序运行状态
    public GuiDict.State state = GuiDict.State.断开连接;

    public void refreshButton() {
        if (this.state.equals(GuiDict.State.断开连接)) {
            
        } else if (this.state.equals(GuiDict.State.启动连接)) {

        }else {
            throw new RuntimeException("刷新按钮出现问题");
        }
    }

    public GuiDict.State getState() {
        return state;
    }

    public void setState(GuiDict.State state) {
        this.state = state;
    }


}
