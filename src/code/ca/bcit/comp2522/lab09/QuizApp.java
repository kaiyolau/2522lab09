package ca.bcit.comp2522.lab09;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.*;
import java.util.*;

/**
 * QuizApp is a JavaFX application that implements a timed quiz game.
 * Users are presented with random questions from a file and must answer them within a time limit.
 * The application tracks the score, handles timeout conditions, and allows restarting the quiz.
 *
 * Features:
 * - Loads questions and answers from a text file.
 * - Accepts user input via a text field and a submit button or the Enter key.
 * - Includes a countdown timer for each question.
 * - Displays the user's score at the end and allows restarting the quiz.
 *
 * This application demonstrates the use of JavaFX components and multithreading
 * with the Timer and Platform.runLater for UI updates.
 *
 *
 *
 * Author: Kyle Lau, Yuho Lim, Daniiel
 * Version: 1.0
 */
public class QuizApp extends Application {

    /** Label to display the current question to the user. */
    private Label questionLabel;

    /** TextField to accept user input for the answer. */
    private TextField answerField;

    /** Button to submit the user's answer. */
    private Button submitButton;

    /** Label to display the user's current score. */
    private Label scoreLabel;

    /** Label to display the countdown timer for each question. */
    private Label timerLabel;

    /** Button to start or restart the quiz. */
    private Button startQuizButton;

    /** List of questions and answers loaded from the file. */
    private List<String[]> questions = new ArrayList<>();

    /** Index of the current question being asked. */
    private int currentQuestionIndex = 0;

    /** User's current score. */
    private int score = 0;

    /** Time left for the current question in seconds. */
    private int timeLeft = 30;

    /** Timer to manage the countdown for each question. */
    private Timer timer;

    /**
     * Entry point for the JavaFX application. Sets up the UI and event handling.
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        loadQuestions();

        questionLabel = new Label("Press 'Start Quiz' to begin.");
        questionLabel.setFont(new Font("Arial", 16));

        answerField = new TextField();
        answerField.setPromptText("Type your answer here...");

        submitButton = new Button("Submit");
        submitButton.setDisable(true);

        scoreLabel = new Label("Score: 0");

        timerLabel = new Label("Time Left: --");

        startQuizButton = new Button("Start Quiz");
        startQuizButton.setOnAction(e -> startQuiz());

        submitButton.setOnAction(e -> handleSubmit());
        answerField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSubmit();
            }
        });

        VBox layout = new VBox(10, questionLabel, timerLabel, answerField, submitButton, scoreLabel, startQuizButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene scene = new Scene(layout, 400, 300);

        primaryStage.setTitle("Quiz App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Loads questions and answers from the "quiz.txt" file.
     * The file must be formatted with questions and answers separated by a pipe ("|").
     *
     * Example:
     * Question|Answer
     */
    private void loadQuestions() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/code/ca/bcit/comp2522/lab09/quiz.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    questions.add(parts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the quiz by shuffling the questions and resetting the score.
     * Enables the submit button and starts the first question.
     */
    private void startQuiz() {
        Collections.shuffle(questions);
        currentQuestionIndex = 0;
        score = 0;
        scoreLabel.setText("Score: 0");
        startQuizButton.setDisable(true);
        submitButton.setDisable(false);
        nextQuestion();
    }

    /**
     * Displays the next question in the quiz or ends the quiz if all questions are answered.
     */
    private void nextQuestion() {
        if (currentQuestionIndex < 10 && currentQuestionIndex < questions.size()) {
            String[] currentQuestion = questions.get(currentQuestionIndex);
            questionLabel.setText(currentQuestion[0]);
            answerField.clear();
            timeLeft = 30;
            timerLabel.setText("Time Left: " + timeLeft);
            startTimer();
        } else {
            endQuiz();
        }
    }

    /**
     * Handles the submission of an answer, checks if it's correct, updates the score,
     * and proceeds to the next question.
     */
    private void handleSubmit() {
        stopTimer();
        String userAnswer = answerField.getText().trim();
        String correctAnswer = questions.get(currentQuestionIndex)[1].trim();

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            score++;
            scoreLabel.setText("Score: " + score);
        }

        currentQuestionIndex++;
        nextQuestion();
    }

    /**
     * Starts the countdown timer for the current question. Automatically submits
     * the answer if the timer reaches zero.
     */
    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeft--;
                Platform.runLater(() -> {
                    timerLabel.setText("Time Left: " + timeLeft);
                    if (timeLeft <= 0) {
                        stopTimer();
                        handleSubmit(); // Automatically submit on timeout
                    }
                });
            }
        }, 1000, 1000);
    }

    /**
     * Stops the countdown timer if it is running.
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Ends the quiz, displays the user's final score, and enables the restart button.
     */
    private void endQuiz() {
        stopTimer();
        questionLabel.setText("Quiz Completed! Final Score: " + score + "/10");
        startQuizButton.setDisable(false);
        submitButton.setDisable(true);
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
