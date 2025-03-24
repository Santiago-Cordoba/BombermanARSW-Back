package bomberman.arsw.Model;

import bomberman.arsw.Model.GameConfig;

public class GameSession
{
    private static GameConfig currentConfig;

    public static void setCurrentConfig(GameConfig currentConfig)
    {
        currentConfig = currentConfig;
        System.out.println("Configuración actualizada: " + currentConfig);

    }

    public static GameConfig getConfig()
    {
        return currentConfig;
    }
}
