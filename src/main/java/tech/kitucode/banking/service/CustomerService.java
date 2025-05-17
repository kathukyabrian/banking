package tech.kitucode.banking.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tech.kitucode.banking.domain.Customer;
import tech.kitucode.banking.error.ValidationException;
import tech.kitucode.banking.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer save(Customer customer) {
        log.debug("Request to save customer: {}", customer);
        validateCustomer(customer);
        customer.setCreatedOn(LocalDate.now());
        return customerRepository.save(customer);
    }

    public Page<Customer> findAll(String name, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        String firstName = null;
        String lastName = null;
        String otherName = null;
        String[] nameArray = name.split(" ");
        int length = nameArray.length;
        if (length == 1) {
            firstName = nameArray[0];
        }

        if (length == 2) {
            firstName = nameArray[0];
            lastName = nameArray[1];
        }

        if (length == 3) {
            firstName = nameArray[0];
            lastName = nameArray[1];
            otherName = nameArray[2];
        }

        log.info("Request to find customers with firstName: {}, lastName: {}, otherName: {}, startDate: {}, endDate: {}", firstName, lastName, otherName, startDate, endDate);

        Specification<Customer> customerSpecification = createSpecification(firstName, lastName, otherName, startDate, endDate);

        return customerRepository.findAll(customerSpecification, pageable);
    }

    public Customer findOne(Long id) {
        log.debug("Request to find customer with id: {}", id);
        return customerRepository.findById(id).orElse(null);
    }

    public Customer update(Customer customer) {
        log.debug("Request to update customer : {}", customer);
        if (customer.getCustomerId() == null) {
            throw new ValidationException("customer id is required");
        }
        validateCustomer(customer);
        customer.setUpdatedOn(LocalDate.now());
        customer = customerRepository.save(customer);
        return customer;
    }

    public void delete(Long id) {
        log.debug("Request to delete customer with id : {}", id);
        customerRepository.deleteById(id);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getFirstName() == null || customer.getFirstName().isEmpty()) {
            throw new ValidationException("customer first name is required");
        }
        if (customer.getLastName() == null || customer.getLastName().isEmpty()) {
            throw new ValidationException("customer last name is required");
        }
    }

    private Specification<Customer> createSpecification(String firstName, String lastName, String otherName, LocalDate startDate, LocalDate endDate) {
        return ((root, query, criteriaBuilder) -> buildPredicates(root, criteriaBuilder, firstName, lastName, otherName, startDate, endDate));
    }

    public Predicate buildPredicates(Root<Customer> root, CriteriaBuilder criteriaBuilder, String firstName, String lastName, String otherName, LocalDate startDate, LocalDate endDate) {
        List<Predicate> predicates = new ArrayList<>();

        if (firstName != null && !firstName.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("firstName"), firstName));
        }

        if (lastName != null && !lastName.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("lastName"), lastName));
        }

        if (otherName != null && !otherName.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("otherName"), otherName));
        }

        if (startDate != null && endDate != null) {
            predicates.add(criteriaBuilder.between(root.get("createdOn"), startDate, endDate));
        }

        if (startDate != null && endDate == null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), startDate));
        }

        if (startDate == null && endDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), endDate));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
