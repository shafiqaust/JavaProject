package io.silverstring.domain.enums;

public enum CodeEnum {
    SUCCESS("0000", "Success"),
    FAIL("1000", "Fail"),
    UNKNOWN_ERROR("1001", "Unknown error"),
    BAD_REQUEST("1002", "Bad request"),
    CONSTANT_VALUE_IS_NULL("1003", "Constant value is null"),

    USER_NOT_EXIST("4001", "User is not exist"),
    USER_SETTING_NOT_EXIST("4002", "User setting is not exist"),
    API_KEY_INVALID("4003", " Invalid api key"),
    AMOUNT_IS_UNDER_ZERO("4004", "Amount is under zero"),
    WALLET_NOT_EXIST("4005", "Wallet is not exist"),
    WALLET_ALREADY_EXIST("4006", "Wallet already is exist"),
    ORDER_TYPE_INVALID("4007", "Invalid OrderType"),
    NOT_ENOUGH_BALANCE("4008", "Avaliable balance is not enough"),
    INVALID_CONFIRM_CODE("4009", "Invalid confirm code"),
    INVALID_EMAIL("4010", "Invalid email"),
    ORDER_CANCEL_FAIL("4011", "Order cancel fail"),
    INVALID_ORDER_TYPE("4012", "Invalid order type"),
    ORDER_NOT_EXIST("4013", "Order not exist"),
    ORDER_STATUS_IS_NOT_PLACED("4014", "Order status is not placed"),
    MIN_AMOUNT("4015", "Order amount is under min amount"),
    NOT_SUPPORTED("4016", "Not supported"),
    USER_FDS_LOCK("4017", "User fds is lock"),
    ADMIN_WALLET_BALANCE_IS_UNDER_ZERO("4018", "Admin wallet balance is under zero"),
    WALLET_UNLOCK_IS_FAIL("4019", "Wallet unlock is fail"),
    DO_NOT_ALLOW_INNER_TRANSFER_WALLET("4020", "Do not allow inner transfer wallet"),
    AMOUNT_IS_UNDER_MIN_AMOUNT("4021", "Amount is under mininum purchase amount"),
    ALREADY_SEND_PROCESS_RUNNING("4022", "Already send process is running"),
    MANUAL_TRANSACTION_NOT_EXIST("4023", "Manual transaction is not exist"),
    ADMIN_WALLET_NOT_EXIST("4023", "Admin wallet not exist"),
    ONLY_KRW_RECEIVED_REQUEST("4024", "Only krw received request"),
    ONLY_KRW_SEND_REQUEST("4025", "Only krw send request"),
    ALREADY_STATUS_IS_NOT_PENDING("4026", "Already status is not pending"),
    NOT_ENOUGH_VIRTUAL_ACCOUNT("4027", "Not enough vitrual account"),
    ALREADY_MANUAL_TRANSACTION_EXIST("4028", "Already manual transaction is exist"),
    ALREADY_TRANSACTION_EXIST("4029", "Already transaction is exist"),
    TRANSACTION_NOT_EXIST("4030", "Transaction not exist"),
    AVAILABLE_BALANCE_NOT_ENOUGH("4031", "Available balance not enough"),
    INVALID_PASSWORD("4032", "Invalid password"),
    WALLET_IS_NOT_ZERO("4031", "Avaliable balance is not zero"),
    ORDER_STATUS_IS_PLACED("4032", "Order_status is placed"),
    UNDER_PRICE_ZERO("4033", "Under price is zero"),
    UNDER_AMOUNT_ZERO("4034", "Under amount is zero"),
    INVAILD_ORDER_TYPE("4035", "Order format is mismatch"),
    COIN_NOT_EXIST("4036", "Coin not exist"),
    INVALID_OTP("4037", "Invalid OTP code"),
    NUMBER_IS_NEGATIVE("4038", "Number is negative"),
    COIN_SELLING_TIME_OVER("4039", "Coin Selling time is over"),
    REMAIN_AMOUNT_NOT_ENOUGH("4040", "The remaining sales amount is insufficient."),
    INPUT_VALUE_INCORRECT("4041", "Any of the input value is incorrect."),
    OTP_FLAG_IS_NULL("4042", "Choose whether to use Google Authenticator."),
    ALREADY_OTP_CREATE("4029", "Already otp code is exist"),
    BTRT_MININUM_PRICE_UNDER("4030", "The minimum price is 0.000002 BTC"),
    WITHDWRL_AMOUNT_UNDER_FEE("4031", "The amount applied for withdrawal must be greater than the withdrawal fee."),
    WITHDWRL_AMOUNT_OVER_REALAVAIL("4032", "The amount applied for withdrawal is more than the actual amount available for withdrawal."),
    WITHDWRL_AMOUNT_OVER_MINAMOUNT("4033", "The amount applied for withdrawal must be greater than mininum withdrawal amount."),
    WITHDWRL_AMOUNT_OVER_ONEDAYRESIDUAL("4043", "The amount applied for withdrawal is more than Residual Limit Amount for One-Day Withdrawal"),
    WITHDWRL_AMOUNT_OVER_ONEDAYLIMIT("4044", "The amount applied for withdrawal is more than Limit Amount for One-Day Withdrawal"),
    WITHDWRL_AMOUNT_OVER_ONETIMELIMIT("4045", "The amount applied for withdrawal is more than Limit Amount for One-Time Withdrawal"),

    ALREADY("501", "Already"),
    EQUAL_USER("502", "Equal_user");

    private String code;
    private String message;

    CodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
