package cm.aptoide.pt.home.apps;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cm.aptoide.analytics.implementation.navigation.ScreenTagHistory;
import cm.aptoide.pt.R;
import cm.aptoide.pt.bottomNavigation.BottomNavigationActivity;
import cm.aptoide.pt.bottomNavigation.BottomNavigationItem;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.GenericDialogs;
import cm.aptoide.pt.view.fragment.NavigationTrackFragment;
import cm.aptoide.pt.view.rx.RxAlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.view.RxView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

import static cm.aptoide.pt.utils.GenericDialogs.EResponse.YES;

/**
 * Created by filipegoncalves on 3/7/18.
 */

public class AppsFragment extends NavigationTrackFragment implements AppsFragmentView {

  private static final BottomNavigationItem BOTTOM_NAVIGATION_ITEM = BottomNavigationItem.APPS;
  private static final int APPC_UPDATES_LIMIT = 2;

  @Inject AppsPresenter appsPresenter;
  private RecyclerView recyclerView;
  private AppsAdapter adapter;
  private View appcAppsLayout;
  private RecyclerView appcAppsRecyclerView;
  private AppcAppsAdapter appcAppsAdapter;
  private Button appcSeeMoreButton;
  private PublishSubject<AppClick> appItemClicks;
  private PublishSubject<Void> updateAll;
  private RxAlertDialog ignoreUpdateDialog;
  private ImageView userAvatar;
  private ProgressBar progressBar;
  private BottomNavigationActivity bottomNavigationActivity;
  private SwipeRefreshLayout swipeRefreshLayout;
  private boolean showDownloads;
  private boolean showUpdates;
  private boolean showInstalled;
  private boolean showUpgrades;
  private List<App> blackListDownloads;
  private PublishSubject<Void> appcUpgradesSectionLoaded;
  private PublishSubject<Void> updatesSectionLoaded;

  public static AppsFragment newInstance() {
    return new AppsFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentComponent(savedInstanceState).inject(this);
    appItemClicks = PublishSubject.create();
    updateAll = PublishSubject.create();
    appcUpgradesSectionLoaded = PublishSubject.create();
    updatesSectionLoaded = PublishSubject.create();
    blackListDownloads = new ArrayList<>();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (bottomNavigationActivity != null) {
      bottomNavigationActivity.requestFocus(BOTTOM_NAVIGATION_ITEM);
    }
    recyclerView = view.findViewById(R.id.fragment_apps_recycler_view);
    recyclerView.setNestedScrollingEnabled(false);
    adapter =
        new AppsAdapter(new ArrayList<>(), new AppsCardViewHolderFactory(appItemClicks, updateAll));

    appcAppsLayout = view.findViewById(R.id.appc_apps_layout);
    appcAppsRecyclerView = view.findViewById(R.id.appc_apps_recycler_view);
    appcAppsRecyclerView.setNestedScrollingEnabled(false);
    appcSeeMoreButton = view.findViewById(R.id.appc_see_more_btn);

    appcAppsAdapter = new AppcAppsAdapter(new ArrayList<>(), appItemClicks, APPC_UPDATES_LIMIT);

    swipeRefreshLayout = view.findViewById(R.id.fragment_apps_swipe_container);
    swipeRefreshLayout.setColorSchemeResources(R.color.default_progress_bar_color,
        R.color.default_color, R.color.default_progress_bar_color, R.color.default_color);
    progressBar = view.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.VISIBLE);
    setupRecyclerView();
    buildIgnoreUpdatesDialog();
    userAvatar = view.findViewById(R.id.user_actionbar_icon);

    attachPresenter(appsPresenter);
  }

  @Override public ScreenTagHistory getHistoryTracker() {
    return ScreenTagHistory.Builder.build(this.getClass()
        .getSimpleName());
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof BottomNavigationActivity) {
      bottomNavigationActivity = ((BottomNavigationActivity) activity);
    }
  }

  @Override public void onDestroy() {
    updateAll = null;
    appItemClicks = null;
    blackListDownloads = null;
    super.onDestroy();
  }

  private void buildIgnoreUpdatesDialog() {
    ignoreUpdateDialog =
        new RxAlertDialog.Builder(getContext()).setTitle(R.string.apps_title_ignore_updates)
            .setPositiveButton(R.string.apps_button_ignore_updates_yes)
            .setNegativeButton(R.string.apps_button_ignore_updates_no)
            .build();
  }

  private void setupRecyclerView() {
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(
        new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    recyclerView.setItemAnimator(null);

    appcAppsRecyclerView.setAdapter(appcAppsAdapter);
    appcAppsRecyclerView.setLayoutManager(
        new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    appcAppsRecyclerView.setItemAnimator(null);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_apps, container, false);
  }

  @Override public void showUpdatesList(List<App> list) {
    Logger.getInstance()
        .d("Apps", "showing updates list");
    if (list != null && !list.isEmpty()) {
      adapter.setAvailableUpdatesList(list);
    }
    showUpdates = true;
    updatesSectionLoaded.onNext(null);
    if (shouldShowAppsList()) {
      showAppsList();
    }
  }

  @Override public void showInstalledApps(List<App> installedApps) {
    Logger.getInstance()
        .d("Apps", "showing installed apps list");
    if (installedApps != null && !installedApps.isEmpty()) {
      adapter.addInstalledAppsList(installedApps);
    }
    showInstalled = true;
    if (shouldShowAppsList()) {
      showAppsList();
    }
  }

  @Override public void showDownloadsList(List<App> list) {
    Logger.getInstance()
        .d("Apps", "showing downloads list");

    list.removeAll(blackListDownloads);
    if (!list.isEmpty()) {
      adapter.addDownloadAppsList(list);
    }
    showDownloads = true;
    if (shouldShowAppsList()) {
      showAppsList();
    }
  }

  @Override public Observable<App> retryDownload() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.RETRY_DOWNLOAD)
        .map(appClick -> appClick.getApp());
  }

  @Override public Observable<App> installApp() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.INSTALL_APP)
        .map(appClick -> appClick.getApp());
  }

  @Override public Observable<App> cancelDownload() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.CANCEL_DOWNLOAD)
        .map(appClick -> appClick.getApp())
        .doOnNext(app -> blackListDownloads.add(app));
  }

  @Override public Observable<App> resumeDownload() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.RESUME_DOWNLOAD)
        .map(appClick -> appClick.getApp());
  }

  @Override public Observable<App> pauseDownload() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.PAUSE_DOWNLOAD)
        .map(appClick -> appClick.getApp());
  }

  @Override public Observable<App> startDownloadInAppview() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.APPC_DOWNLOAD_APPVIEW)
        .map(appClick -> appClick.getApp());
  }

  @Override public Observable<AppClickEventWrapper> retryUpdate() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.RETRY_UPDATE
            || appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_RETRY)
        .map(appClick -> new AppClickEventWrapper(
            appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_RETRY, appClick.getApp()));
  }

  @Override public Observable<AppClickEventWrapper> updateApp() {
    return appItemClicks.filter(appClick -> appClick.getClickType() == AppClick.ClickType.UPDATE_APP
        || appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_APP)
        .map(appClick -> new AppClickEventWrapper(
            appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_APP, appClick.getApp()));
  }

  @Override public Observable<AppClickEventWrapper> pauseUpdate() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.PAUSE_UPDATE
            || appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_PAUSE)
        .map(appClick -> new AppClickEventWrapper(
            appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_PAUSE, appClick.getApp()));
  }

  @Override public Observable<AppClickEventWrapper> cancelUpdate() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.CANCEL_UPDATE
            || appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_CANCEL)
        .map(appClick -> new AppClickEventWrapper(
            appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_CANCEL, appClick.getApp()));
  }

  @Override public Observable<AppClickEventWrapper> resumeUpdate() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.RESUME_UPDATE
            || appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_RESUME)
        .map(appClick -> new AppClickEventWrapper(
            appClick.getClickType() == AppClick.ClickType.APPC_UPGRADE_RESUME, appClick.getApp()));
  }

  @Override public Observable<Boolean> showRootWarning() {
    return GenericDialogs.createGenericYesNoCancelMessage(getContext(), "",
        AptoideUtils.StringU.getFormattedString(R.string.root_access_dialog, getResources()))
        .map(response -> (response.equals(YES)));
  }

  @Override public void showUpdatesDownloadList(List<App> updatesDownloadList) {
    Logger.getInstance()
        .d("Apps", "showing updates observable list");
    if (updatesDownloadList != null && !updatesDownloadList.isEmpty()) {
      adapter.addUpdateAppsList(updatesDownloadList);
    }
    if (shouldShowAppsList()) {
      showAppsList();
    }
  }

  @Override public void showAppcUpgradesDownloadList(List<App> updatesDownloadList) {
    Logger.getInstance()
        .d("Apps", "showing appc upgrades observable list");
    if (updatesDownloadList != null && !updatesDownloadList.isEmpty()) {
      appcAppsAdapter.addApps(updatesDownloadList);
    }
    triggerAppcUpgradesVisibility(appcAppsAdapter.getTotalItemCount());
  }

  @Override public Observable<Void> updateAll() {
    return updateAll;
  }

  @Override public Observable<App> updateLongClick() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.UPDATE_CARD_LONG_CLICK)
        .map(appClick -> appClick.getApp());
  }

  @Override public void showIgnoreUpdate() {
    ignoreUpdateDialog.show();
  }

  @Override public Observable<Void> ignoreUpdate() {
    return ignoreUpdateDialog.positiveClicks()
        .map(__ -> null);
  }

  @Override public void showUnknownErrorMessage() {
    Snackbar.make(this.getView(), R.string.unknown_error, Snackbar.LENGTH_SHORT)
        .show();
  }

  @Override public void removeExcludedUpdates(List<App> excludedUpdatesList) {
    adapter.removeUpdatesList(excludedUpdatesList);
  }

  @Override public Observable<Void> moreAppcClick() {
    return RxView.clicks(appcSeeMoreButton);
  }

  @Override public Observable<App> cardClick() {
    return appItemClicks.filter(
        appClick -> appClick.getClickType() == AppClick.ClickType.CARD_CLICK)
        .map(appClick -> appClick.getApp());
  }

  @Override public void setUserImage(String userAvatarUrl) {
    ImageLoader.with(getContext())
        .loadWithShadowCircleTransformWithPlaceholder(userAvatarUrl, userAvatar,
            R.drawable.ic_account_circle);
  }

  @Override public void showAvatar() {
    userAvatar.setVisibility(View.VISIBLE);
  }

  @Override public Observable<Void> imageClick() {
    return RxView.clicks(userAvatar);
  }

  @Override public void removeInstalledDownloads(List<App> installedDownloadsList) {
    adapter.removeInstalledDownloads(installedDownloadsList);
  }

  @UiThread @Override public void scrollToTop() {
    LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
    if (lastVisibleItemPosition > 10) {
      recyclerView.scrollToPosition(10);
    }
    recyclerView.smoothScrollToPosition(0);
  }

  @Override public Observable<Void> refreshApps() {
    return RxSwipeRefreshLayout.refreshes(swipeRefreshLayout);
  }

  @Override public void hidePullToRefresh() {
    if (swipeRefreshLayout.isRefreshing()) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  @Override public void removeCanceledAppDownload(App app) {
    adapter.removeCanceledAppDownload(app);
  }

  @Override public void removeAppcCanceledAppDownload(App app) {
    appcAppsAdapter.removeCanceledAppDownload(app);
    triggerAppcUpgradesVisibility(appcAppsAdapter.getTotalItemCount());
  }

  @Override public void setStandbyState(App app) {
    adapter.setAppStandby(app);
  }

  @Override public void setAppcStandbyState(App app) {
    appcAppsAdapter.setAppStandby(app);
  }

  @Override public void showIndeterminateAllUpdates() {
    Logger.getInstance()
        .d("Apps", "show indeterminate all uploads");
    adapter.setAllUpdatesIndeterminate();
  }

  @Override public void setDefaultUserImage() {
    ImageLoader.with(getContext())
        .loadUsingCircleTransform(R.drawable.ic_account_circle, userAvatar);
  }

  @Override public void setPausingDownloadState(App app) {
    adapter.setAppOnPausing(app);
  }

  @Override public void setAppcPausingDownloadState(App app) {
    appcAppsAdapter.setAppOnPausing(app);
  }

  @Override public void showAppcUpgradesList(List<App> list) {
    Logger.getInstance()
        .d("Apps", "showing appc upgrades list");
    for (App app : list) {
      if (app instanceof AppcUpdateApp && ((AppcUpdateApp) app).hasPromotion()) {
        showPromoHeaderMessage(((AppcUpdateApp) app).getAppcReward());
      }
    }
    if (list != null && !list.isEmpty()) {
      appcAppsAdapter.setAvailableUpgradesList(list);
    }
    showUpgrades = true;
    appcUpgradesSectionLoaded.onNext(null);
    if (shouldShowAppcAppsList()) {
      showAppsList();
    }
  }

  @Override public void removeExcludedAppcUpgrades(List<App> excludedUpdatesList) {
    appcAppsAdapter.removeAppcUpgradesList(excludedUpdatesList);
    triggerAppcUpgradesVisibility(appcAppsAdapter.getTotalItemCount());
  }

  @Override public Observable<Void> onLoadAppcUpgradesSection() {
    return appcUpgradesSectionLoaded;
  }

  @Override public Observable<Void> onLoadUpdatesSection() {
    return updatesSectionLoaded;
  }

  private void showPromoHeaderMessage(float appcReward) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone((ConstraintLayout) appcAppsLayout);
    constraintSet.connect(R.id.appc_apps_recycler_view, ConstraintSet.TOP,
        R.id.header_promo_message, ConstraintSet.BOTTOM);
    constraintSet.applyTo((ConstraintLayout) appcAppsLayout);
    ((TextView) appcAppsLayout.findViewById(R.id.header_promo_message)
        .findViewById(R.id.message)).setText(
        String.format(getResources().getString(R.string.promo_update2appc_message),
            String.valueOf((Math.round(appcReward)))));
    appcAppsLayout.findViewById(R.id.header_message)
        .setVisibility(View.GONE);
    appcAppsLayout.findViewById(R.id.header_promo_message)
        .setVisibility(View.VISIBLE);
  }

  private void showAppsList() {
    recyclerView.scrollToPosition(0);
    hideLoadingProgressBar();
    triggerAppcUpgradesVisibility(appcAppsAdapter.getTotalItemCount());
    recyclerView.setVisibility(View.VISIBLE);
    Logger.getInstance()
        .d("Apps", "showing apps lists");
  }

  private void triggerAppcUpgradesVisibility(int itemCount) {
    if (itemCount > 0) {
      appcAppsRecyclerView.scrollToPosition(0);
      hideLoadingProgressBar();
      appcAppsLayout.setVisibility(View.VISIBLE);
      if (itemCount > APPC_UPDATES_LIMIT) {
        appcSeeMoreButton.setVisibility(View.VISIBLE);
      } else {
        appcSeeMoreButton.setVisibility(View.GONE);
      }
    } else {
      appcAppsLayout.setVisibility(View.GONE);
    }
  }

  private boolean shouldShowAppsList() {
    return showDownloads
        && showUpdates
        && showInstalled
        && showUpgrades
        && recyclerView.getVisibility() != View.VISIBLE;
  }

  private boolean shouldShowAppcAppsList() {
    return showDownloads && showUpdates && showInstalled && showUpgrades;
  }

  private void hideLoadingProgressBar() {
    progressBar.setVisibility(View.GONE);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    progressBar = null;
    swipeRefreshLayout = null;
    ignoreUpdateDialog = null;
    recyclerView = null;
    adapter = null;
    userAvatar = null;
    appcAppsLayout = null;
    appcAppsRecyclerView = null;
    appcAppsAdapter = null;
  }

  @Override public void onDetach() {
    bottomNavigationActivity = null;
    super.onDetach();
  }
}
