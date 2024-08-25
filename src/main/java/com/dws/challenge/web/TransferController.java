package com.dws.challenge.web;

import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transfer")
@Slf4j
public class TransferController {

    private final AccountsService accountsService;

    @Autowired
    public TransferController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    /**
     * Method to handle Money transfer between accounts.
     * @param transferRequest transferRequest
     * @return ResponseEntity
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest transferRequest) {
        try {
            log.info("Transfer Request received {}",transferRequest.toString());
            accountsService.transferMoney(transferRequest);
            return ResponseEntity.ok("Transfer successful");
        } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
