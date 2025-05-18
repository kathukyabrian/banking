package tech.kitucode.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import tech.kitucode.banking.ApplicationProperties;
import tech.kitucode.banking.domain.Card;
import tech.kitucode.banking.domain.enumerations.CardType;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.CardRepository;
import tech.kitucode.banking.service.dto.CreateCardDTO;
import tech.kitucode.banking.service.dto.UpdateCardDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private CardService cardService;

    @Test
    void testInvalidCreateCardRequest() {
        CreateCardDTO withoutCardType = new CreateCardDTO();
        withoutCardType.setCardType(null);
        withoutCardType.setAccountId(1L);
        withoutCardType.setCardAlias("Brian Kitunda Kathukya");

        CreateCardDTO withoutAccountId = new CreateCardDTO();
        withoutAccountId.setCardType(CardType.VIRTUAL);
        withoutAccountId.setAccountId(null);
        withoutAccountId.setCardAlias("Brian Kitunda Kathukya");

        CreateCardDTO withoutCardAlias = new CreateCardDTO();
        withoutCardAlias.setCardType(CardType.VIRTUAL);
        withoutCardAlias.setAccountId(1L);
        withoutCardAlias.setCardAlias(null);

        assertThrows(ValidationException.class, () -> {
            cardService.save(withoutCardType);
        });

        assertThrows(ValidationException.class, () -> {
            cardService.save(withoutAccountId);
        });

        assertThrows(ValidationException.class, () -> {
            cardService.save(withoutCardAlias);
        });
    }

    @Test
    void testCardExistsByAccountIdAndCardType() {
        CreateCardDTO createCardDTO = new CreateCardDTO();
        createCardDTO.setCardType(CardType.VIRTUAL);
        createCardDTO.setAccountId(1L);
        createCardDTO.setCardAlias("Brian Kitunda Kathukya");

        Card cardByTypeAndAccountId = new Card();
        cardByTypeAndAccountId.setCardId(1L);
        cardByTypeAndAccountId.setAccountId(createCardDTO.getAccountId());
        cardByTypeAndAccountId.setCardType(createCardDTO.getCardType());
        cardByTypeAndAccountId.setCardAlias(createCardDTO.getCardAlias());

        when(cardRepository.findOneByCardTypeAndAccountId(createCardDTO.getCardType(), createCardDTO.getAccountId()))
                .thenReturn(Optional.of(cardByTypeAndAccountId));

        assertThrows(ValidationException.class, () -> {
            cardService.save(createCardDTO);
        });
    }

    @Test
    void testAccountCardNumberCapReached() {
        CreateCardDTO createCardDTO = new CreateCardDTO();
        createCardDTO.setCardType(CardType.VIRTUAL);
        createCardDTO.setAccountId(1L);
        createCardDTO.setCardAlias("Brian Kitunda Kathukya");

        when(applicationProperties.getMaxCardsPerAccount()).thenReturn(2);
        when(cardRepository.countByAccountId(createCardDTO.getAccountId())).thenReturn(2);

        assertThrows(ValidationException.class, () -> {
            cardService.save(createCardDTO);
        });
    }

    @Test
    void testSuccessfulCreateCard() {
        Card mockCard = new Card();
        mockCard.setCardAlias("Brian Kitunda Kathukya");
        mockCard.setPan("1882738818773737");
        mockCard.setCvv("390");
        mockCard.setAccountId(1L);
        mockCard.setCardType(CardType.VIRTUAL);
        mockCard.setCreatedOn(LocalDate.now());

        when(cardRepository.save(mockCard)).thenReturn(mockCard);

        Card card = cardService.save(mockCard);
        assert card != null;
        assertEquals(card.getCardAlias(), "Brian Kitunda Kathukya");
        assertEquals(card.getPan(), "188273******3737");
        assertEquals(card.getCvv(), "***");
        assertEquals(card.getAccountId(), 1L);
        assertEquals(card.getCardType(), CardType.VIRTUAL);
        assertEquals(card.getCreatedOn(), LocalDate.now());
    }

    @Test
    void testGetAllCards() {
        Card card1 = new Card();
        card1.setCardId(1L);
        card1.setCardAlias("Brian Kitunda Kathukya");
        card1.setPan("1882738818773737");
        card1.setCvv("390");
        card1.setAccountId(1L);
        card1.setCardType(CardType.VIRTUAL);
        card1.setCreatedOn(LocalDate.now());

        Card card2 = new Card();
        card2.setCardId(2L);
        card2.setCardAlias("Brian Kitunda Kathukya");
        card2.setPan("7474586876274757");
        card2.setCvv("789");
        card2.setAccountId(1L);
        card2.setCardType(CardType.PHYSICAL);
        card2.setCreatedOn(LocalDate.now());


        Page page = new PageImpl(List.of(card1, card2));

        Card probe = new Card();
        probe.setCardType(null);
        probe.setCardAlias(null);
        probe.setPan(null);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        when(cardRepository.findAll(Example.of(probe, matcher), Pageable.unpaged())).thenReturn(page);

        Page<Card> cards = cardService.findAll(probe.getCardAlias(), probe.getCardType(), probe.getPan(), true, Pageable.unpaged());

        assert !cards.getContent().isEmpty();
        assert cards.getContent().size() == 2;
    }

    @Test
    void testFindOneById() {
        Card mockCard = new Card();
        mockCard.setCardId(1L);
        mockCard.setCardAlias("Brian Kitunda Kathukya");
        mockCard.setPan("1882738818773737");
        mockCard.setCvv("390");
        mockCard.setAccountId(1L);
        mockCard.setCardType(CardType.VIRTUAL);
        mockCard.setCreatedOn(LocalDate.now());

        when(cardRepository.findById(mockCard.getCardId())).thenReturn(Optional.of(mockCard));

        Card card = cardService.findById(mockCard.getCardId(), true);

        assert card != null;
        assertEquals(card.getCardId(), mockCard.getCardId());
    }

    @Test
    void testUpdateNonExistentCard() {
        UpdateCardDTO updateCardDTO = new UpdateCardDTO();
        updateCardDTO.setCardAlias("Brian Kitush");
        updateCardDTO.setCardId(100L);

        when(cardRepository.findById(updateCardDTO.getCardId())).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            cardService.update(updateCardDTO);
        });
    }
}
