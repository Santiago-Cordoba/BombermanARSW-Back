package bomberman.arsw.Model;

import org.springframework.stereotype.Component;

@Component
public class GameConfig
{

    private int duracion;
    private int jugadores;
    private int bloques;
    private int vidas;

    // 🔹 Constructor vacío para que Spring pueda crear el bean
    public GameConfig() {
        this.duracion = 5;  // Valor por defecto
        this.jugadores = 2;
        this.bloques = 10;
        this.vidas = 3;
    }

    // Constructor completo (no es usado por Spring directamente)
    public GameConfig(int duracion, int jugadores, int bloques, int vidas) {
        setDuracion(duracion);
        setJugadores(jugadores);
        setBloques(bloques);
        setVidas(vidas);
    }

    // 🔹 Métodos Getters y Setters (se mantienen igual)
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public int getJugadores() { return jugadores; }
    public void setJugadores(int jugadores) { this.jugadores = jugadores; }

    public int getBloques() { return bloques; }
    public void setBloques(int bloques) { this.bloques = bloques; }

    public int getVidas() { return vidas; }
    public void setVidas(int vidas) { this.vidas = vidas; }

    public int getPlayerCount()
    {
        return jugadores;
    }

    public int getMapaAlto() {
        return 15;
    }

    public int getMapaAncho() {
        return 15;
    }
}
