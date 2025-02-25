package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.crypto.forex.ForexDataService;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.logger.MDCUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CronForexData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${env:Default}")
  private String env;

  @Value(value = "#{new Boolean(${rest.scheduled.forex.active:false})}")
  private Boolean isSchedulerActive;

  @Autowired private ForexDataService forexDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleAllCryptoAsset() {
    MDCUtils.registerDefaultMDC(env).subscribe();
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    LOG.info("[Forex] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("[Forex] Scheduler Active status is NOT-ACTIVE, Stopping Scheduler");
      return;
    }

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    // List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    if (fiatCurrencies.isEmpty()) {
      LOG.info("No Currency found on Database, Stopping Scheduler");
      return;
    }

    // Mi salvo tutti i Market Data presenti a DB in caso di rollback
    List<ForexData> allForexData = forexDataService.getAllForexData();

    // Cancello tutti i dati dalla tabella MarketData
    forexDataService.deleteForexData();
    Flux.fromIterable(fiatCurrencies)
        .flatMap(
            currency ->
                forexDataService
                    .getFromExchangeRateForexData(currency)
                    .doOnNext(
                        forexData -> {
                          LOG.info(
                              "Found {} rates of Forex Data for {}",
                              forexData.getQuotes().size(),
                              currency);
                          forexDataService.saveForexData(forexData);
                        })
                    .contextWrite(MDCUtils.contextViewMDC(env))
                    .doOnEach(signal -> MDCUtils.setContextMap(contextMap)))
        .doOnTerminate(() -> LOG.info("All operations completed"))
        .doOnError(
            throwable -> {
              LOG.error("Transaction is rolling back due to an error: {}", throwable.getMessage());
              LOG.error("Cleaning Forex Database");
              rollBackForexData(fiatCurrencies, allForexData);
            })
        .subscribe();

    LOG.info("Scheduler Finished at {}", LocalDateTime.now());
  }

  private void rollBackForexData(List<String> fiatCurrencies, List<ForexData> forexDataList) {
    forexDataService.deleteForexData();
    fiatCurrencies.forEach(
        fc -> {
          LOG.info("Found {} data of Forex Data to RollBack", forexDataList.size());
          if (!forexDataList.isEmpty())
            forexDataService.saveForexData(
                forexDataList.stream()
                    .filter(forex -> forex.getCurrency().equalsIgnoreCase(fc))
                    .findFirst()
                    .orElse(new ForexData()));
        });
  }
}
