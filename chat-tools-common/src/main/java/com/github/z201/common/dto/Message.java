
package com.github.z201.common.dto;

import lombok.*;

/**
 * @author z201.coding@gmail.com
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message {
    private String sender;
    private String receiver;
    private String content;
    private Long time;

}
