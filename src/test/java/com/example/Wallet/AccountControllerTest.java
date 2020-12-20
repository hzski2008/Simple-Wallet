package com.example.Wallet;

import com.example.wallet.controller.AccountController;
import com.example.wallet.model.Account;
import com.example.wallet.service.WalletService;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
  @Autowired 
  private MockMvc mockMvc;
  @MockBean
  private WalletService walletService;
 
  @Test
  public void shouldReturnAllAccounts() throws Exception {
    Account account = new Account("John", 100.0);
        
    List<Account> list = List.of(account);
    when(walletService.findAllUsers()).thenReturn(list);
    this.mockMvc.perform(get("/accounts/all")).andDo(print()).andExpect(status().isOk())
      .andExpect(content().json("[{\"id\":null,\"name\":\"John\",\"balance\":100.0,\"version\":null}]"));         
  }
}
