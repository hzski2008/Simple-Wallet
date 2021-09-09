package com.example.wallet.service;

import com.example.wallet.model.Account;
import com.example.wallet.model.Event;
import java.util.Optional;

public interface IWalletService {
  Optional<Account> findUserById(Long id);
  Account saveUser(Account account);
  Iterable<Account>  findAllUsers();
  void deleteUser(Account account);
  Optional<Event> findTransactionById(Long id);
  Event saveTransaction(Event event);
  Account updateUserAndLog(Long accountId, Event event);
}