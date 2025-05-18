package bomberman.arsw.Socket;

public class PlayerActionRequest {
    private String playerId;

    // Constructor vacío necesario para la deserialización JSON
    public PlayerActionRequest() {
    }

    // Constructor con parámetros
    public PlayerActionRequest(String playerId) {
        this.playerId = playerId;
    }

    // Getter y Setter
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}