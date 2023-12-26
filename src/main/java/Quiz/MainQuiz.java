package Quiz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Kelas utama aplikasi kuis.
 */
public class MainQuiz extends Application {

    private Map<String, String> userCredentials = new HashMap<>();
    /** Map untuk menyimpan kredensial pengguna. */
    private Map<String, Integer> userScores = new HashMap<>();
    /** Map untuk menyimpan skor pengguna. */
    private String currentUser;
    /**
     * Username pengguna saat ini.
     */
    private static final String FILE_PATH = "user_credentials.txt";
    /**
     * Path file penyimpanan kredensial.
     */
    private List<String> correctAnswerList = new ArrayList<>();
    /**
     * Daftar jawaban benar.
     */
    private Quiz currentQuiz;
    /**
     * Kuis saat ini.
     */
    private int score = 0;
    /**
     * Skor saat ini.
     */

    public static void main(String[] args) {
        launch(args);
    }

    private int currentQuestionIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple Quiz App");

        // Membuat form login
        GridPane loginGrid = createLoginForm();

        // Membuat form pembuatan akun
        GridPane signupGrid = createSignupForm();

        // Membuat scene utama dengan opsi login dan pembuatan akun
        TabPane tabPane = new TabPane();
        Tab loginTab = new Tab("Login", loginGrid);
        Tab signupTab = new Tab("Signup", signupGrid);
        tabPane.getTabs().addAll(loginTab, signupTab);

        Scene scene = new Scene(tabPane, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.show();

        readUserCredentialsFromFile();
    }

    private GridPane createLoginForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (validateLogin(username, password)) {
                showAlert("Login Successful", "Welcome, " + username + "!");
                UserSession.setLoggedInUser(username);
                showQuiz();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        });

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        return grid;
    }

    private void showQuiz() {
        List<String> questionList = new ArrayList<>();
        List<String> answerList = new ArrayList<>();
        List<String> correctAnswerList = new ArrayList<>();

        // Baca pertanyaan dari file (contoh: questions.txt)
        try (Stream<String> lines = Files.lines(Paths.get("questions.txt"))) {
            lines.forEach(questionList::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Baca jawaban dari file (contoh: answers.txt)
        try (Stream<String> lines = Files.lines(Paths.get("answers.txt"))) {
            lines.forEach(answerList::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Baca jawaban benar dari file (contoh: correct_answers.txt)
        try (Stream<String> lines = Files.lines(Paths.get("correct_answers.txt"))) {
            lines.forEach(correctAnswerList::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentQuestionIndex = 0;

        List<Quiz> quizzes = new ArrayList<>();

        // Pastikan jumlah pertanyaan, jawaban, dan jawaban benar sesuai
        if (questionList.size() == answerList.size() && questionList.size() == correctAnswerList.size()) {
            for (int i = 0; i < questionList.size(); i++) {
                String[] options = answerList.get(i).split(",");
                String correctAnswer = correctAnswerList.get(i);

                quizzes.add(new Quiz(questionList.get(i), options, correctAnswer));
            }

            for (Quiz currentQuiz : quizzes) {
                // Munculkan dialog pertanyaan kuis
                showQuizQuestion(currentQuiz);
            }

            resetScoreAndShowInfo();
        }
        else {
            System.out.println("Error: Jumlah pertanyaan, jawaban, dan jawaban benar tidak sesuai.");
        }
    }

    private void resetScoreAndShowInfo() {

        showAlert("Quiz Finished", "Congratulations! You have finished the quiz.\nYour final score is: " + score);
        writeUserScoresToFile();
        score = 0;
        currentQuestionIndex = 0;
    }

    private void writeUserScoresToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("user_scores.txt",true))) {
            for (Map.Entry<String, Integer> entry : userScores.entrySet()) {
                /*writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();*/
                String key = UserSession.getLoggedInUser();
                Integer value = entry.getValue();

                if (key != null && value != null) {
                    writer.write(key + "," + value);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readUserScoresFromFile() {
        try (Stream<String> lines = Files.lines(Paths.get("user_scores.txt"))) {
            lines.forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showQuizQuestion(Quiz currentQuiz) {
        Stage quizStage = new Stage();
        quizStage.setTitle("Quiz");

        VBox quizLayout = new VBox(10);
        quizLayout.setPadding(new Insets(10));

        Label questionLabel = new Label(currentQuiz.getQuestion());
        quizLayout.getChildren().add(questionLabel);

        ToggleGroup toggleGroup = new ToggleGroup();

        // Mendapatkan opsi quiz
        String[] options = currentQuiz.getOptions();

        // Membuat array untuk menyimpan urutan indeks
        Integer[] indexArray = new Integer[options.length];
        for (int i = 0; i < options.length; i++) {
            indexArray[i] = i;
        }

        // Mengacak urutan indeks
        Collections.shuffle(Arrays.asList(indexArray));

        // Menambahkan RadioButton dengan urutan yang diacak
        for (int i = 0; i < options.length; i++) {
            int index = indexArray[i];
            RadioButton radioButton = new RadioButton(options[index]);
            radioButton.setToggleGroup(toggleGroup);
            quizLayout.getChildren().add(radioButton);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            if (selectedRadioButton != null) {
                checkAnswer(selectedRadioButton.getText(), currentQuiz);
                quizStage.close();
            } else {
                showAlert("Error", "Please select an answer.");
            }
        });

        quizLayout.getChildren().add(submitButton);

        Scene quizScene = new Scene(quizLayout, 400, 200);
        quizStage.setScene(quizScene);
        quizStage.showAndWait();
    }



    private void checkAnswer(String selectedOption, Quiz currentQuiz) {
        if (currentQuiz.checkAnswer(selectedOption)) {
            score++;
            userScores.put(currentUser, score);
            showAlert("Correct", "Your answer is correct!\nYour current score is: " + score);
            currentQuestionIndex++;

            // Lanjut ke pertanyaan berikutnya (bisa diimplementasikan sesuai kebutuhan)
        } else {
            showAlert("Incorrect", "Sorry, that's not correct. The correct answer is: " + currentQuiz.getCorrectAnswer());
        }
    }

    private GridPane createSignupForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        TextField newUsernameField = new TextField();
        PasswordField newPasswordField = new PasswordField();
        Button signupButton = new Button("Create Account");

        signupButton.setOnAction(e -> {
            String newUsername = newUsernameField.getText();
            String newPassword = newPasswordField.getText();

            try {
                validateSignup(newUsername, newPassword);
                if (userCredentials.containsKey(newUsername)) {
                    showAlert("Signup Failed", "Username already exists. Choose a different username.");
                } else {
                    userCredentials.put(newUsername, newPassword);
                    showAlert("Signup Successful", "Account created successfully. You can now login.");
                    newUsernameField.clear();
                    newPasswordField.clear();

                }
            } catch (IllegalArgumentException ex) {
                showAlert("Signup Failed", ex.getMessage());
            }
        });

        grid.add(new Label("New Username:"), 0, 0);
        grid.add(newUsernameField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(signupButton, 1, 2);

        return grid;
    }

    private void validateSignup(String username, String password) throws IllegalArgumentException {
        if (username.length() < 6) {
            throw new IllegalArgumentException("Username must be at least 6 characters long.");
        }

        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException("The password must consist of uppercase letters, lowercase letters and 1 number");
        }
    }

    private boolean isPasswordValid(String password) {
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        // Minimal satu huruf besar, satu huruf kecil, dan satu angka
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    private boolean validateLogin(String username, String password) {
        return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
    }

    private void readUserCredentialsFromFile() {
        try (Stream<String> lines = Files.lines(Paths.get(FILE_PATH))) {
            lines.forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeUserCredentialsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

        writeUserCredentialsToFile();
    }

    private static class Quiz {
        /**
         * Kelas mewakili pertanyaan kuis.
         */
        private String question;
        /** Pertanyaan. */
        private String[] options;
        /** Opsi jawaban. */
        private String correctAnswer;
        /** Jawaban yang benar. */

        public Quiz(String question, String[] options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getOptions() {
            return options;
        }

        // getter dan setter

        /**
         * Mengecek apakah jawaban yang dipilih benar.
         * @param selectedOption opsi jawaban yang dipilih
         * @return true jika benar, false jika salah
         */
        public boolean checkAnswer(String selectedOption) {
            return correctAnswer.equals(selectedOption);
        }
        private String getCorrectAnswer() {
            return correctAnswer;
        }
    }

    /**
     * Kelas untuk menyimpan informasi sesi pengguna.
     */
    public class UserSession {
        private static String username;
        /** Username pengguna yang sedang login. */
        public static void setLoggedInUser(String loggedInUsername) {
            username = loggedInUsername;
        }
        // Metode untuk set username saat login
        public static String getLoggedInUser() {
            return username;
        }
        // Metode untuk mendapatkan username

        // getter dan setter

    }
}