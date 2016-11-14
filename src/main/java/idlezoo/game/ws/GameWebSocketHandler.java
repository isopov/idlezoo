package idlezoo.game.ws;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;
import idlezoo.game.services.GameService;
import idlezoo.security.IdUser;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

  private final ConcurrentHashMap<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();

  @Autowired
  private GameService gameService;
  @Autowired
  private FightService fightService;
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    WebSocketSession prev = wsSessions.put(session.getPrincipal().getName(), session);
    if (prev != null) {
      // TODO this is intended to close ws from prev session if new one is opened
      // however it closes ws even for single session
      // prev.close(CloseStatus.POLICY_VIOLATION);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message)
      throws Exception {
    String payload = message.getPayload();
    //TODO is this the intended way?
    int userId = ((IdUser) ((Authentication) session.getPrincipal()).getPrincipal()).getId();
    switch (payload) {
      case "ping":
        // do nothing
        break;
      case "me":
        sendStateToPlayer(session, gameService.getZoo(userId));
        break;
      case "fight":
        Zoo enemy = fightService.fight(userId);
        sendStateToPlayer(session, gameService.getZoo(userId));
        if (enemy != null) {
          sendStateToPlayer(enemy);
        }
        break;
      default:
        handleMessage(session, userId, payload);
    }
  }

  private void handleMessage(WebSocketSession session, int userId, String payload) {
    if (payload.startsWith("buy/")) {
      String animal = payload.substring("buy/".length());
      Zoo zoo = gameService.buy(userId, animal);
      sendStateToPlayer(session, zoo);
    } else if (payload.startsWith("upgrade/")) {
      String animal = payload.substring("upgrade/".length());
      Zoo zoo = gameService.upgrade(userId, animal);
      sendStateToPlayer(session, zoo);
    } else {
      throw new IllegalStateException("Unkown message " + payload);
    }

  }

  private void sendStateToPlayer(Zoo zoo) {
    WebSocketSession session = wsSessions.get(zoo.getName());
    if (session != null) {
      sendStateToPlayer(session, zoo);
    }
  }


  private void sendStateToPlayer(WebSocketSession session, Zoo zoo) {
    try {
      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(zoo)));;
    } catch (IOException e) {
      wsSessions.remove(session.getPrincipal().getName());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
      throws Exception {
    wsSessions.remove(session.getPrincipal().getName());
  }

}
