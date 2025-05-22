package bomberman.arsw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootApplication
public class ArswApplication implements CommandLineRunner {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	public static void main(String[] args) {
		SpringApplication.run(ArswApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			RedisConnection connection = redisConnectionFactory.getConnection();
			System.out.println("Conexión a Redis exitosa!");
			System.out.println("Servidor: " + connection.ping());
			connection.close();
		} catch (Exception e) {
			System.err.println("Error conectando a Redis: " + e.getMessage());
		}
	}

}
