package tech.kitucode.banking.domain;

import jakarta.persistence.*;
import lombok.Data;
import tech.kitucode.banking.domain.enumerations.CardType;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tbl_cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(name = "card_alias")
    private String cardAlias;

    @Column(name = "account_id")
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Column(name = "pan")
    private String pan;

    @Column(name = "cvv")
    private String cvv;

    @Column(name = "created_on")
    private LocalDate createdOn;

    @Column(name = "updated_on")
    private LocalDate updatedOn;
}
