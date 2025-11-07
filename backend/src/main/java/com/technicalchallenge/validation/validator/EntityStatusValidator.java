package com.technicalchallenge.validation.validator;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.validation.ValidationContext;
import com.technicalchallenge.validation.ValidationResult;
import com.technicalchallenge.validation.refData.ReferenceDataGateway;

public class EntityStatusValidator implements Validator {

    @Override
    public void validate(ValidationContext context, ValidationResult result) {
        TradeDTO tradeDTO = context.getTradeDTO();
        ReferenceDataGateway refData = context.getRefDataGateway();
        ApplicationUser user = context.getUser();

        // BOOK
        if (tradeDTO.getBookId() == null) {
            result.addError("Book ID is missing from the trade.");
        } else if (!refData.bookExists(tradeDTO.getBookId())) {
            result.addError("Book with ID " + tradeDTO.getBookId() + " does not exist.");
        } else if (!refData.isBookActive(tradeDTO.getBookId())) {
            result.addError("Book with ID " + tradeDTO.getBookId() + " is inactive.");
        }

        // COUNTERPARTY
        if (tradeDTO.getCounterpartyId() == null) {
            result.addError("Counterparty ID is missing from the trade.");
        } else if (!refData.counterpartyExists(tradeDTO.getCounterpartyId())) {
            result.addError("Counterparty with ID " + tradeDTO.getCounterpartyId() + " does not exist.");
        } else if (!refData.isCounterpartyActive(tradeDTO.getCounterpartyId())) {
            result.addError("Counterparty with ID " + tradeDTO.getCounterpartyId() + " is inactive.");
        }

        // TRADER USER
        if (tradeDTO.getTraderUserId() == null) {
            result.addError("Trader User ID is missing from the trade.");
        } else if (!refData.userExistsById(tradeDTO.getTraderUserId())) {
            result.addError("Trader User with ID " + tradeDTO.getTraderUserId() + " does not exist.");
        } else if (!refData.isUserActive(tradeDTO.getTraderUserId())) {
            result.addError("Trader User with ID " + tradeDTO.getTraderUserId() + " is inactive.");
        }

        // TRADE INPUTTER USER
        if (tradeDTO.getTradeInputterUserId() == null) {
            result.addError("Trade Inputter User ID is missing from the trade.");
        } else if (!refData.userExistsById(tradeDTO.getTradeInputterUserId())) {
            result.addError("Trade Inputter User with ID " + tradeDTO.getTradeInputterUserId() + " does not exist.");
        } else if (!refData.isUserActive(tradeDTO.getTradeInputterUserId())) {
            result.addError("Trade Inputter User with ID " + tradeDTO.getTradeInputterUserId() + " is inactive.");
        }

        // USER WARNING — SAME USER FOR BOTH
        if (tradeDTO.getTraderUserId() != null &&
                tradeDTO.getTradeInputterUserId() != null &&
                tradeDTO.getTraderUserId().equals(tradeDTO.getTradeInputterUserId())) {
            result.addWarning("Trader User and Trade Inputter User are the same person — separation of duties warning.");
        }

        // TRADE STATUS
        if (tradeDTO.getTradeStatusId() == null) {
            result.addError("Trade Status ID is missing from the trade.");
        } else if (!refData.tradeStatusExists(tradeDTO.getTradeStatusId())) {
            result.addError("Trade Status with ID " + tradeDTO.getTradeStatusId() + " does not exist.");
        }

        // TRADE TYPE
        if (tradeDTO.getTradeTypeId() == null) {
            result.addError("Trade Type ID is missing from the trade.");
        } else if (!refData.tradeTypeExists(tradeDTO.getTradeTypeId())) {
            result.addError("Trade Type with ID " + tradeDTO.getTradeTypeId() + " does not exist.");
        }

        // TRADE SUB TYPE
        if (tradeDTO.getTradeSubTypeId() == null) {
            result.addError("Trade Sub-Type ID is missing from the trade.");
        } else if (!refData.tradeSubTypeExists(tradeDTO.getTradeSubTypeId())) {
            result.addError("Trade Sub-Type with ID " + tradeDTO.getTradeSubTypeId() + " does not exist.");
        }

        // LOGGED-IN USER CONTEXT
        if (user == null || user.getLoginId() == null) {
            result.addError("No user context provided for validation.");
        } else if (!refData.userExists(user.getLoginId())) {
            result.addError("User with loginId '" + user.getLoginId() + "' does not exist.");
        } else if (!refData.isUserActive(user.getId())) {
            result.addError("User with loginId '" + user.getLoginId() + "' is inactive and cannot perform this operation.");
        }
    }
}
