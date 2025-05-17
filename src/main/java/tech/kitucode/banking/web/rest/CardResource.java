package tech.kitucode.banking.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.kitucode.banking.domain.Card;
import tech.kitucode.banking.domain.enumerations.CardType;
import tech.kitucode.banking.error.EntityNotFoundException;
import tech.kitucode.banking.service.CardService;
import tech.kitucode.banking.service.dto.CreateCardDTO;
import tech.kitucode.banking.service.dto.UpdateCardDTO;
import tech.kitucode.banking.web.util.PaginationUtil;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class CardResource {
    private final String BASE_URL = "/api/cards";
    private final CardService cardService;

    public CardResource(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/cards")
    public ResponseEntity<Card> create(@RequestBody CreateCardDTO createCardDTO) {
        log.info("REST request to create card: {}", createCardDTO);

        Card savedCard = cardService.save(createCardDTO);

        return ResponseEntity.created(URI.create(BASE_URL + "/" + savedCard.getCardId())).body(savedCard);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<Card>> findAll(
            @RequestParam(name = "cardAlias", required = false) String cardAlias,
            @RequestParam(name = "cardType", required = false) CardType cardType,
            @RequestParam(name = "pan", required = false) String pan,
            @RequestParam(name = "masked", required = false) Boolean masked,
            Pageable pageable
    ) {
        log.info("REST request to find cards by cardAlias: {}, cardType: {}, pan: {}", cardAlias, cardType, pan);

        if (masked == null) {
            masked = true;
        }

        Page page = cardService.findAll(cardAlias, cardType, pan, masked, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, BASE_URL);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<Card> findById(@PathVariable Long id, @RequestParam(name = "masked", required = false) Boolean masked) {
        log.info("REST request to find card by id : {}", id);

        if (masked == null) {
            masked = true;
        }

        Card card = cardService.findById(id, masked);

        if (card == null) {
            throw new EntityNotFoundException("Card with id: " + id + " does not exist");
        }

        return ResponseEntity.ok(card);
    }

    @PutMapping("/cards")
    public ResponseEntity<Card> update(@RequestBody UpdateCardDTO updateCardDTO) {
        log.info("REST request to update card : {}", updateCardDTO);

        Card updatedCard = cardService.update(updateCardDTO);

        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("REST request to delete card with id: {}", id);

        cardService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
