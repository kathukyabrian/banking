package tech.kitucode.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kitucode.banking.domain.Customer;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testFindOneById() {
        Customer mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setFirstName("Brian");
        mockCustomer.setLastName("Kitunda");
        mockCustomer.setOtherName("Kathukya");
        mockCustomer.setCreatedOn(LocalDate.now());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        Customer customer = customerService.findOne(1L);
        assert customer != null;
        assert customer.getCustomerId().equals(1L);
        assert customer.getFirstName().equals("Brian");
        assert customer.getLastName().equals("Kitunda");
        assert customer.getOtherName().equals("Kathukya");
        assert customer.getCreatedOn().equals(LocalDate.now());
    }

    @Test
    void testCreateCustomerWithoutFirstAndSecondName() {
        Customer withoutFirstName = new Customer();
        withoutFirstName.setFirstName(null);
        withoutFirstName.setLastName("Kitunda");
        withoutFirstName.setOtherName("Kathukya");

        Customer withoutLastName = new Customer();
        withoutLastName.setFirstName("Brian");
        withoutLastName.setLastName(null);
        withoutLastName.setOtherName("Kathukya");

        assertThrows(ValidationException.class, () -> {
            customerService.save(withoutFirstName);
        });

        assertThrows(ValidationException.class, () -> {
            customerService.save(withoutLastName);
        });
    }

    @Test
    void testSuccessfulCreateCustomer() {
        Customer mockCustomer = new Customer();
        mockCustomer.setFirstName("Brian");
        mockCustomer.setLastName("Kitunda");
        mockCustomer.setOtherName("Kathukya");

        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId(1L);
        savedCustomer.setFirstName("Brian");
        savedCustomer.setLastName("Kitunda");
        savedCustomer.setOtherName("Kathukya");
        savedCustomer.setCreatedOn(LocalDate.now());

        when(customerRepository.save(mockCustomer)).thenReturn(savedCustomer);

        Customer customer = customerService.save(mockCustomer);

        assertEquals(customer.getCustomerId(), 1L);
        assertEquals(customer.getFirstName(), "Brian");
        assertEquals(customer.getLastName(), "Kitunda");
        assertEquals(customer.getOtherName(), "Kathukya");
        assertEquals(customer.getCreatedOn(), LocalDate.now());
    }

    @Test
    void testInvalidUpdate() {
        Customer withoutFirstName = new Customer();
        withoutFirstName.setCustomerId(1L);
        withoutFirstName.setFirstName(null);
        withoutFirstName.setLastName("Kitunda");
        withoutFirstName.setOtherName("Kathukya");

        Customer withoutLastName = new Customer();
        withoutLastName.setCustomerId(1L);
        withoutLastName.setFirstName("Brian");
        withoutLastName.setLastName(null);
        withoutLastName.setOtherName("Kathukya");

        Customer withoutCustomerId = new Customer();
        withoutCustomerId.setFirstName("Brian");
        withoutCustomerId.setLastName("Kitunda");
        withoutCustomerId.setOtherName("Kathukya");

        assertThrows(ValidationException.class, () -> {
            customerService.update(withoutCustomerId);
        });

        assertThrows(ValidationException.class, () -> {
            customerService.update(withoutFirstName);
        });

        assertThrows(ValidationException.class, () -> {
            customerService.update(withoutLastName);
        });
    }

    @Test
    void testSuccessfulCustomerUpdate() {
        Customer mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setFirstName("Brian");
        mockCustomer.setLastName("Kitush");
        mockCustomer.setOtherName("Kathukya");

        when(customerRepository.save(mockCustomer)).thenReturn(mockCustomer);

        Customer updatedCustomer = customerService.update(mockCustomer);

        assertEquals(updatedCustomer.getCustomerId(), 1L);
        assertEquals(updatedCustomer.getFirstName(), "Brian");
        assertEquals(updatedCustomer.getLastName(), "Kitush");
        assertEquals(updatedCustomer.getOtherName(), "Kathukya");
    }
}
