package com.example.wallet.service;

import com.example.wallet.commontypes.EventType;
import com.example.wallet.commontypes.WalletException;
import com.example.wallet.model.Account;
import com.example.wallet.model.Event;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.EventRepository;
import java.sql.Timestamp;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class WalletService implements IWalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletService.class);

  @Autowired
  private AccountRepository accountRepository;
  @Autowired EventRepository eventRepository;

  //@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
  @Override
  public Optional<Account> findUserById(Long id) {
     // accountRepository.flush();
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

  @Override
  //@Transactional
  public Account updateUserAndLog(Account account,  Event event) {
    Double amount = event.getAmount();
  
    Account accountCopy = new Account(account); // make a clone of account before updating it
    Double balance = account.getBalance();

    if (EventType.purchase.equals(event.getEventType())) {
      if(amount > balance) {
        throw new WalletException(WalletException.INSUFFICIENT_BALANCE, " Balance = " + balance + " Amount to deduct = " + amount);
      }
      account.setBalance(balance - amount);
    } else if (EventType.profit.equals(event.getEventType())) {
      account.setBalance(balance + amount);
    }

    Account updatedAccount = accountCopy;
    
    try {
      updatedAccount = accountRepository.saveAndFlush(account);
    } catch (Exception e) { // saving could fail in case consurrency or account copy becomes obsolete  
      log.error("OptimisticLockException received! ", e);
      //Thread.sleep(1000);
      // when saving fails, need to retrieve the balance from db
      Account user = this.findUserById(account.getId()).get();
      user.setBalance(user.getBalance() + amount);
      updatedAccount = accountRepository.saveAndFlush(user); // retrying
    }
    log.info("Saved to Account table: " + account);

    // write event log to db
    event.setUserId(account.getId());
    log.info("Save to Events table: " + eventRepository.save(event)); 

    return updatedAccount;
  }
}