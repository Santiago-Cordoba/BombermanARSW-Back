package bomberman.arsw.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class CryptoUtils {
    private static final String SECRET_KEY = "clave-super-secreta-123"; // Esta clave debe ser exactamente igual en frontend
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // Usar CBC para compatibilidad con CryptoJS

    // IV fijo para que ambos lados usen el mismo
    // En producción deberías usar un IV aleatorio y transmitirlo junto con el mensaje
    private static final String IV = "1234567890abcdef";

    public static String encrypt(String data) throws Exception {
        // Crear clave AES de 256 bits (32 bytes) a partir de la clave secreta
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        keyBytes = Arrays.copyOf(keyBytes, 16); // Usar los primeros 16 bytes (AES-128)

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Inicializar el cipher con modo CBC y padding
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        // Cifrar y codificar en Base64
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData) throws Exception {
        // Crear clave AES de 256 bits (32 bytes) a partir de la clave secreta
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        keyBytes = Arrays.copyOf(keyBytes, 16); // Usar los primeros 16 bytes (AES-128)

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Inicializar el cipher con modo CBC y padding
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        // Decodificar Base64 y descifrar
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}