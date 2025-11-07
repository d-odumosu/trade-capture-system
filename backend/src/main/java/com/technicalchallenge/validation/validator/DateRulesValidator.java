package com.technicalchallenge.validation.validator;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.validation.ValidationContext;
import com.technicalchallenge.validation.ValidationResult;
import java.time.LocalDate;

public class DateRulesValidator implements Validator {
    @Override
    public void validate(ValidationContext context, ValidationResult result) {
        TradeDTO tradeDto = context.getTradeDTO();
        LocalDate today = context.getCurrentDate();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        if ((tradeDto.getTradeDate() != null) && tradeDto.getTradeMaturityDate() != null) {

            if (tradeDto.getTradeStartDate() != null && tradeDto.getTradeStartDate().isBefore(tradeDto.getTradeDate())) {
                result.addError("Trade start date cannot be before the trade date.");
            }
            if (tradeDto.getTradeDate() != null && tradeDto.getTradeDate().isBefore(thirtyDaysAgo)) {
                result.addError("Trade date cannot be more than 30 days in the past.");
            }
            if (tradeDto.getTradeMaturityDate() != null) {
                if (tradeDto.getTradeDate() != null && tradeDto.getTradeMaturityDate().isBefore(tradeDto.getTradeDate())) {
                    result.addError("Maturity date cannot be before trade date.");
                }
                if (tradeDto.getTradeStartDate() != null && tradeDto.getTradeMaturityDate().isBefore(tradeDto.getTradeStartDate())) {
                    result.addError("Maturity date cannot be before start date.");
                }

            }

        }
    }
}