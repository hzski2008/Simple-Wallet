package apitest;

import com.example.wallet.model.Account;
import com.example.wallet.commontypes.EventType;
import com.example.wallet.model.Event;
import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class WalletApiTest {

  @BeforeClass
  public static void setup() {
    RestAssured.reset();
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 8080;
    System.out.println("Accessing " + RestAssured.baseURI + " - Port:" + RestAssured.port);
  }
  
  private Map<String, String> userToMap(Account user) {
    return Map.of("name",user.getName(), "balance", String.valueOf(user.getBalance())); 
  }
  
  private Map<String, String> eventToMap(Event event) {
    Map<String, String> map = Map.of("eventId",String.valueOf(event.getEventId()), "eventType", String.valueOf(event.getEventType()), 
      "userId", String.valueOf(event.getUserId()),"amount", event.getAmount().toString(), "timestamp", "2020-12-20T16:41:07"); 
      return map;
  }

  private int createUser(Account user) { 
    Map<String, String> req = userToMap(user);
   
    Response res = given().contentType("application/json")
      .body(req).when().post("accounts/new").then()
      .statusCode(200).extract().response();
    
    JsonPath jsonPathEvaluator = res.jsonPath();
    return jsonPathEvaluator.get("id");
  }
  
  private void deleteUser(int id) {
    given().when().delete("accounts/" + id).then().statusCode(200);
  }
  
  private int generateRandomId() {
    //UUID.randomUUID().toString();
    Random rn = new Random();
    return rn.nextInt(10000);
  }
  
  @Test
  public void basicPingTest() {
    given().when().get("/accounts").then().statusCode(200);
  }
  
  @Test
  public void testGetAccountByInvalidIdReturnsError() {
    given().when().get("/accounts/2673").then().statusCode(404)
      .body("error", equalTo("Not Found"));
  }
  
  
  @Test
  public void testDeductBalanceOkCase() {    
    Account account = new Account("John", new BigDecimal(100.0));
    int id = createUser(account);
    Event event = new Event(Long.valueOf(generateRandomId()),Long.valueOf(id), EventType.purchase, new BigDecimal(10.0), null);
    
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(90f));    
    
    deleteUser(id);
  }
  
  @Test
  public void testAddBalanceOkCase() {
    Account account = new Account("Rachel", new BigDecimal(100.0));
    int id = createUser(account);
    
    Event event = new Event(Long.valueOf(generateRandomId()),Long.valueOf(id), EventType.profit, new BigDecimal(300.0), null);
      Map<String, String> ss = eventToMap(event);   
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(400f));
    
    deleteUser(id);
  }

  @Test
  public void testDuplicateRequestDoNotUpdateDB() {
    Account account = new Account("Rachel", new BigDecimal(100.0));
    int id = createUser(account);   
    Event event = new Event(Long.valueOf(generateRandomId()),Long.valueOf(id), EventType.profit, new BigDecimal(300.0), null);
    
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(400f));
    
    //the 2time, send the request with same transaction id again and check the balance is not changed
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(400f));
     
    // the 3rd time send the req with a new transaction id
    event.setEventId(Long.valueOf(generateRandomId()));
    event.setAmount(new BigDecimal(500.0));
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(900f));
     
    deleteUser(id);
  }
  
  @Test
  public void testNotFoundError() {
    Long nonExistingUserId = Long.valueOf(generateRandomId());
    Event event = new Event(Long.valueOf(generateRandomId()), nonExistingUserId, EventType.profit, new BigDecimal(300.0), null);
    
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + String.valueOf(nonExistingUserId)).then()
      .statusCode(404).body("error", equalTo("Not Found"));
  }
  
  @Test
  public void testInsufficientBalanceError() {      
    // create new user with balance 100
    Account account = new Account("Rachel", new BigDecimal(100.0));
    int id = createUser(account);
    Event event = new Event(Long.valueOf(generateRandomId()),Long.valueOf(id), EventType.purchase, new BigDecimal(100.0), null);
     
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(200).body("balance", equalTo(0f));
    
    event.setAmount(new BigDecimal(1.0));
    event.setEventId(Long.valueOf(generateRandomId()));
    
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(500).body("message", containsString("Insufficient balance"));
    deleteUser(id);
  }
   
  @Test
  public void testBadRequestError() {
    // create new user with balance 100
    Account account = new Account("Rachel", new BigDecimal(100.0));
    int id = createUser(account);
 
    // when amount is negative number
    Event event = new Event(Long.valueOf(generateRandomId()),Long.valueOf(id), EventType.profit, new BigDecimal(-1.0), null);
    given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + id).then()
      .statusCode(400).body("error", equalTo("BAD_REQUEST")).body("errorMessage", containsString("amount must be positive"));
    
    // when event id is not present in request
    Event event2 = new Event(null,Long.valueOf(id), EventType.profit, new BigDecimal(100.0), null);
     given().contentType("application/json")
     .body(eventToMap(event2)).when().put("accounts/" + id).then()
     .statusCode(400).body("error", equalTo("BAD_REQUEST")).body("errorMessage", containsString("eventId is mandatory"));
     
    // event type is invalid
    Map<String, String> reqInvalid = Map.of("eventId",String.valueOf(generateRandomId()), "event11Type", "profit", "amount", "100");
     given().contentType("application/json")
     .body(reqInvalid).when().put("accounts/" + id).then()
     .statusCode(400).body("error", equalTo("BAD_REQUEST")).body("errorMessage", containsString("eventType is mandatory"));
     
     deleteUser(id);
  }
  
  private BigDecimal addBalance(int userId, BigDecimal amount) {      
    Long uid = Long.valueOf(userId);
    Long eventId = Long.valueOf(generateRandomId());
    Event event = new Event( eventId,uid, EventType.profit, amount, null);
      
    Response res = given().contentType("application/json")
      .body(eventToMap(event)).when().put("accounts/" + userId).then()
      .statusCode(200)
      .extract().response(); 
      
    JsonPath jsonPathEvaluator = res.jsonPath();
    BigDecimal value = new BigDecimal(((Number)jsonPathEvaluator.get("balance")).toString());
    return value;
  }
  
  private BigDecimal getBalance(int userId) {
    Response res = given().when().get("accounts/" + userId).then()
      .statusCode(200).extract().response(); 
      
    JsonPath jsonPathEvaluator = res.jsonPath();
    BigDecimal value = new BigDecimal (((Number)jsonPathEvaluator.get("balance")).toString());
    return value;
  }
  
  //@Ignore
  @Test
  public void testUpdateBalanceWithParalleExecution() throws Exception {
    // create new user with balance 100
    Account account = new Account("Rachel", new BigDecimal(100.0));
    int id = createUser(account);
   
    ExecutorService executor = Executors.newCachedThreadPool();
    List<Callable<BigDecimal>> callables = Arrays.asList(
      () -> addBalance(id, new BigDecimal(100)),
      () -> addBalance(id, new BigDecimal(200)),
      () -> addBalance(id, new BigDecimal(300)));
    
    executor.invokeAll(callables)      
      .stream()
      .map(future -> {
        try {
          return future.get();
        }
        catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .forEach(System.out::println); 
    
    executor.shutdown();
    
    BigDecimal result = getBalance(id);
    
    Assert.assertEquals(700, result.intValue());
    deleteUser(id);
  }
}
