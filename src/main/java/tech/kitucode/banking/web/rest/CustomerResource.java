package tech.kitucode.banking.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.kitucode.banking.domain.Customer;
import tech.kitucode.banking.error.EntityNotFoundException;
import tech.kitucode.banking.service.CustomerService;
import tech.kitucode.banking.web.util.PaginationUtil;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class CustomerResource {
    private final String BASE_URL = "/api/customers";
    private final CustomerService customerService;

    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        log.info("REST request to create customer : {}", customer);
        Customer savedCustomer = customerService.save(customer);
        return ResponseEntity.created(URI.create(BASE_URL + "/" + savedCustomer.getCustomerId())).body(savedCustomer);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> findAll(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            Pageable pageable) {
        log.info("REST request to find all customers");
        Page<Customer> page = customerService.findAll(name, startDate, endDate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, BASE_URL);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> findOne(@PathVariable Long id) {
        log.info("REST request to find customer by customer id: {}", id);
        Customer customer = customerService.findOne(id);
        if (customer == null) {
            throw new EntityNotFoundException("Customer with id " + id + " not found");
        }
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/customers")
    public ResponseEntity<Customer> update(@RequestBody Customer customer) {
        log.info("REST request to update customer : {}", customer);
        Customer updatedCustomer = customerService.update(customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("REST request to delete customer with id : {}", id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
