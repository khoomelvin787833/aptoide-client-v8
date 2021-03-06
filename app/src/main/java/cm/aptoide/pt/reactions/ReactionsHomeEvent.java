package cm.aptoide.pt.reactions;

import cm.aptoide.pt.home.bundles.base.HomeBundle;
import cm.aptoide.pt.home.bundles.editorial.EditorialHomeEvent;

public class ReactionsHomeEvent extends EditorialHomeEvent {
  private final String reaction;

  public ReactionsHomeEvent(String cardId, String groupId, HomeBundle bundle, int bundlePosition,
      Type clickType, String reaction) {
    super(cardId, groupId, bundle, bundlePosition, clickType);
    this.reaction = reaction;
  }

  public String getReaction() {
    return reaction;
  }
}
