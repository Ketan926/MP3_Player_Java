package application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Controller implements Initializable {

    @FXML
    private Pane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File directory;
    private File[] files;
    private ArrayList<File> songs;

    private int songNumber;
    private int[] speeds = { 25, 50, 75, 100, 125, 150, 175, 200 };

    private Timer timer;
    private TimerTask task;

    private boolean running;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        songs = new ArrayList<File>();

        directory = new File("C:\\Users\\ketan\\OneDrive\\Desktop\\Mp3-Player-JAVAFX-main\\Oops-Project\\project_songs");

        files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
            }
        }

        if (!songs.isEmpty()) {
            initializeMediaPlayer();
            initializeUIComponents();
        } else {
            handleNoSongsFound();
        }
    }

    private void initializeMediaPlayer() {
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> handleMediaError());
    }

    private void initializeUIComponents() {
        songLabel.setText(songs.get(songNumber).getName());
        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }
        speedBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
                }
            }
        });

        songProgressBar.setStyle("-fx-accent: #00FF00;");
    }

    private void handleNoSongsFound() {
        System.out.println("No songs found in the 'music' directory.");
        // You can display an error message to the user or take other appropriate action
    }

    private void handleMediaError() {
        System.out.println("Error occurred while playing media.");
        // You can display an error message to the user or take other appropriate action
    }

    public void playMedia() {
        if (mediaPlayer != null) {
            beginTimer();
            changeSpeed(null);
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            mediaPlayer.play();
        } else {
            System.out.println("MediaPlayer is null. Cannot play media.");
        }
    }

    public void pauseMedia() {
        if (mediaPlayer != null) {
            cancelTimer();
            mediaPlayer.pause();
        }
    }

    public void resetMedia() {
        if (mediaPlayer != null) {
            songProgressBar.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
        }
    }

    public void previousMedia() {
        if (mediaPlayer != null && songNumber > 0) {
            songNumber--;
            handleMediaChange();
        }
    }

    public void nextMedia() {
        if (mediaPlayer != null && songNumber < songs.size() - 1) {
            songNumber++;
            handleMediaChange();
        }
    }

    private void handleMediaChange() {
        mediaPlayer.stop();
        if (running) {
            cancelTimer();
        }
        initializeMediaPlayer();
        songLabel.setText(songs.get(songNumber).getName());
        playMedia();
    }

    public void changeSpeed(ActionEvent event) {
        if (mediaPlayer != null && speedBox.getValue() != null) {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1))
                    * 0.01);
        }
    }

    public void beginTimer() {
        if (mediaPlayer != null) {
            timer = new Timer();
            task = new TimerTask() {
                public void run() {
                    running = true;
                    if (mediaPlayer != null) {
                        double current = mediaPlayer.getCurrentTime().toSeconds();
                        double end = media.getDuration().toSeconds();
                        songProgressBar.setProgress(current / end);
                        if (current / end == 1) {
                            cancelTimer();
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }

    public void cancelTimer() {
        running = false;
        if (timer != null) {
            timer.cancel();
        }
    }
}
