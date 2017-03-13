package cm.aptoide.pt.dataprovider.ws.v7.store;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.BodyInterceptor;
import cm.aptoide.pt.dataprovider.ws.v7.Endless;
import cm.aptoide.pt.dataprovider.ws.v7.V7;
import cm.aptoide.pt.model.v7.store.ListStores;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import rx.Observable;

/**
 * Created by trinkes on 12/12/2016.
 */

public class GetMyStoreListRequest extends V7<ListStores, GetMyStoreListRequest.EndlessBody> {

  private static boolean useEndless;
  @Nullable private String url;

  public GetMyStoreListRequest(EndlessBody body, String baseHost) {
    super(body, baseHost);
  }

  public GetMyStoreListRequest(String url, EndlessBody body, String baseHost) {
    super(body, baseHost);
    this.url = url;
  }

  public static GetMyStoreListRequest of(String url, BodyInterceptor bodyInterceptor) {
    return of(url, false, bodyInterceptor);
  }

  public static GetMyStoreListRequest of(String url, boolean useEndless,
      BodyInterceptor bodyInterceptor) {
    GetMyStoreListRequest.useEndless = useEndless;

    return new GetMyStoreListRequest(url,
        (EndlessBody) bodyInterceptor.intercept(new EndlessBody(WidgetsArgs.createDefault())),
        BASE_HOST);
  }

  @Override
  protected Observable<ListStores> loadDataFromNetwork(Interfaces interfaces, boolean bypassCache) {
    if (url.contains("getSubscribed")) {
      body.setRefresh(bypassCache);
    }
    if (TextUtils.isEmpty(url)) {
      return interfaces.getMyStoreList(body, bypassCache);
    } else {
      if (useEndless) {
        return interfaces.getMyStoreListEndless(url, body, bypassCache);
      } else {
        return interfaces.getMyStoreList(url, body, bypassCache);
      }
    }
  }

  @EqualsAndHashCode(callSuper = true) public static class EndlessBody extends Body
      implements Endless {

    @Getter private Integer limit = 25;
    @Getter @Setter private int offset;

    public EndlessBody(WidgetsArgs widgetsArgs) {
      super(widgetsArgs);
    }
  }

  @EqualsAndHashCode(callSuper = true) public static class Body extends BaseBody {
    @Getter private WidgetsArgs widgetsArgs;
    @Getter @Setter private boolean refresh;

    public Body(WidgetsArgs widgetsArgs) {
      super();
      this.widgetsArgs = widgetsArgs;
    }
  }
}
