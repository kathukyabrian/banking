package tech.kitucode.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.kitucode.banking.domain.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findOneByIban(String iban);
}
