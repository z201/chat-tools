package com.github.z201.hander;

import com.github.z201.Controller;
import com.github.z201.common.SyncFuture;
import com.github.z201.common.dto.Message;
import com.github.z201.common.dto.OnlineAccount;
import com.github.z201.common.json.Serializer;
import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.common.protocol.ProtocolHeader;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author z201.coding@gmail.com
 **/
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private static volatile ClientHandler single = null;

    private volatile SyncFuture syncFuture;

    private Controller controller;

    private ClientHandler(SyncFuture syncFuture, Controller controller) {
        this.syncFuture = syncFuture;
        this.controller = controller;
    }

    public static ClientHandler getInstance(SyncFuture syncFuture, Controller controller) {
        if (null == single) {
            synchronized (ClientHandler.class) {
                if (null == single) {
                    single = new ClientHandler(syncFuture, controller);
                }
            }
        }
        return single;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("客户端发出数据");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MessageHolder) {
            MessageHolder messageHolder = (MessageHolder) msg;
            messageHolder.setChannel(ctx.channel());
            if (logger.isDebugEnabled()) {
                logger.debug("response body {} ", messageHolder.getBody());
            }
            if (messageHolder.getSign() == ProtocolHeader.NOTICE) {
                if (messageHolder.getType() == ProtocolHeader.ALL_MESSAGE) {
                    logger.info("ALL_MESSAGE body {} ", messageHolder.getBody());
                    Type type = new TypeToken<List<Message>>() {}.getType();
                    List<Message> messageList = Serializer.getGson().fromJson(messageHolder.getBody(), type);
                    for(Message message : messageList){
                        controller.consoleMsg(message);
                    }
                }else if(messageHolder.getType() == ProtocolHeader.ONLINE_USER_LIST){
                    logger.info("ONLINE_USER_LIST body {} ", messageHolder.getBody());
                    OnlineAccount onlineAccount = Serializer.deserialize(messageHolder.getBody(), OnlineAccount.class);
                    // 更新在线列表
                    controller.loadLeftTree(onlineAccount.getOnlineAccount());
                }
            } else {
                syncFuture.setResponse(messageHolder);
            }
        }
    }

}
