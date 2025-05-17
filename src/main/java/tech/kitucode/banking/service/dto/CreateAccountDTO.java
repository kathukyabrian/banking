package tech.kitucode.banking.service.dto;

import lombok.Data;

@Data
public class CreateAccountDTO {
    private Long customerId;
    private String branchCode;
}
