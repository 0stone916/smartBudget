package com.jys.smartbudget.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;                // PK

    private String userId;              

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;    

    @NotBlank(message = "승인번호는 필수입니다.")
    private String approvalNo;       

    @NotBlank(message = "가맹점명은 필수입니다.")
    private String merchantName;    

    @NotNull(message = "지출 금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0원 이상이어야 합니다.")
    private Long amount;            

    private Integer year;

    private Integer month;

    private Integer day;

    private String status;           // 처리 상태 (SUCCESS, PENDING 등)
    
    private LocalDateTime transactedAt; // 실제 결제 일시

}
