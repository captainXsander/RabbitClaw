package ru.captainxsander.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration; // <-- класс из core
import ru.captainxsander.MainGame;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Claw Machine");
        config.setWindowedMode(800, 600);
        new Lwjgl3Application(new MainGame(), config); // <-- запускаем core
    }
}
