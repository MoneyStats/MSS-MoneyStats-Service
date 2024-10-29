package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.authentication.AuthCacheService;
import com.giova.service.moneystats.crypto.forex.database.ForexDataCacheService;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataCacheService;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronCachingReset {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "#{new Boolean(${rest.scheduled.caching.active:false})}")
  @Autowired
  private Boolean isSchedulerActive;

  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private AuthCacheService authCacheService;
  @Autowired private WalletCacheService walletCacheService;
  @Autowired private ForexDataCacheService forexDataCacheService;

  @Scheduled(cron = "${rest.scheduled.caching.cron}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleCleanCache() {
    LOG.info("[Clean-Cache] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info(
          "[Clean-Cache] Scheduler Active status is {}, Stopping Scheduler", isSchedulerActive);
      return;
    }

    walletCacheService.clearAllWalletsCache();
    authCacheService.deleteUserCache();
    marketDataCacheService.clearAllMarketDataCache();
    forexDataCacheService.clearAllForexDataCache();

    LOG.info("[Clean-Cache] Scheduler Finished at {}", LocalDateTime.now());
  }
}
