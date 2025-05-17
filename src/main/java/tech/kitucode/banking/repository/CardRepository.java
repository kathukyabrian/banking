package tech.kitucode.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.kitucode.banking.domain.Card;
import tech.kitucode.banking.domain.enumerations.CardType;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findOneByPan(String pan);
    Optional<Card> findOneByCardTypeAndAccountId(CardType cardType, Long accountId);

    Integer countByAccountId(Long accountId);
}
