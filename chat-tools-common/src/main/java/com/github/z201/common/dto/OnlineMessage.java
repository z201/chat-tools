package com.github.z201.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author z201.coding@gmail.com
 **/
@Data
@Builder
public class OnlineMessage {

    List<Message> messageList = new ArrayList<>();
}
