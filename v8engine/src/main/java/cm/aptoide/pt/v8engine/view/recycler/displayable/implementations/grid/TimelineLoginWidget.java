package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import android.accounts.AccountManager;
import android.view.View;
import android.widget.Button;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.dataprovider.repository.IdsRepositoryImpl;
import cm.aptoide.pt.preferences.Application;
import cm.aptoide.pt.preferences.secure.SecureCoderDecoder;
import cm.aptoide.pt.preferences.secure.SecurePreferencesImplementation;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;

/**
 * Created by trinkes on 21/12/2016.
 */

public class TimelineLoginWidget extends Widget<TimelineLoginDisplayable> {

  private AptoideAccountManager accountManager;
  private Button button;

  public TimelineLoginWidget(View itemView) {
    super(itemView);
  }

  @Override protected void assignViews(View itemView) {
    button = (Button) itemView.findViewById(R.id.login_button);
  }

  @Override public void bindView(TimelineLoginDisplayable displayable) {
    accountManager = AptoideAccountManager.getInstance(getContext(), Application.getConfiguration(),
        new SecureCoderDecoder.Builder(getContext().getApplicationContext()).create(),
        AccountManager.get(getContext().getApplicationContext()), new IdsRepositoryImpl(
            SecurePreferencesImplementation.getInstance(),
            getContext().getApplicationContext()));
    compositeSubscription.add(
        RxView.clicks(button).subscribe(click -> displayable.login(getContext(), accountManager), err -> {
          CrashReport.getInstance().log(err);
        }));
  }
}
