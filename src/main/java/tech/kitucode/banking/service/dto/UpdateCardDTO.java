package tech.kitucode.banking.service.dto;

import lombok.Data;

@Data
public class UpdateCardDTO {
    private Long cardId;
    private String cardAlias;
}
