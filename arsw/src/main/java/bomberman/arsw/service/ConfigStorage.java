package bomberman.arsw.service;

import bomberman.arsw.Model.GameConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ConfigStorage
{
    private static final String FILE_PATH = "config.json";

    public static void saveConfig(GameConfig config) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(FILE_PATH), config);
    }

    public static GameConfig loadConfig() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(FILE_PATH), GameConfig.class);
    }
}
