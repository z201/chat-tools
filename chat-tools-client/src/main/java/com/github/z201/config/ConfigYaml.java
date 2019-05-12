package com.github.z201.config;

import com.github.z201.core.GuiDict;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @author z201.coding@gmail.com
 */
@Data
public class ConfigYaml {

    private String privateKey;

    private Integer port;

    private String serverUrl;

    public final void initConfigYaml() {
        Yaml yaml = new Yaml();
        ConfigYaml autoConfigYaml = null;
        InputStream keyPath = ConfigYaml.class.getResourceAsStream(GuiDict.CONFIG_FILE_PATH);
        // loadAs 将yaml 文档转换成 javabeans
        autoConfigYaml = yaml.loadAs(keyPath, ConfigYaml.class);
        this.serverUrl = autoConfigYaml.getServerUrl();
        this.port = autoConfigYaml.getPort();
        System.out.println(autoConfigYaml.toString());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return gson.toJson(jsonObject);
    }
}
