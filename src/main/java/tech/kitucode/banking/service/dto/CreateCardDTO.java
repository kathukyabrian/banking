package tech.kitucode.banking.service.dto;

import lombok.Data;
import tech.kitucode.banking.domain.enumerations.CardType;

@Data
public class CreateCardDTO {
    private CardType cardType;
    private Long accountId;
    private String cardAlias;
}
