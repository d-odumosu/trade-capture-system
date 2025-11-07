package com.technicalchallenge.validation.validator;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.validation.ValidationContext;
import com.technicalchallenge.validation.ValidationResult;

import java.util.List;

public class LegConsistencyValidator implements Validator {
    @Override
    public void validate(ValidationContext context, ValidationResult result) {
        TradeDTO tradeDTO = context.getTradeDTO();
        List<TradeLegDTO> legs = tradeDTO.getTradeLegs();

        //CHECKING NULL AND EMPTY
        if(legs == null || legs.isEmpty()) {
            result.addError(("Trade has no legs defined. At least one leg is required."));
            return;
        }
        if (legs.size() < 2) {
            result.addWarning("Trade cannot have less than two legs ");
            return;
        }


    }
}
