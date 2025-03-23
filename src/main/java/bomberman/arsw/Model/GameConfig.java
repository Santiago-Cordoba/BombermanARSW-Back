package bomberman.arsw.Model;

import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class GameConfig
{

    private int duracion;
    private int jugadores;
    private int bloques;
    private int vidas;

    public GameConfig(int duracion, int jugadores, int bloques, int vidas) {
        setDuracion(duracion);
        setJugadores(jugadores);
        setBloques(bloques);
        setVidas(vidas);
    }

    public boolean isValidConfig() {
        return (duracion == 2 || duracion == 5 || duracion == 10) &&
                (jugadores >= 1 && jugadores <= 4) &&
                (bloques >= 1 && bloques <= 30) &&
                (vidas >= 1 && vidas <= 10);
    }

    public int getDuracion() { return duracion; }

    public void setDuracion(int duracion) {
        if (duracion == 2 || duracion == 5 || duracion == 10) {
            this.duracion = duracion;
        } else {
            throw new IllegalArgumentException("Duración inválida. Debe ser 2, 5 o 10 minutos.");
        }
    }

    public int getJugadores() { return jugadores; }

    public void setJugadores(int jugadores) {
        if (jugadores >= 1 && jugadores <= 4) {
            this.jugadores = jugadores;
        } else {
            throw new IllegalArgumentException("Número de jugadores inválido. Debe estar entre 1 y 4.");
        }
    }

    public int getBloques() { return bloques; }

    public void setBloques(int bloques) {
        if (bloques >= 1 && bloques <= 30) {
            this.bloques = bloques;
        } else {
            throw new IllegalArgumentException("Número de bloques inválido. Debe estar entre 1 y 30.");
        }
    }

    public int getVidas() { return vidas; }

    public void setVidas(int vidas)
    {
        if (vidas >= 1 && vidas <= 10)
        {
            this.vidas = vidas;
        } else {
            throw new IllegalArgumentException("Número de vidas inválido. Debe estar entre 1 y 10.");
        }
    }

    @Override
    public String toString()
    {
        return "Configuración de la partida: " +
                "Duración = " + duracion + " minutos, " +
                "Jugadores = " + jugadores + ", " +
                "Bloques = " + bloques + ", " +
                "Vidas = " + vidas;
    }

}
