package com.technicalchallenge.model;

import lombok.*;

import java.time.LocalDate;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//This class groups together all the optional search fields (counterparty, book, trader, etc.) into one object.
//        It’s passed from the controller to the service layer instead of many separate parameters.

public class SearchParameters {
   String counterpartyName;
   String bookName;
   String trader;
   String status;
   LocalDate fromDate;
   LocalDate toDate;

}
