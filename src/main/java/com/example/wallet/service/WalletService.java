package com.example.wallet.service;

import com.example.wallet.commontypes.EventType;
import com.example.wallet.commontypes.WalletException;
import com.example.wallet.model.Account;
import com.example.wallet.model.Event;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.EventRepository;
import java.util.Optional;
//import javax.persistence.OptimisticLockException;
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

  
  private Double calculateBalance(Double balance, Event event) {
    Double amount = event.getAmount();
    Double result = balance;
    if (EventType.purchase.equals(event.getEventType())) {   
      result = balance - amount;
    } else if (EventType.profit.equals(event.getEventType())) {
      result = balance + amount;
    } 
    if (result < 0) {
      throw new WalletException(WalletException.INSUFFICIENT_BALANCE, " Balance = " + balance + " Amount to deduct = " + amount);
    }
    return result; 
  }
  
  @Override
  //@Transactional
  public Account updateUserAndLog(Account account,  Event event) {      
    Double calculatedBalance = calculateBalance(account.getBalance(), event);
    account.setBalance(calculatedBalance);

    Account updatedAccount;    
    try {
      updatedAccount = accountRepository.saveAndFlush(account);
    } catch (Exception e) { // saving could fail in case consurrency or account copy becomes obsolete  
      log.error("OptimisticLockException received! ", e);
      // when saving fails, need to retrieve the fresh balance from db again
      Account user = this.findUserById(account.getId()).get();
      calculatedBalance = calculateBalance(user.getBalance(), event);
      user.setBalance(calculatedBalance);
      updatedAccount = accountRepository.saveAndFlush(user); // retrying saving
    }
    log.info("Saved to Account table: " + account);

    // write event log to db
    event.setUserId(account.getId());
    log.info("Save to Events table: " + eventRepository.save(event)); 

    return updatedAccount;
  }
}