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
public class Account {
    private String username;
    private String password;
    private Long token;
}
