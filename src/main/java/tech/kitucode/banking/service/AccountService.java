package tech.kitucode.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.kitucode.banking.domain.Account;
import tech.kitucode.banking.domain.Customer;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.AccountRepository;
import tech.kitucode.banking.repository.CustomerRepository;
import tech.kitucode.banking.service.dto.CreateAccountDTO;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account save(CreateAccountDTO createAccountDTO) {
        log.debug("Request to save account: {}", createAccountDTO);

        Customer customer = customerRepository.findById(createAccountDTO.getCustomerId()).orElse(null);
        if (customer == null) {
            throw new ValidationException("Customer with the specified id : " + createAccountDTO.getCustomerId() + " does not exist");
        }

        validateCreateAccountRequest(createAccountDTO);

        Account account = new Account();
        account.setCustomerId(createAccountDTO.getCustomerId());
        account.setBicSwift(generateBicSwift(createAccountDTO.getBranchCode()));
        account.setIban(generateIban(createAccountDTO.getBranchCode()));
        account.setCreatedOn(LocalDate.now());

        return accountRepository.save(account);
    }

    public Page<Account> findAll(String iban, String bicSwift, Long accountId, Pageable pageable) {
        log.debug("Request to find accounts by iban: {}, bicSwift: {}, accountId: {}", iban, bicSwift, accountId);

        Account probe = getProbe(iban, bicSwift, accountId);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        Example<Account> example = Example.of(probe, matcher);

        return accountRepository.findAll(example, pageable);
    }

    public Account findById(Long id) {
        log.debug("Request to find account by id: {}", id);

        return accountRepository.findById(id).orElse(null);
    }

    public Account update(Account account) {
        log.debug("Request to update account: {}", account);

        return null;
    }

    public void delete(Long id) {
        log.debug("Request to delete account by id : {}", id);
        accountRepository.deleteById(id);
    }

    private Account getProbe(String iban, String bicSwift, Long accountId) {
        Account account = new Account();

        if (iban != null && !iban.isEmpty()) {
            account.setIban(iban);
        }

        if (bicSwift != null && !bicSwift.isEmpty()) {
            account.setBicSwift(bicSwift);
        }

        if (accountId != null) {
            account.setAccountId(accountId);
        }

        return account;
    }

    private void validateCreateAccountRequest(CreateAccountDTO createAccountDTO) {
        if (createAccountDTO.getCustomerId() == null) {
            throw new ValidationException("Customer id cannot be null");
        }

        if (createAccountDTO.getBranchCode() == null || createAccountDTO.getBranchCode().isEmpty()) {
            throw new ValidationException("Branch code cannot be null or empty");
        }
    }

    private String generateBicSwift(String branchCode) {
        return "DTKEKENA" + branchCode;
    }

    private String generateIban(String branchCode) {
        String ibanPrefix = "DTKEKENA" + branchCode;

        Random random = new Random();
        long accountNumber = random.nextLong(1000000000L, 9999999999L);

        String iban = ibanPrefix + accountNumber;

        Optional<Account> optionalAccount = accountRepository.findOneByIban(iban);
        if (optionalAccount.isPresent()) {
            // recursive call to generate iban if the iban already exists in the accounts table
            return generateIban(branchCode);
        }

        return iban;
    }
}
