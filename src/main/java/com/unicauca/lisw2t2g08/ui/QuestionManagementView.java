package com.unicauca.lisw2t2g08.ui;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Vista principal del micro patron MVC para el banco de preguntas: permite
 * seleccionar una pregunta, ver su detalle y cambiar su estado. No calcula
 * nada, todo lo delega en el controlador; las vistas observadoras se enteran
 * del cambio a traves del modelo.
 */
public class QuestionManagementView {

    private final Stage stage;
    private final QuestionController controller;

    private final ComboBox<Question> questionCombo = new ComboBox<>();
    private final TextField idField = readOnlyField();
    private final TextField nameField = readOnlyField();
    private final TextArea statementArea = readOnlyArea(3);
    private final TextArea optionsArea = readOnlyArea(4);
    private final TextField correctAnswerField = readOnlyField();
    private final TextField currentStateField = readOnlyField();
    private final ComboBox<QuestionState> newStateCombo = new ComboBox<>();

    public QuestionManagementView(Stage stage, QuestionController controller) {
        this.stage = stage;
        this.controller = controller;
        stage.setTitle("Banco de Preguntas Saber Pro - Gestión de preguntas");
        stage.setScene(buildScene());
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Vuelve a consultar las preguntas al controlador y carga la primera.
     */
    public void reload() {
        List<Question> questions = controller.listQuestions();
        questionCombo.getItems().setAll(questions);
        if (!questions.isEmpty()) {
            questionCombo.getSelectionModel().selectFirst();
            loadSelectedQuestion();
        }
    }

    private Scene buildScene() {
        Label title = new Label("GESTIÓN DE PREGUNTAS");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox layout = new VBox(12, title, buildSelectorPane(), buildFormPane());
        layout.setPadding(new Insets(15));

        return new Scene(layout, 560, 640);
    }

    private TitledPane buildSelectorPane() {
        Button loadButton = new Button("Cargar pregunta");
        loadButton.setOnAction(e -> loadSelectedQuestion());

        questionCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(questionCombo, Priority.ALWAYS);

        HBox row = new HBox(10, new Label("Pregunta:"), questionCombo, loadButton);
        row.setPadding(new Insets(5));

        TitledPane pane = new TitledPane("SELECCIONAR PREGUNTA", row);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildFormPane() {
        newStateCombo.getItems().setAll(QuestionState.values());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(5));
        ColumnConstraints labels = new ColumnConstraints();
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, fields);

        int row = 0;
        grid.addRow(row++, new Label("Id:"), idField);
        grid.addRow(row++, new Label("Nombre:"), nameField);
        grid.addRow(row++, new Label("Pregunta:"), statementArea);
        grid.addRow(row++, new Label("Opciones:"), optionsArea);
        grid.addRow(row++, new Label("Respuesta correcta:"), correctAnswerField);
        grid.addRow(row++, new Label("Estado actual:"), currentStateField);
        grid.addRow(row++, new Label("Nuevo estado:"), newStateCombo);

        Button updateButton = new Button("Actualizar estado");
        updateButton.setOnAction(e -> updateState());
        grid.add(updateButton, 1, row);

        TitledPane pane = new TitledPane("FORMULARIO DE PREGUNTA", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private void loadSelectedQuestion() {
        Question selected = questionCombo.getValue();
        if (selected == null) {
            return;
        }
        Optional<Question> found = controller.findById(selected.getId());
        if (found.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "La pregunta ya no existe en el banco");
            return;
        }
        showQuestion(found.get());
    }

    private void showQuestion(Question question) {
        QuestionDistractors distractors = question.getDistractors();
        idField.setText(question.getId());
        nameField.setText(question.getName());
        statementArea.setText(question.getStatement());

        StringBuilder options = new StringBuilder();
        for (int i = 0; i < distractors.getOptions().size(); i++) {
            options.append(distractors.getLabeledOption(i)).append(System.lineSeparator());
        }
        optionsArea.setText(options.toString().trim());

        correctAnswerField.setText(distractors.getCorrectLetter() + ". " + distractors.getCorrectAnswer());
        currentStateField.setText(question.getState().getLabel());
        newStateCombo.setValue(question.getState());
    }

    /**
     * Solicita al controlador el cambio de estado de la pregunta cargada. El
     * modelo se encarga de notificar a las vistas observadoras.
     */
    private void updateState() {
        String id = idField.getText();
        if (id == null || id.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Primero cargue una pregunta");
            return;
        }
        QuestionState newState = newStateCombo.getValue();
        try {
            if (controller.changeState(id, newState)) {
                controller.findById(id).ifPresent(this::showQuestion);
                showMessage(Alert.AlertType.INFORMATION, "Estado actualizado a: " + newState.getLabel());
            } else {
                showMessage(Alert.AlertType.WARNING, "No fue posible actualizar el estado");
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showMessage(Alert.AlertType.ERROR, ex.getMessage());
            controller.findById(id).ifPresent(this::showQuestion);
        }
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.initOwner(stage);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static TextField readOnlyField() {
        TextField field = new TextField();
        field.setEditable(false);
        return field;
    }

    private static TextArea readOnlyArea(int rows) {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        return area;
    }
}
