package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {


  @Mock
  private NotificationService emailNotificationService;

  @Autowired
  private AccountsService accountsService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void testTransferMoney_success() {

    Long accountFromId = 1L;
    Long accountToId = 2L;
    BigDecimal transferAmount = BigDecimal.valueOf(100);

    Account accountFrom = new Account(accountFromId.toString(), BigDecimal.valueOf(500));
    Account accountTo = new Account(accountToId.toString(), BigDecimal.valueOf(200));
    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);
    TransferRequest request = new TransferRequest(accountFromId, accountToId, transferAmount);

    accountsService.transferMoney(request);


    verify(emailNotificationService,never()).notifyAboutTransfer(accountFrom, "Transferred 100 to account 2");
    verify(emailNotificationService,never()).notifyAboutTransfer(accountTo, "Received 100 from account 1");

    Assertions.assertEquals(accountsService.getAccount(accountFrom.getAccountId()).getBalance(),BigDecimal.valueOf(400));
    Assertions.assertEquals(accountsService.getAccount(accountTo.getAccountId()).getBalance(),BigDecimal.valueOf(300));

  }

  @Test
  void testTransferMoney_insufficientFunds() throws Exception {
    Long accountFromId = 1L;
    Long accountToId = 2L;
    BigDecimal transferAmount = BigDecimal.valueOf(400);

    Account accountFrom = new Account(accountFromId.toString(), BigDecimal.valueOf(300));
    Account accountTo = new Account(accountToId.toString(), BigDecimal.valueOf(200));
    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);
    TransferRequest request = new TransferRequest(accountFromId, accountToId, transferAmount);

    assertThrows(InsufficientFundsException.class,() ->accountsService.transferMoney(request));


  }

  @Test
  void testTransferMoney_accountNotFound() {

    Long accountFromId = 1L;
    Long accountToId = 2L;
    BigDecimal transferAmount = BigDecimal.valueOf(100);
    Account accountTo = new Account(accountToId.toString(), BigDecimal.valueOf(200));

    accountsService.createAccount(accountTo);
    TransferRequest request = new TransferRequest(accountFromId, accountToId, transferAmount);

    assertThrows(AccountNotFoundException.class,() ->accountsService.transferMoney(request));
  }

  @Test
  void testTransferMoney_negativeAmount() {

    Long accountFromId = 1L;
    Long accountToId = 2L;
    BigDecimal transferAmount = BigDecimal.valueOf(-100);
    Account accountFrom = new Account(accountFromId.toString(), BigDecimal.valueOf(300));
    Account accountTo = new Account(accountToId.toString(), BigDecimal.valueOf(200));
    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);
    TransferRequest request = new TransferRequest(accountFromId, accountToId, transferAmount);

    assertThrows(IllegalArgumentException.class,() ->accountsService.transferMoney(request));
  }
}
