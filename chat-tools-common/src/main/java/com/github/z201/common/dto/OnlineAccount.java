package com.github.z201.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author z201.coding@gmail.com
 **/
@Data
@Builder
public class OnlineAccount {

    Set<String> onlineAccount = new HashSet<>();

}
