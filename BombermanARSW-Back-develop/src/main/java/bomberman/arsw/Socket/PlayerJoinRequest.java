package bomberman.arsw.Socket;

public class PlayerJoinRequest {
    private String playerName;

    // Constructor vacío necesario para la deserialización JSON
    public PlayerJoinRequest() {
    }

    // Constructor con parámetros
    public PlayerJoinRequest(String playerName) {
        this.playerName = playerName;
    }

    // Getter y Setter
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}