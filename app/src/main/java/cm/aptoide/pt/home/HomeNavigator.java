package cm.aptoide.pt.home;

import android.os.Bundle;
import cm.aptoide.pt.R;
import cm.aptoide.pt.account.AccountAnalytics;
import cm.aptoide.pt.account.view.AccountNavigator;
import cm.aptoide.pt.app.AppNavigator;
import cm.aptoide.pt.app.view.AppCoinsInfoFragment;
import cm.aptoide.pt.app.view.AppViewFragment;
import cm.aptoide.pt.bottomNavigation.BottomNavigationItem;
import cm.aptoide.pt.bottomNavigation.BottomNavigationMapper;
import cm.aptoide.pt.dataprovider.ws.v7.store.StoreContext;
import cm.aptoide.pt.editorial.EditorialFragment;
import cm.aptoide.pt.home.bundles.base.AppBundle;
import cm.aptoide.pt.home.bundles.base.HomeEvent;
import cm.aptoide.pt.link.CustomTabsHelper;
import cm.aptoide.pt.navigator.ActivityNavigator;
import cm.aptoide.pt.navigator.FragmentNavigator;
import cm.aptoide.pt.promotions.PromotionsFragment;
import cm.aptoide.pt.search.model.SearchAdResult;
import cm.aptoide.pt.store.view.StoreTabGridRecyclerFragment;
import cm.aptoide.pt.view.settings.MyAccountFragment;
import java.util.AbstractMap;
import rx.Observable;

/**
 * Created by jdandrade on 13/03/2018.
 */

public class HomeNavigator {
  private static final String TAG = HomeNavigator.class.getSimpleName();
  private final FragmentNavigator fragmentNavigator;
  private final AptoideBottomNavigator aptoideBottomNavigator;
  private final BottomNavigationMapper bottomNavigationMapper;
  private final AppNavigator appNavigator;
  private final ActivityNavigator activityNavigator;
  private final String theme;
  private final AccountNavigator accountNavigator;

  public HomeNavigator(FragmentNavigator fragmentNavigator,
      AptoideBottomNavigator aptoideBottomNavigator, BottomNavigationMapper bottomNavigationMapper,
      AppNavigator appNavigator, ActivityNavigator activityNavigator, String theme,
      AccountNavigator accountNavigator) {
    this.fragmentNavigator = fragmentNavigator;
    this.aptoideBottomNavigator = aptoideBottomNavigator;
    this.bottomNavigationMapper = bottomNavigationMapper;
    this.appNavigator = appNavigator;
    this.activityNavigator = activityNavigator;
    this.theme = theme;
    this.accountNavigator = accountNavigator;
  }

  public void navigateToAppView(long appId, String packageName, String tag) {
    appNavigator.navigateWithAppId(appId, packageName, AppViewFragment.OpenType.OPEN_ONLY, tag);
  }

  public void navigateWithEditorsPosition(long appId, String packageName, String storeTheme,
      String storeName, String tag, String editorsPosition) {
    appNavigator.navigatewithEditorsPosition(appId, packageName, storeTheme, storeName, tag,
        editorsPosition);
  }

  public void navigateWithDownloadUrlAndReward(long appId, String packageName, String tag,
      String downloadUrl, float reward) {
    appNavigator.navigateWithDownloadUrlAndReward(appId, packageName, tag, downloadUrl, reward);
  }

  public void navigateWithAction(HomeEvent click) {

    String tag = click.getBundle()
        .getTag();
    if (click.getBundle() instanceof AppBundle) {
      tag = ((AppBundle) click.getBundle()).getActionTag();
    }

    fragmentNavigator.navigateTo(StoreTabGridRecyclerFragment.newInstance(click.getBundle()
        .getEvent(), click.getType(), click.getBundle()
        .getTitle(), "default", tag, StoreContext.home), true);
  }

  public void navigateToAppView(AbstractMap.SimpleEntry<String, SearchAdResult> entry) {
    appNavigator.navigateWithAdAndTag(entry.getValue(), entry.getKey());
  }

  public Observable<Integer> bottomNavigation() {
    return aptoideBottomNavigator.navigationEvent()
        .filter(menuPosition -> bottomNavigationMapper.mapItemClicked(menuPosition)
            .equals(BottomNavigationItem.HOME));
  }

  public void navigateToMyAccount() {
    fragmentNavigator.navigateTo(MyAccountFragment.newInstance(), true);
  }

  public void navigateToAppCoinsInformationView() {
    fragmentNavigator.navigateTo(new AppCoinsInfoFragment(), true);
  }

  public void navigateToEditorial(String cardId) {
    Bundle bundle = new Bundle();
    bundle.putString(EditorialFragment.CARD_ID, cardId);
    bundle.putBoolean(EditorialFragment.FROM_HOME, true);
    EditorialFragment fragment = new EditorialFragment();
    fragment.setArguments(bundle);
    fragmentNavigator.navigateTo(fragment, true);
  }

  public void navigateToTermsAndConditions() {
    CustomTabsHelper.getInstance()
        .openInChromeCustomTab(activityNavigator.getActivity()
            .getString(R.string.all_url_terms_conditions), activityNavigator.getActivity(), theme);
  }

  public void navigateToPrivacyPolicy() {
    CustomTabsHelper.getInstance()
        .openInChromeCustomTab(activityNavigator.getActivity()
            .getString(R.string.all_url_privacy_policy), activityNavigator.getActivity(), theme);
  }

  public void navigateToPromotions() {
    fragmentNavigator.navigateTo(new PromotionsFragment(), true);
  }

  public void navigateToLogIn() {
    accountNavigator.navigateToAccountView(AccountAnalytics.AccountOrigins.EDITORIAL);
  }
}
