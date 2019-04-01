package cm.aptoide.pt.editorial;

import retrofit2.Response;

import static cm.aptoide.pt.editorial.ReactionsResponse.ReactionResponseMessage.GENERAL_ERROR;
import static cm.aptoide.pt.editorial.ReactionsResponse.ReactionResponseMessage.INVALID_USER_AGENT;
import static cm.aptoide.pt.editorial.ReactionsResponse.ReactionResponseMessage.REACTIONS_EXCEEDED;
import static cm.aptoide.pt.editorial.ReactionsResponse.ReactionResponseMessage.SUCCESS;
import static cm.aptoide.pt.editorial.ReactionsResponse.ReactionResponseMessage.TOKEN_NOT_VALID;

public class ReactionsResponse {

  private final ReactionResponseMessage reactionResponseMessage;

  public ReactionsResponse(ReactionResponseMessage reactionResponseMessage) {

    this.reactionResponseMessage = reactionResponseMessage;
  }

  public boolean wasSuccess() {
    return reactionResponseMessage == SUCCESS;
  }

  public boolean reactionsExceeded() {
    return reactionResponseMessage == REACTIONS_EXCEEDED;
  }

  public enum ReactionResponseMessage {
    SUCCESS, GENERAL_ERROR, INVALID_USER_AGENT, TOKEN_NOT_VALID, REACTIONS_EXCEEDED
  }

  public static class ReactionResponseMapper {

    public static ReactionResponseMessage mapReactionResponse(Response httpResponse) {
      ReactionResponseMessage reactionResponseMessage = GENERAL_ERROR;
      switch (httpResponse.code()) {
        case 200:
          reactionResponseMessage = SUCCESS;
          break;
        case 201:
          reactionResponseMessage = SUCCESS;
          break;
        case 204:
          reactionResponseMessage = SUCCESS;
          break;
        case 500:
          reactionResponseMessage = GENERAL_ERROR;
          break;
        case 406:
          reactionResponseMessage = INVALID_USER_AGENT;
          break;
        case 401:
          reactionResponseMessage = TOKEN_NOT_VALID;
          break;
        case 429:
          reactionResponseMessage = REACTIONS_EXCEEDED;
          break;
      }
      return reactionResponseMessage;
    }
  }
}