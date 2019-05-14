package com.github.z201;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.net.URL;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        startApp(stage);
    }

    public void startApp(Stage stage) throws Exception {
        this.primaryStage = stage;
        URL fxUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/MainUI.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxUrl);
        Parent root = fxmlLoader.load();
        // 设置标题
        primaryStage.setTitle("chat-tools");
        primaryStage.setScene(new Scene(root));
        primaryStage.initStyle(StageStyle.UTILITY);
        setMaximized();
        primaryStage.show();
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    // 设置窗口最大大小
    private void setMaximized() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(700);
        primaryStage.setHeight(600);
    }


    public static void main(String[] args) {
        String version = System.getProperty("java.version");
        if (Integer.parseInt(version.substring(2, 3)) >= 8 && Integer.parseInt(version.substring(6)) >= 60) {
            launch(args);
        } else {
            JFrame jFrame = new JFrame("版本错误");
            jFrame.setSize(500, 100);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JPanel jPanel = new JPanel();
            JLabel jLabel = new JLabel("JDK的版本不能低于1.8.0.60，请升级至最近的JDK 1.8再运行此软件");
            jPanel.add(jLabel);
            jFrame.add(jPanel);
            jFrame.setLocationRelativeTo(null);
            jFrame.setVisible(true);
        }
    }
}
