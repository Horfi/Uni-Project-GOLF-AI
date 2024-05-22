package com.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SettingsScreen implements Screen {

    // back button
    private static final int BACK_BUTTON_WIDTH = 95;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final int BACK_BUTTON_Y = 30;
    private Texture backButtonActive;
    private Texture backButtonInactive;

    // Buttons
    private TextButton submitButton;
    private TextButton defaultButton;
    private SelectBox FunctionSelect;

    // Text fields
    private TextField function;
    private TextField InitialCoordinateX;
    private TextField InitialCoordinateY;
    private TextField SAND_K;
    private TextField SAND_S;
    private TextField GRASS_K;
    private TextField GRASS_S;
    private TextField TargetXBox, TargetYBox;

    // labels
    private Label Xo;
    private Label Yo;
    private Label F;
    private Label Gk, Gs;
    private Label Sk, Ss;
    private Label TXoLabel, TYoLabel;
    private Image errorIcon;
    private Label errorLabel;

    // other
    GolfGame game;
    private Stage stage;
    private Texture backgroundTexture = new Texture("assets/clouds.jpg");
    private Skin skin = new Skin(Gdx.files.internal("assets/skins/visui/assets/uiskin.json"));

    // passing variables
    public static String terrainFunction;
    public static Double InitialX;
    public static Double InitialY;
    public static Double grassK, grassS;
    public static Double sandK, sandS;
    public static Double TargetXo, TargetYo;

    public SettingsScreen(GolfGame game) {
        this.game = game;

        stage = new Stage(new StretchViewport(GolfGame.WIDTH, GolfGame.HEIGHT));
        Gdx.input.setInputProcessor(stage);
        backButtonActive = new Texture("assets/backbuttonactive.png");
        backButtonInactive = new Texture("assets/backbuttoninactivepng.png");
        setupTextFields();
        handleSubmitButton();
        handleDefaultButton();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background texture
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Setting up the back button using textures. (The back button sets the screen
        // to MainMenu class)

        int x = 30;
        if (Gdx.input.getX() >= x && Gdx.input.getX() <= x + BACK_BUTTON_WIDTH
                && GolfGame.HEIGHT - Gdx.input.getY() >= BACK_BUTTON_Y
                && GolfGame.HEIGHT - Gdx.input.getY() <= BACK_BUTTON_Y + BACK_BUTTON_HEIGHT) {
            game.batch.draw(backButtonActive, x, BACK_BUTTON_Y, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
            if (Gdx.input.isTouched()) {
                game.setScreen(new MainMenu(game));
            }
        } else {
            game.batch.draw(backButtonInactive, x, BACK_BUTTON_Y, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
        }

        game.batch.end();

        // Draw the stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    // method for setting up the textfield for user input
    private void setupTextFields() {
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));

        // handling X initial coordinates label and textfield
        Xo = new Label("Initial start x coordinate", skin);
        Xo.setPosition(200, 630);
        Xo.setColor(Color.MAGENTA);

        InitialCoordinateX = new TextField("Enter initial X coordinate for golf ball", skin);
        InitialCoordinateX.setPosition(200, 600);
        InitialCoordinateX.setSize(300, 30);

        // handling Y initial coordinates label and textfield
        Yo = new Label("Initial start y coordinate", skin);
        Yo.setPosition(200, 580);
        Yo.setColor(Color.MAGENTA);

        InitialCoordinateY = new TextField("Enter initial Y coordinate for golf ball", skin);
        InitialCoordinateY.setPosition(200, 550);
        InitialCoordinateY.setSize(300, 30);

        // handling function label and textfield
        F = new Label("Terrain function", skin);
        F.setPosition(200, 130);
        F.setColor(Color.MAGENTA);

        function = new TextField("Type in function", skin);
        function.setPosition(180, 100);
        function.setSize(250, 30);

        // handling Grass static and kinetic friction
        Gk = new Label("Grass kinetic", skin);
        Gk.setPosition(80, 430);
        Gk.setColor(Color.MAGENTA);

        GRASS_K = new TextField("Type in the kinetic friction of grass", skin);
        GRASS_K.setPosition(80, 400);
        GRASS_K.setSize(250, 30);

        Gs = new Label("Grass static", skin);
        Gs.setPosition(370, 430);
        Gs.setColor(Color.MAGENTA);

        GRASS_S = new TextField("Type in the static friction of grass", skin);
        GRASS_S.setPosition(370, 400);
        GRASS_S.setSize(250, 30);

        // handling Sand static and kinetic friction
        Sk = new Label("Sand kinetic", skin);
        Sk.setPosition(80, 500);
        Sk.setColor(Color.MAGENTA);

        SAND_K = new TextField("Type in the kinetic friction of sand", skin);
        SAND_K.setPosition(80, 470);
        SAND_K.setSize(250, 30);

        Ss = new Label("Sand static", skin);
        Ss.setPosition(370, 500);
        Ss.setColor(Color.MAGENTA);

        SAND_S = new TextField("Type in the static friction of sand", skin);
        SAND_S.setPosition(370, 470);
        SAND_S.setSize(250, 30);

        // Target coordinates
        TXoLabel = new Label("Coordinate X of target", skin);
        TXoLabel.setPosition(200, 330);
        TXoLabel.setColor(Color.MAGENTA);

        TargetXBox = new TextField("Type in the coordinate X of the target", skin);
        TargetXBox.setPosition(200, 300);
        TargetXBox.setSize(300, 30);

        TYoLabel = new Label("Coordinate Y of target", skin);
        TYoLabel.setPosition(200, 280);
        TYoLabel.setColor(Color.MAGENTA);

        TargetYBox = new TextField("Type in the coordinate Y of the target", skin);
        TargetYBox.setPosition(200, 250);
        TargetYBox.setSize(300, 30);

        stage.addActor(Xo);
        stage.addActor(InitialCoordinateX);
        stage.addActor(Yo);
        stage.addActor(InitialCoordinateY);
        stage.addActor(F);
        stage.addActor(function);
        stage.addActor(Gk);
        stage.addActor(GRASS_K);
        stage.addActor(Gs);
        stage.addActor(GRASS_S);
        stage.addActor(Sk);
        stage.addActor(SAND_K);
        stage.addActor(Ss);
        stage.addActor(SAND_S);
        stage.addActor(TXoLabel);
        stage.addActor(TargetXBox);
        stage.addActor(TYoLabel);
        stage.addActor(TargetYBox);

    }

    // method for handling the submit button. Updates variables: InitialX, InitialY,
    // terrainFunction, grassK, grassS, sandK, sandS, TargetXo, TargetYo
    private void handleSubmitButton() {
        submitButton = new TextButton("Submit", skin);
        submitButton.setPosition(530, 100);
        submitButton.setSize(100, 30);
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // ERROR HANDLING
                if (errorLabel != null) {
                    errorLabel.remove();
                    errorLabel = null;
                }
                if (errorIcon != null) {
                    errorIcon.remove();
                    errorIcon = null;
                }

                try {
                    InitialX = Double.parseDouble(InitialCoordinateX.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("please enter a number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(17, 605);
                    errorIcon.setPosition(170, 600);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }

                try {
                    InitialY = Double.parseDouble(InitialCoordinateY.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("please enter a number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(17, 555);
                    errorIcon.setPosition(170, 550);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }

                try {
                    terrainFunction = function.getText();
                } catch (NumberFormatException e) {
                    errorLabel = new Label("incorrect function", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(17, 405);
                    errorIcon.setPosition(170, 400);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }

                try {
                    grassK = Double.parseDouble(GRASS_K.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(20, 380);
                    errorIcon.setPosition(40, 400);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }
                try {
                    grassS = Double.parseDouble(GRASS_S.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(600, 380);
                    errorIcon.setPosition(630, 400);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }

                try {
                    sandK = Double.parseDouble(SAND_K.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(20, 450);
                    errorIcon.setPosition(40, 470);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }
                try {
                    sandS = Double.parseDouble(SAND_S.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(600, 450);
                    errorIcon.setPosition(630, 470);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }
                try {
                    TargetXo = Double.parseDouble(TargetXBox.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(65, 305);
                    errorIcon.setPosition(170, 300);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }
                try {
                    TargetYo = Double.parseDouble(TargetYBox.getText());
                } catch (NumberFormatException e) {
                    errorLabel = new Label("type in number", skin);
                    errorIcon = new Image(new Texture("assets/Error.png"));
                    errorLabel.setColor(Color.RED);
                    errorLabel.setPosition(65, 255);
                    errorIcon.setPosition(170, 250);
                    errorIcon.setSize(20, 30);
                    stage.addActor(errorIcon);
                    stage.addActor(errorLabel);
                }
                game.setScreen(new MainMenu(game));

            }
        });

        stage.addActor(submitButton); // Add the submit button to the stage

    }

    // method for handling the default button. It sets all the variables used in
    // physics engine to default.
    public void handleDefaultButton() {
        defaultButton = new TextButton("Default", skin);
        defaultButton.setPosition(30, 100);
        defaultButton.setSize(100, 30);
        defaultButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                function.setText(" sqrt ( ( sin ( 0.1 * x ) + cos ( 0.1 * y ) ) ^ 2 ) + 0.5 * sin ( 0.3 * x ) * cos ( 0.3 * y ) ");
                InitialCoordinateX.setText("5.0");
                InitialCoordinateY.setText("2.0");
                GRASS_K.setText("1.0");
                GRASS_S.setText("0.5");
                SAND_K.setText("0.3");
                SAND_S.setText("0.4");
                TargetXBox.setText("4.0");
                TargetYBox.setText("1.0");

                // Update corresponding static variables
                terrainFunction = function.getText();
                InitialX = Double.parseDouble(InitialCoordinateX.getText());
                InitialY = Double.parseDouble(InitialCoordinateY.getText());
                grassK = Double.parseDouble(GRASS_K.getText());
                grassS = Double.parseDouble(GRASS_S.getText());
                sandK = Double.parseDouble(SAND_K.getText());
                sandS = Double.parseDouble(SAND_S.getText());
                TargetXo = Double.parseDouble(TargetXBox.getText());
                TargetYo = Double.parseDouble(TargetYBox.getText());

            }
        });

        stage.addActor(defaultButton); // Add the default button to the stage
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

}