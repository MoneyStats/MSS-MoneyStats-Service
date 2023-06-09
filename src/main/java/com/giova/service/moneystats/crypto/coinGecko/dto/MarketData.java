package com.giova.service.moneystats.crypto.coinGecko.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketData {
  private String identifier;
  @UpperCase private String symbol;
  @UpperCamelCase private String name;
  private String icon;
  @UpperCase private String currency;
  private Double current_price;
  private Double market_cap;
  private Double rank;
  private Double total_volume;
  private Double high_24h;
  private Double low_24h;
  private Double price_change_24h;
  private Double price_change_percentage_24h;
  private Double market_cap_change_24h;
  private Double market_cap_change_percentage_24h;
}
