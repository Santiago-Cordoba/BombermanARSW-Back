package bomberman.arsw.Model;

import java.util.HashMap;
import java.util.Map;

public class Explosion {
    private final int x;
    private final int y;
    private final boolean isCenter;

    public Explosion(int x, int y, boolean isCenter) {
        this.x = x;
        this.y = y;
        this.isCenter = isCenter;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isCenter() { return isCenter; }

    public String toJsonString() {
        return String.format("{\"x\":%d,\"y\":%d,\"isCenter\":%s}", x, y, isCenter);
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("isCenter", isCenter);
        return map;
    }
}
