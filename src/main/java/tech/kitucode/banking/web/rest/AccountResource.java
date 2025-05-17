package tech.kitucode.banking.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.kitucode.banking.domain.Account;
import tech.kitucode.banking.error.EntityNotFoundException;
import tech.kitucode.banking.service.AccountService;
import tech.kitucode.banking.service.dto.CreateAccountDTO;
import tech.kitucode.banking.web.util.PaginationUtil;
import tech.kitucode.banking.web.vm.ErrorResponse;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class AccountResource {
    private final String BASE_URL = "/api/accounts";
    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> create(@RequestBody CreateAccountDTO createAccountDTO) {
        log.info("REST request to save account: {}", createAccountDTO);

        Account savedAccount = accountService.save(createAccountDTO);

        return ResponseEntity.created(URI.create(BASE_URL + "/" + savedAccount.getAccountId())).body(savedAccount);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> findAll(
            @RequestParam(name = "iban", required = false) String iban,
            @RequestParam(name = "bicSwift", required = false) String bicSwift,
            @RequestParam(name = "accountId", required = false) Long accountId,
            Pageable pageable
    ) {
        log.info("REST request to find accounts by iban: {}, bicSwift: {}, accountId: {}", iban, bicSwift, accountId);

        Page page = accountService.findAll(iban, bicSwift, accountId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, BASE_URL);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> findById(@PathVariable Long id) {
        log.info("REST request to find account by id : {}", id);

        Account account = accountService.findById(id);

        if (account == null) {
            throw new EntityNotFoundException("Account with id: " + id + " does not exist");
        }

        return ResponseEntity.ok(account);
    }

    @PutMapping("/accounts")
    public ResponseEntity<ErrorResponse> update(@RequestBody Account account) {
        log.info("REST request to update account : {}", account);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_IMPLEMENTED.value(), "Nothing to update");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("REST request to delete account with id: {}", id);

        accountService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
