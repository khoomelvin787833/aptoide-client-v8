package cm.aptoide.pt.ads;

import cm.aptoide.pt.abtesting.experiments.MoPubBannerAdExperiment;
import cm.aptoide.pt.abtesting.experiments.MoPubInterstitialAdExperiment;
import cm.aptoide.pt.abtesting.experiments.MoPubNativeAdExperiment;
import rx.Single;

public class MoPubAdsManager {

  private final MoPubInterstitialAdExperiment moPubInterstitialAdExperiment;
  private final MoPubBannerAdExperiment moPubBannerAdExperiment;
  private final MoPubNativeAdExperiment moPubNativeAdExperiment;
  private final WalletAdsOfferManager walletAdsOfferManager;

  public MoPubAdsManager(MoPubInterstitialAdExperiment moPubInterstitialAdExperiment,
      MoPubBannerAdExperiment moPubBannerAdExperiment,
      MoPubNativeAdExperiment moPubNativeAdExperiment,
      WalletAdsOfferManager walletAdsOfferManager) {
    this.moPubInterstitialAdExperiment = moPubInterstitialAdExperiment;
    this.moPubBannerAdExperiment = moPubBannerAdExperiment;
    this.moPubNativeAdExperiment = moPubNativeAdExperiment;
    this.walletAdsOfferManager = walletAdsOfferManager;
  }

  public Single<Boolean> shouldLoadInterstitialAd() {
    return shouldShowAds().flatMap(requestInterstitialAd -> {
      if (requestInterstitialAd) {
        return shouldHaveInterstitialAds();
      }
      return Single.just(false);
    });
  }

  public Single<Boolean> shouldHaveInterstitialAds() {
    return moPubInterstitialAdExperiment.shouldLoadInterstitial();
  }

  public Single<Boolean> shouldLoadBannerAd() {
    return shouldShowAds().flatMap(requestInterstitialAd -> {
      if (requestInterstitialAd) {
        return shouldHaveBannerAds();
      }
      return Single.just(false);
    });
  }

  private Single<Boolean> shouldHaveBannerAds() {
    return moPubBannerAdExperiment.shouldLoadBanner();
  }

  public Single<Boolean> shouldLoadNativeAds() {
    return shouldShowAds().flatMap(requestInterstitialAd -> {
      if (requestInterstitialAd) {
        return shouldHaveNativeAds();
      }
      return Single.just(false);
    });
  }

  private Single<Boolean> shouldHaveNativeAds() {
    return moPubNativeAdExperiment.shouldLoadNative();
  }

  public Single<Boolean> recordInterstitialAdImpression() {
    return moPubInterstitialAdExperiment.recordAdImpression();
  }

  public Single<Boolean> recordInterstitialAdClick() {
    return moPubInterstitialAdExperiment.recordAdClick();
  }

  public Single<Boolean> shouldShowAds() {
    return walletAdsOfferManager.shouldRequestMoPubAd();
  }
}