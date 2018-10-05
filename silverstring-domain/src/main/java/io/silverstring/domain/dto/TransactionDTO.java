package io.silverstring.domain.dto;

import io.silverstring.domain.enums.CategoryEnum;
import io.silverstring.domain.hibernate.Transaction;
import lombok.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class TransactionDTO {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReqTransactions {
        CategoryEnum category;
        String coinName;
        Integer pageNo;
        Integer pageSize;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResTransactions {
        Integer pageNo;
        Integer pageSize;
        Integer pageTotalCnt;
        List<Transaction> transactions;
    }

    @Data
    public static class ReqCancelWithdrawal {
        String transaction_id;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResCancelWithdrawal {
        String transaction_id;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReqWithdrawal {
        @NotNull
        String coinName;

        @NotNull
        String address;
        String tag;

        //FOR KRW
        String bankNm;
        String recvNm;

        @NotNull
        BigDecimal amount;

        @NotNull
        String otp;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResWithdrawal {
        @NotNull
        String coinName;

        @NotNull
        String address;
        String tag;

        //FOR KRW
        String bankNm;
        String recvNm;
        BigDecimal amount;
    }
}
