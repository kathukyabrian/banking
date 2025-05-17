package tech.kitucode.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.kitucode.banking.ApplicationProperties;
import tech.kitucode.banking.domain.Card;
import tech.kitucode.banking.domain.enumerations.CardType;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.CardRepository;
import tech.kitucode.banking.service.dto.CreateCardDTO;
import tech.kitucode.banking.service.dto.UpdateCardDTO;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class CardService {
    private final Random random = new Random();
    private final CardRepository cardRepository;
    private final ApplicationProperties applicationProperties;

    public CardService(CardRepository cardRepository, ApplicationProperties applicationProperties) {
        this.cardRepository = cardRepository;
        this.applicationProperties = applicationProperties;
    }

    public Card save(CreateCardDTO createCardDTO) {
        log.debug("Request to create card : {}", createCardDTO);

        validateCardCreationRequest(createCardDTO);

        Optional<Card> cardByCardTypeAndAccountId = cardRepository.findOneByCardTypeAndAccountId(createCardDTO.getCardType(), createCardDTO.getAccountId());
        if (cardByCardTypeAndAccountId.isPresent()) {
            throw new ValidationException("Card of type: " + createCardDTO.getCardType() + " and account id: " + createCardDTO.getAccountId() + " already exists");
        }

        // proceed to create card
        Integer cardsByAccountCount = cardRepository.countByAccountId(createCardDTO.getAccountId());
        if (cardsByAccountCount.equals(applicationProperties.getMaxCardsPerAccount())) {
            throw new ValidationException("A maximum of " + applicationProperties.getMaxCardsPerAccount() + " accounts is allowed for each account.");
        }

        Card card = new Card();
        card.setCardAlias(createCardDTO.getCardAlias());
        card.setAccountId(createCardDTO.getAccountId());
        card.setCardType(createCardDTO.getCardType());

        card.setPan(generatePAN());
        card.setCvv(generateCVV());
        card.setCreatedOn(LocalDate.now());

        return maskCardDetails(cardRepository.save(card));
    }

    public Page<Card> findAll(String cardAlias, CardType cardType, String pan, Boolean masked, Pageable pageable) {
        log.debug("Request to find cards by cardAlias: {}, cardType: {}, pan: {}", cardAlias, cardType, pan);

        Card probe = getProbe(cardAlias, cardType, pan);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        Example<Card> example = Example.of(probe, matcher);

        Page<Card> page = cardRepository.findAll(example, pageable);
        if (masked) {
            page.getContent().forEach(this::maskCardDetails);
        }

        return page;
    }

    public Card findById(Long id, Boolean masked) {
        log.debug("Request to find card by id : {}", id);

        Card card = cardRepository.findById(id).orElse(null);

        if (masked) {
            return maskCardDetails(card);
        }

        return card;
    }

    public Card update(UpdateCardDTO cardUpdateDTO) {
        log.debug("Request to update card: {}", cardUpdateDTO);

        Card card = findById(cardUpdateDTO.getCardId(), false);

        if (card == null) {
            throw new ValidationException("Card with id " + cardUpdateDTO.getCardId() + " not found");
        }

        if (cardUpdateDTO.getCardAlias() != null) {
            card.setCardAlias(cardUpdateDTO.getCardAlias());
            card.setUpdatedOn(LocalDate.now());
            return maskCardDetails(cardRepository.save(card));
        }

        return maskCardDetails(card);
    }

    public void delete(Long id) {
        log.debug("Request to delete card by id : {}", id);
        cardRepository.deleteById(id);
    }


    private void validateCardCreationRequest(CreateCardDTO createCardDTO) {
        if (createCardDTO.getAccountId() == null) {
            throw new ValidationException("Account id is required");
        }

        if (createCardDTO.getCardAlias() == null || createCardDTO.getCardAlias().isEmpty()) {
            throw new ValidationException("Card alias is required");
        }

        if (createCardDTO.getCardType() == null) {
            throw new ValidationException("Card Type is required");
        }
    }

    private String generateCVV() {
        // create a random 3 digit number
        int cvv = random.nextInt(100, 999);

        return String.valueOf(cvv);
    }

    private String generatePAN() {
        // create a random 16 digit number
        long pan = random.nextLong(1000000000000000L, 9999999999999999L);

        return String.valueOf(pan);
    }

    private Card getProbe(String cardAlias, CardType cardType, String pan) {
        Card card = new Card();

        if (cardAlias != null && !cardAlias.isEmpty()) {
            card.setCardAlias(cardAlias);
        }

        if (cardType != null) {
            card.setCardType(cardType);
        }

        if (pan != null && !pan.isEmpty()) {
            card.setPan(pan);
        }

        return card;
    }

    private Card maskCardDetails(Card card) {
        if (card == null) {
            return null;
        }

        card.setCvv("***");
        card.setPan(maskPan(card));
        return card;
    }

    private String maskPan(Card card) {
        String pan = card.getPan();

        String firstSix = pan.substring(0, 6);
        String lastFour = pan.substring(pan.length() - 4);
        String maskedMiddle = "*".repeat(pan.length() - 10);

        return firstSix + maskedMiddle + lastFour;
    }
}
