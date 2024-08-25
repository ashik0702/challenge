package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  @Getter
  private final EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService) {

    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public synchronized void transferMoney(final TransferRequest request){

    //Fetching Account details for transferring
    Account accountFrom = fetchAccountDetails(request.getAccountFromId());
    Account accountTo = fetchAccountDetails(request.getAccountToId());

    // Amount to transfer
    BigDecimal amount = request.getAmount();

    // Validating amount is non-negative and has sufficient funds for transfer.
    validateBalanceTransferCondition( accountFrom,  amount);

    //Updating account with updated balance
    updateAccount(accountFrom,accountTo,amount);

    //Email notification
    emailNotificationService.notifyAboutTransfer(accountFrom, "Transferred " + request.getAmount() + " to account " + accountTo.getAccountId());
    emailNotificationService.notifyAboutTransfer(accountTo, "Received " + request.getAmount() + " from account " + accountFrom.getAccountId());
  }

  private void updateAccount(Account accountFrom, Account accountTo,BigDecimal amount){
    this.accountsRepository.update(accountFrom.withUpdatedBalance(accountFrom.getBalance().subtract(amount)));
    this.accountsRepository.update(accountTo.withUpdatedBalance(accountTo.getBalance().add(amount)));
  }

  private void validateBalanceTransferCondition(Account accountFrom, BigDecimal amount){
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive.");
    }
    if (accountFrom.getBalance().compareTo(amount) < 0) {
      throw new InsufficientFundsException("Insufficient funds in account: " + accountFrom.getAccountId());
    }
  }

  private Account fetchAccountDetails(Long accountId){
    return Optional.ofNullable(getAccount(String.valueOf(accountId)))
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
  }

}
