package bomberman.arsw.Model;

public class Cell {
    private boolean hasWall;
    private boolean player;

    public Cell(int x, int y) {
        this.hasWall = false;
        this.player = false; // ✅ Inicializado en false
    }

    public boolean hasWall() {
        return hasWall;
    }

    public boolean hasPlayer() {
        return player; // ✅ Corregido (ya es boolean, no necesita comparación con null)
    }

    public boolean isEmpty() {
        return !hasWall && !player; // ✅ Corregido (player nunca es null)
    }

    public void setBlock(boolean hasWall) {
        this.hasWall = hasWall;
    }

    public void setPlayer(boolean player) {
        this.player = player;
    }
}
