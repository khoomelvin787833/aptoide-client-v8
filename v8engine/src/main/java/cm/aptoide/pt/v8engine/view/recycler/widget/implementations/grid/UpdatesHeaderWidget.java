package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cm.aptoide.pt.actions.PermissionManager;
import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.database.realm.Update;
import cm.aptoide.pt.downloadmanager.AptoideDownloadManager;
import cm.aptoide.pt.downloadmanager.DownloadServiceHelper;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.util.DownloadFactory;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.UpdatesHeaderDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by neuro on 02-08-2016.
 */
public class UpdatesHeaderWidget extends Widget<UpdatesHeaderDisplayable> {

	private TextView title;
	private Button more;

	public UpdatesHeaderWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		title = (TextView) itemView.findViewById(R.id.title);
		more = (Button) itemView.findViewById(R.id.more);
	}

	@Override
	public void bindView(UpdatesHeaderDisplayable displayable) {
		title.setText(displayable.getLabel());
		more.setText(R.string.update_all);
		more.setVisibility(View.VISIBLE);
		more.setOnClickListener((view) -> {
			Realm realm = Database.get();
			RealmResults<Update> all = Database.UpdatesQ.getAll(realm);
			for (Update update : all) {
				new DownloadServiceHelper(AptoideDownloadManager.getInstance(), new PermissionManager()).startDownload((PermissionRequest) getContext(), new
						DownloadFactory()
						.create(update))
						.filter(download -> download.getOverallDownloadStatus() == Download.COMPLETED)
						.flatMap(download -> displayable.getInstallManager().install(getContext(), (PermissionRequest) getContext(), download.getAppId()))
						.onErrorReturn(throwable -> null)
						.subscribe();
			}
			Analytics.Updates.updateAll();
		});
	}

	@Override
	public void onViewAttached() {

	}

	@Override
	public void onViewDetached() {

	}
}
