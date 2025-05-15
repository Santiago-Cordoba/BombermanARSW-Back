package bomberman.arsw.Model;


public interface PowerUp {
    void applyEffect(Player player);
    PowerUpType getType();

    // Añadir métodos para posición
    int getX();
    int getY();
    void setPosition(int x, int y);

    // Método para la representación JSON
    String toJsonString();
}
