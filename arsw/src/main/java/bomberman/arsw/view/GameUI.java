package bomberman.arsw.view;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameUI
{
    private JFrame frame;
    private JButton confirmButton;
    private GameConfig config;

    public GameUI(GameConfig config)
    {
        this.config = config;
        frame = new JFrame("Configuración del Juego");
        confirmButton = new JButton("Confirmar");

        confirmButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Game game = new Game(config);
                game.startGame();
                JOptionPane.showMessageDialog(frame, "Juego iniciado con la configuración aplicada.");
            }
        });

        frame.add(confirmButton);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
