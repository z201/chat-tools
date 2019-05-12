package com.github.z201.common.protocol;

import io.netty.channel.Channel;
import lombok.*;

/**
 * 消息载体.
 *
 * 传输模块与服务模块之间双向数据传输载体:
 *
 *                   MessageHolder
 * Service Module <----------------> Transport Module
 *
 * @author z201.coding@gmail.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageHolder {

    // 消息标志
    private byte sign;
    // 消息类型
    private byte type;
    // 响应状态
    private byte status;
    // Json消息体
    private String body;
    // 接收到消息的通道
    private Channel channel;

}
