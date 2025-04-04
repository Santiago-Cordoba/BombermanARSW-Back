package bomberman.arsw.Socket;

import bomberman.arsw.Model.Player;
import bomberman.arsw.Service.roomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class WebSocketController {
    private final roomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(roomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/{roomCode}/join")
    public void joinRoom(@DestinationVariable String roomCode, @Payload PlayerJoinRequest request) {
        Player player = new Player(

                0,  // initial X position
                0,  // initial Y position
                3,  // initial lives
                request.getPlayerName(),
                1   // initial bomb capacity
        );

        boolean isFirstPlayer = roomService.getPlayersInRoom(roomCode).isEmpty();
        player.setHost(isFirstPlayer);

        roomService.addPlayerToRoom(roomCode, player);

        // Enviar actualización a todos los jugadores de la sala
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/ready")
    public void toggleReady(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.togglePlayerReadyStatus(roomCode, request.getPlayerId());
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/start")
    public void startGame(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        if (roomService.canStartGame(roomCode) && roomService.isHost(roomCode, request.getPlayerId())) {
            // Enviar mensaje especial SOLO al host
            messagingTemplate.convertAndSendToUser(
                    request.getPlayerId(), // El ID de sesión del host
                    "/queue/host",
                    Map.of("type", "HOST_CONFIG")
            );

            // Enviar mensaje de espera a los demás jugadores
            messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                    Map.of(
                            "type", "WAITING_FOR_HOST",
                            "message", "El host está configurando el juego..."
                    )
            );
        }
    }

    @MessageMapping("/room/{roomCode}/leave")
    public void leaveRoom(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.removePlayerFromRoom(roomCode, request.getPlayerId());
        sendRoomUpdate(roomCode);
    }

    private void sendRoomUpdate(String roomCode) {
        List<Player> players = roomService.getPlayersInRoom(roomCode);
        String hostId = players.isEmpty() ? "" : players.get(0).getId();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "PLAYER_UPDATE");
        response.put("players", players);
        response.put("host", hostId);

        messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
    }


}