/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 02/09/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.appView;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.model.v7.GetApp;
import cm.aptoide.pt.model.v7.GetAppMeta;
import cm.aptoide.pt.model.v7.store.Store;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.design.ShowMessage;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.repository.RepositoryFactory;
import cm.aptoide.pt.v8engine.repository.StoreRepository;
import cm.aptoide.pt.v8engine.util.StoreThemeEnum;
import cm.aptoide.pt.v8engine.util.StoreUtilsProxy;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.appView.AppViewStoreDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Displayables;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;
import java.util.Locale;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by sithengineer on 10/05/16.
 */
@Displayables({ AppViewStoreDisplayable.class }) public class AppViewStoreWidget
    extends Widget<AppViewStoreDisplayable> {

  private ImageView storeAvatarView;
  private TextView storeNameView;
  private TextView storeNumberUsersView;
  private Button followButton;
  private View storeLayout;
  private StoreRepository storeRepository;

  private AppViewStoreDisplayable displayable;

  public AppViewStoreWidget(View itemView) {
    super(itemView);
    storeRepository = RepositoryFactory.getStoreRepository();
  }

  @Override protected void assignViews(View itemView) {
    storeAvatarView = ((ImageView) itemView.findViewById(R.id.store_avatar));
    storeNameView = ((TextView) itemView.findViewById(R.id.store_name));
    storeNumberUsersView = ((TextView) itemView.findViewById(R.id.store_number_users));
    followButton = ((Button) itemView.findViewById(R.id.follow_store_btn));
    storeLayout = itemView.findViewById(R.id.store_layout);
  }

  @Override public void bindView(AppViewStoreDisplayable displayable) {
    setupStoreInfo(displayable.getPojo());
    this.displayable = displayable;
  }

  private void setupStoreInfo(GetApp getApp) {

    GetAppMeta.App app = getApp.getNodes().getMeta().getData();
    Store store = app.getStore();

    if (TextUtils.isEmpty(store.getAvatar())) {
      ImageLoader.loadWithCircleTransform(R.drawable.ic_avatar_apps, storeAvatarView);
    } else {
      ImageLoader.loadWithCircleTransform(store.getAvatar(), storeAvatarView);
    }

    StoreThemeEnum storeThemeEnum = StoreThemeEnum.get(store);

    storeNameView.setText(store.getName());
    storeNameView.setTextColor(storeThemeEnum.getStoreHeaderInt());

    storeNumberUsersView.setText(String.format(Locale.ENGLISH,
        V8Engine.getContext().getString(R.string.appview_followers_count_text),
        AptoideUtils.StringU.withSuffix(store.getStats().getSubscribers())));

    followButton.setBackgroundDrawable(storeThemeEnum.getButtonLayoutDrawable());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      followButton.setElevation(0);
    }

    final String storeName = store.getName();
    final String storeTheme = store.getAppearance().getTheme();

    Action1<Void> openStore = __ -> {
      getNavigationManager().navigateTo(
          V8Engine.getFragmentProvider().newStoreFragment(storeName, storeTheme));
    };

    Action1<Void> subscribeStore = __ -> {
      StoreUtilsProxy.subscribeStore(storeName, getStoreMeta -> {
        ShowMessage.asSnack(itemView,
            AptoideUtils.StringU.getFormattedString(R.string.store_followed, storeName));
      }, err -> {
        CrashReport.getInstance().log(err);
      });
    };

    followButton.setTextColor(storeThemeEnum.getStoreHeaderInt());
    compositeSubscription.add(RxView.clicks(storeLayout).subscribe(openStore));

    compositeSubscription.add(storeRepository.isSubscribed(store.getId())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(isSubscribed -> {
          if (isSubscribed) {
            //int checkmarkDrawable = storeThemeEnum.getCheckmarkDrawable();
            //followButton.setCompoundDrawablesWithIntrinsicBounds(checkmarkDrawable, 0, 0, 0);
            followButton.setText(R.string.followed);
            compositeSubscription.add(RxView.clicks(followButton).subscribe(openStore));
          } else {
            //int plusMarkDrawable = storeThemeEnum.getPlusmarkDrawable();
            //followButton.setCompoundDrawablesWithIntrinsicBounds(plusMarkDrawable, 0, 0, 0);
            followButton.setText(R.string.appview_follow_store_button_text);
            compositeSubscription.add(RxView.clicks(followButton).subscribe(subscribeStore));
          }
        }));
  }
}
