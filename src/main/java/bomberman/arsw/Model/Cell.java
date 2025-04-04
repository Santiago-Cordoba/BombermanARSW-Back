package bomberman.arsw.Model;

public class Cell {
    private boolean hasWall;
    private boolean player;
    private boolean hasBomb;  // Nuevo atributo

    public Cell(int x, int y) {
        this.hasWall = false;
        this.player = false; // ✅ Inicializado en false
        this.hasBomb = false;
    }

    public boolean hasBomb() {
        return hasBomb;
    }

    public void setBomb(boolean hasBomb) {
        this.hasBomb = hasBomb;
    }

    public boolean hasWall() {
        return hasWall;
    }

    public boolean hasPlayer() {
        return player; // ✅ Corregido (ya es boolean, no necesita comparación con null)
    }

    public boolean isEmpty() {
        return !hasWall && !player && !hasBomb; // ✅ Corregido (player nunca es null)
    }

    public void setBlock(boolean hasWall) {
        this.hasWall = hasWall;
    }

    public void setPlayer(boolean player) {
        this.player = player;
    }
}
