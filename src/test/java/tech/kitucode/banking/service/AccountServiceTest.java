package tech.kitucode.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import tech.kitucode.banking.domain.Account;
import tech.kitucode.banking.domain.Customer;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.AccountRepository;
import tech.kitucode.banking.repository.CustomerRepository;
import tech.kitucode.banking.service.dto.CreateAccountDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testGetAllAccounts() {
        Account account1 = new Account();
        account1.setAccountId(1L);
        account1.setIban("DTKEKENA4652669599669");
        account1.setBicSwift("DTKEKENA465");
        account1.setCustomerId(1L);
        account1.setCreatedOn(LocalDate.now());

        Account account2 = new Account();
        account2.setAccountId(2L);
        account2.setIban("DTKEKENA8192838877477");
        account2.setBicSwift("DTKEKENA366");
        account2.setCustomerId(1L);
        account2.setCreatedOn(LocalDate.now());

        Page page = new PageImpl(List.of(account1, account2));

        Account probe = new Account();
        probe.setIban(null);
        probe.setBicSwift(null);
        probe.setAccountId(null);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        when(accountRepository.findAll(Example.of(probe, matcher), Pageable.unpaged())).thenReturn(page);

        Page<Account> accounts = accountService.findAll(probe.getIban(), probe.getBicSwift(), probe.getAccountId(), Pageable.unpaged());

        assert !accounts.getContent().isEmpty();
        assert accounts.getContent().size() == 2;
    }

    @Test
    void testFindOneById() {
        Account mockAccount = new Account();
        mockAccount.setAccountId(1L);
        mockAccount.setIban("DTKEKENA4652669599669");
        mockAccount.setBicSwift("DTKEKENA465");
        mockAccount.setCustomerId(1L);
        mockAccount.setCreatedOn(LocalDate.now());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));

        Account account = accountService.findById(1L);
        assert account != null;
        assert account.getAccountId().equals(1L);
        assert account.getIban().equals("DTKEKENA4652669599669");
        assert account.getBicSwift().equals("DTKEKENA465");
        assert account.getCustomerId().equals(1L);
        assert account.getCreatedOn().equals(LocalDate.now());
    }

    @Test
    void testNonExistentCustomerDuringAccountCreation() {
        CreateAccountDTO createAccountDTO = new CreateAccountDTO();
        createAccountDTO.setBranchCode("465");
        createAccountDTO.setCustomerId(100L);

        when(customerRepository.findById(createAccountDTO.getCustomerId())).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            accountService.save(createAccountDTO);
        });
    }

    @Test
    void testInvalidRequestDuringAccountCreation() {
        CreateAccountDTO createAccountDTO = new CreateAccountDTO();
        createAccountDTO.setBranchCode(null);
        createAccountDTO.setCustomerId(100L);

        Customer mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        when(customerRepository.findById(createAccountDTO.getCustomerId())).thenReturn(Optional.of(mockCustomer));

        assertThrows(ValidationException.class, () -> {
            accountService.save(createAccountDTO);
        });
    }

    @Test
    void testCreateAccount() {
        Account mockAccount = new Account();
        mockAccount.setIban("DTKEKENA4652669599669");
        mockAccount.setBicSwift("DTKEKENA465");
        mockAccount.setCustomerId(1L);
        mockAccount.setCreatedOn(LocalDate.now());

        Account savedAccount = new Account();
        savedAccount.setAccountId(1L);
        savedAccount.setIban("DTKEKENA4652669599669");
        savedAccount.setBicSwift("DTKEKENA465");
        savedAccount.setCustomerId(1L);
        savedAccount.setCreatedOn(LocalDate.now());

        when(accountRepository.save(mockAccount)).thenReturn(savedAccount);

        Account account = accountService.save(mockAccount);

        assert account != null;
        assertEquals(account.getAccountId(), 1L);
        assertEquals(account.getIban(), "DTKEKENA4652669599669");
        assertEquals(account.getBicSwift(), "DTKEKENA465");
        assertEquals(account.getCustomerId(), 1L);
        assertEquals(account.getCreatedOn(), LocalDate.now());
    }

}
