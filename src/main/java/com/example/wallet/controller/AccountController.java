package com.example.wallet.controller;

import com.example.wallet.commontypes.WalletException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.wallet.model.Account;
import com.example.wallet.model.Event;
import com.example.wallet.service.IWalletService;
import com.example.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;


@RestController
@Validated
@Tag(name = "wallet", description = "The wallet API")
public class AccountController {
  private static final Logger log = LoggerFactory.getLogger(WalletService.class);

  @Autowired
  private IWalletService walletService;

  @Operation(summary = "Update user account balance by user ID", description = "If eventType is 'purchase', deduct amount from user's balance. "
    + "If eventType is 'profit', add amount to user's balance. Other than those, returns invalid request error. "
    + "When success, returns updated user Account object in Json", tags = { "wallet" })
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation",
      content = @Content(schema = @Schema(implementation = Account.class))),
    @ApiResponse(responseCode = "404", description = "User account not found"),
    @ApiResponse(responseCode = "500", description = "Insufficient balance etc"),    
    @ApiResponse(responseCode = "400", description = "Transaction info contains invalid or missing field")})
  @PutMapping("/accounts/{id}")
  public Account updateBalance(@PathVariable(value = "id", required = true) Long accountId, @Valid @RequestBody Event event) {
    Account account = getAccountById(accountId);

    // Check if same request has been processed
    if (walletService.findTransactionById(event.getEventId()).isPresent()) {
      log.info("Ignore the duplicate request, transactionI id: " +event.getEventId());
      return account;
    }

    Account result;
    try {
      result = walletService.updateUserAndLog(account, event);
    } catch (WalletException ex) {
      if (ex.getErrorCode() == WalletException.INSUFFICIENT_BALANCE) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
      } else {
        throw ex;
      }
    }
    return result;
  }
  
  @Operation(summary = "Get user account by user ID", description = "Returns user Account object in Json", tags = {"wallet"})
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "successful operation"),
    @ApiResponse(responseCode = "404", description = "User account not found")})
  @GetMapping("/accounts/{id}")
  public Account getAccountById(@PathVariable(value = "id") Long accountId) {
    return walletService.findUserById(accountId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Id = " + accountId + " not found"));
  }
  
  /**
   * 
   * The below api are simply implemented just to facilitate the API testing of updateBalance API
   * They are not documented 
   */
  @GetMapping("/accounts/all")
  public Iterable<Account> getAllAccounts() {
    return walletService.findAllUsers();
  }

  @PostMapping("/accounts/new")
  public Account createAccount(@Valid @RequestBody Account account) {
    return walletService.saveUser(account);
  }

  @DeleteMapping("/accounts/{id}")
  public ResponseEntity<?> deleteAccount(@PathVariable(value = "id") Long accountId) {
    Account account = getAccountById(accountId);
    walletService.deleteUser(account);
    return ResponseEntity.ok().build();
  }
}
