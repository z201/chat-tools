package com.github.z201;


import com.github.z201.client.ClientLauncher;
import com.github.z201.common.MsgTools;
import com.github.z201.config.ConfigYaml;
import com.github.z201.core.GuiContext;
import com.github.z201.core.GuiDict;
import com.github.z201.core.ViewI;
import com.github.z201.common.dto.Account;
import com.github.z201.common.dto.Message;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.ProtocolHeader;
import com.github.z201.utils.KeyUtils;
import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author z201
 */
public class Controller implements Initializable, ViewI {


    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private GuiContext guiContext;

    /**
     * 退出
     */
    @FXML
    private Button exit;

    /**
     * 联网
     */
    @FXML
    private ToggleButton monitoring;

    /**
     * 在线客户端信息
     */
    @FXML
    private TreeView<String> onlineUserTableView;
    /**
     * 聊天信息
     */
    @FXML
    private TextArea textArea;

    @FXML
    private TextField username;

    @FXML
    private TextField password;
    /**
     * 输入栏
     */
    @FXML
    private TextArea speak;

    @FXML
    private Button sendMsg;

    @FXML
    private Text text;

    private Account account;

    private Channel channel;

    @Override
    public void initView() throws Exception {
        text.setStyle("-fx-font: 15 arial; -fx-base: #CD0000;");
        monitoring.setStyle("-fx-font: 18 arial; -fx-base: #F0FFFF;");
        textArea.setStyle("-fx-font: 13 arial; -fx-base: #00FA9A;");
        guiContext = GuiContext.getInstance();
        refreshButton();
    }

    @Override
    public void initEvent() throws Exception {
        onlineUserTableView.setShowRoot(false);
        onlineUserTableView.setRoot(new TreeItem<>());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initView();
            initEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void exitAction() {
        System.exit(0);
    }

    public final void cleanConsoleAction(ActionEvent actionEvent) {
        textArea.clear();
    }

    public final void sendMsg(ActionEvent actionEvent) {
        String msg = speak.getText();
        if (null != msg && !msg.trim().isEmpty()) {
            msg = KeyUtils.stripXSS(msg);
            Message message = new Message(account.getUsername(), "", msg, Instant.now().toEpochMilli());
            MsgTools.request(channel, ProtocolHeader.ALL_MESSAGE, Serializer.serialize(message));
            speak.clear();
        }
    }

    // Runs task in JavaFX Thread
    public void runActionLater(final Runnable runnable) {
        // 判断线程是不是JavaFx线程
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            try {
                Platform.runLater(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                refreshStateInfo();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final void monitoringAction(ActionEvent actionEvent) {
        if (guiContext.state.equals(GuiDict.State.断开连接)) {
            ConfigYaml configYaml = new ConfigYaml();
            configYaml.initConfigYaml();
            Account account = Account.builder().username(username.getText()).password(password.getText()).build();
            try {
                new ClientLauncher(this, configYaml, account).run();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        } else {
            guiContext.setState(GuiDict.State.断开连接);
            console("断开连接...");
            monitoring.setText("联网");
            textArea.clear();
        }
        refreshButton();
    }

    public final void console(String msg) {
        if (null != msg && !msg.trim().equals("") && msg.length() != 0) {
            // 防止控制台输出过多，定期清理
            if (textArea.getLength() > 2000) {
                textArea.clear();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
            String format = formatter.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
            String str = format + " | %s \n";
            textArea.appendText(String.format(str, msg));
        }
    }

    public final void consoleMsg(Message msg) {
        // 防止控制台输出过多，定期清理
        if (textArea.getLength() > 2000) {
            textArea.clear();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        String format = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(msg.getTime()), ZoneId.systemDefault()));
        String str = "\n" + format + "  " + msg.getSender() + " | \n %s ";
        textArea.appendText(String.format(str, msg.getContent()));
    }

    public final void changeStateSelected(Boolean change) {
        monitoring.setSelected(change);
        monitoring.setDisable(true);
    }

    private void refreshButton() {
        if (null == guiContext) {
            logger.error("guiContext 初始化异常。。。");
        }
        guiContext.refreshButton();
        refreshStateInfo();
    }

    private void refreshStateInfo() {
        text.setText("状态: " + guiContext.getState().getDesc());
        if (guiContext.getState() == GuiDict.State.断开连接) {
            monitoring.setSelected(false);
            sendMsg.setDisable(true);
        } else if (guiContext.getState() == GuiDict.State.启动连接) {
            monitoring.setSelected(true);
            sendMsg.setDisable(false);
        }
    }

    public GuiContext getGuiContext() {
        return guiContext;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void loadLeftTree(Set<String> users) {
        TreeItem rootTreeItem = onlineUserTableView.getRoot();
        rootTreeItem.getChildren().clear();
        for (String user : users) {
            TreeItem<String> treeItem = new TreeItem<>();
            treeItem.setValue(user);
//            ImageView dbImage = new ImageView("icons/computer.png");
//            dbImage.setFitHeight(16);
//            dbImage.setFitWidth(16);
//            treeItem.setGraphic(dbImage);
            rootTreeItem.getChildren().add(treeItem);
        }

    }
}
