package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.config.cache.CacheDataConfig;
import com.giova.service.moneystats.crypto.asset.database.AssetCacheService;
import com.giova.service.moneystats.crypto.forex.database.ForexDataCacheService;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataCacheService;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronCachingReset extends CacheDataConfig {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private WalletCacheService walletCacheService;
  @Autowired private ForexDataCacheService forexDataCacheService;
  @Autowired private AssetCacheService assetCacheService;

  @Scheduled(cron = "${spring.data.redis.cron}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleCleanCache() {
    LOG.info("[Clean-Cache] Scheduler Started at {}", LocalDateTime.now());

    if (!redisCacheEnabled) {
      LOG.info(
          "[Clean-Cache] Scheduler Active status is {}, Stopping Scheduler", redisCacheEnabled);
      return;
    }

    walletCacheService.clearAllWalletsCache();
    marketDataCacheService.clearAllMarketDataCache();
    forexDataCacheService.clearAllForexDataCache();
    assetCacheService.clearAllAssetsCache();

    LOG.info("[Clean-Cache] Scheduler Finished at {}", LocalDateTime.now());
  }
}
