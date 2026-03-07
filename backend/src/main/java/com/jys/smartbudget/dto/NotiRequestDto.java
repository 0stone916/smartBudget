package com.jys.smartbudget.dto;

import java.time.LocalDateTime;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotiRequestDto {
    private String userId; 
    private String accountNumber;
    private Long amount;
    private String merchantName;
    private String approvalNo;    
    private LocalDateTime transactedAt;
    private Integer year;
    private Integer month;
    private Integer day;

}