package bomberman.arsw.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/redis")
public class RedisCheckController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> checkRoomExists(@PathVariable String roomCode) {
        Object roomData = redisTemplate.opsForValue().get("room:" + roomCode);
        if (roomData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomData);
    }
}