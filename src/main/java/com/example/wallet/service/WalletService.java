package com.example.wallet.service;

import com.example.wallet.commontypes.EventType;
import com.example.wallet.commontypes.WalletException;
import com.example.wallet.model.Account;
import com.example.wallet.model.Event;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.EventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService implements IWalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletService.class);

  @Autowired
  private AccountRepository accountRepository;
  @Autowired EventRepository eventRepository;
  
  @Override
  public Optional<Account> findUserById(Long id) {
    return accountRepository.findById(id);
  }

  @Override
  public Account saveUser(Account account) {
    return accountRepository.save(account);
  }

  @Override
  public Iterable<Account> findAllUsers() {
    return accountRepository.findAll();
  }

  @Override
  public void deleteUser(Account account) {
    accountRepository.delete(account);
  }

  @Override
  public Optional<Event> findTransactionById(Long id) {
    return eventRepository.findById(id);
  }

  @Override
  public Event saveTransaction(Event event) {
    return eventRepository.save(event);
  }

  
  private BigDecimal calculateBalance(BigDecimal balance, Event event) {
    BigDecimal amount = event.getAmount();
    BigDecimal result = balance;

    if (EventType.purchase.equals(event.getEventType())) {   
      result = balance.subtract(amount);
    } else if (EventType.profit.equals(event.getEventType())) {
      result = balance.add(amount);
    }     
    result = result.setScale(2, RoundingMode.HALF_EVEN);
    if (result.compareTo(BigDecimal.ZERO) < 0) {
      throw new WalletException(WalletException.INSUFFICIENT_BALANCE, " Balance = " + balance + " Amount to deduct = " + amount);
    }
    return result; 
  }
  
  @Override 
  public Account updateUserAndLog(Long accountId, Event event) {
    Account account = updateUser(accountId, event);
    // write event log to db
    event.setUserId(accountId);
    log.info("Save to Events table: " + eventRepository.save(event));
    return account;
  }
  
  private synchronized Account updateUser(Long accountId,  Event event) {
    Account account = findUserById(accountId)
      .orElseThrow(() -> new WalletException(WalletException.NOT_FOUND, "Id = " + accountId + " not found"));
            
    // Check if same request has been processed
    if (findTransactionById(event.getEventId()).isPresent()) {
      log.info("Ignore the duplicate request, transactionI id: " +event.getEventId());
      return account;
    }
    
    BigDecimal calculatedBalance = calculateBalance(account.getBalance(), event);
    account.setBalance(calculatedBalance);

    Account updatedAccount = accountRepository.saveAndFlush(account);
    log.info("Saved to Account table: " + account);

    return updatedAccount;
  }
}