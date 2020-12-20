package com.example.wallet.commontypes;

public class WalletException extends RuntimeException {
  public static final int GENERIC_ERROR = 0;
  public static final int NOT_FOUND = 1001;
  public static final int INSUFFICIENT_BALANCE = 1002;

  private final int errorCode;
  private final String message;

  public WalletException (String message) {
    super(message);
    errorCode = GENERIC_ERROR;
    this.message = message;
  }

  public WalletException(int errorCode, String additionalInfo) {
    super(errorCodeToMessage(errorCode));
    this.errorCode = errorCode;
    message = errorCodeToMessage(errorCode) + ": " + additionalInfo;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return message;
  }

  private static String errorCodeToMessage(int errorCode) {
    switch (errorCode) {
      case GENERIC_ERROR: return "Generic error";
      case NOT_FOUND: return "Not found";
      case INSUFFICIENT_BALANCE: return "Insufficient balance";
      default: return "Unknown error";
    }
  }

  public String getExceptionType() {
    return this.getClass().getName();
  }
}
