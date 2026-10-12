package com.unicauca.lisw2t2g08.ui;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
 * consultar preguntas con filtros, crear y modificar borradores, ver su detalle
 * y cambiar su estado. No calcula
 * nada, todo lo delega en el controlador; las vistas observadoras se enteran
 * del cambio a traves del modelo.
 */
public class QuestionManagementView {

    private final Stage stage;
    private final QuestionController controller;

    private final ComboBox<Question> questionCombo = new ComboBox<>();
    private final TextField idField = readOnlyField();
    private final TextField nameField = readOnlyField();
    private final TextField classificationField = readOnlyField();
    private final TextArea contextArea = readOnlyArea(3);
    private final TextArea statementArea = readOnlyArea(3);
    private final TextArea optionsArea = readOnlyArea(4);
    private final TextField correctAnswerField = readOnlyField();
    private final TextArea justificationArea = readOnlyArea(2);
    private final TextArea bibliographyArea = readOnlyArea(2);
    private final TextField currentStateField = readOnlyField();
    private final ComboBox<QuestionState> newStateCombo = new ComboBox<>();

    // Filtros de consulta (RF-07)
    private final ComboBox<QuestionState> filterState = new ComboBox<>();
    private final ComboBox<DifficultyLevel> filterDifficulty = new ComboBox<>();
    private final TextField filterCompetence = new TextField();
    private final TextField filterTopic = new TextField();
    private final TextField filterSubtopic = new TextField();
    private final TextField filterText = new TextField();
    private Button editButton;

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
        search(null);
    }

    /**
     * RF-07: consulta las preguntas con los filtros actuales y, si se indica,
     * deja seleccionada la pregunta con ese id.
     */
    private void search(String selectId) {
        List<Question> questions = controller.searchQuestions(currentCriteria());
        questionCombo.getItems().setAll(questions);
        Question toSelect = questions.stream()
                .filter(q -> q.getId().equals(selectId)).findFirst()
                .orElse(questions.isEmpty() ? null : questions.get(0));
        if (toSelect == null) {
            clearDetail();
            return;
        }
        questionCombo.getSelectionModel().select(toSelect);
        loadSelectedQuestion();
    }

    private QuestionCriteria currentCriteria() {
        return new QuestionCriteria()
                .withState(filterState.getValue())
                .withDifficulty(filterDifficulty.getValue())
                .withCompetence(filterCompetence.getText())
                .withTopic(filterTopic.getText())
                .withSubtopic(filterSubtopic.getText())
                .withText(filterText.getText());
    }

    private void clearFilters() {
        filterState.setValue(null);
        filterDifficulty.setValue(null);
        filterCompetence.clear();
        filterTopic.clear();
        filterSubtopic.clear();
        filterText.clear();
        search(idField.getText());
    }

    private void clearDetail() {
        for (TextField field : List.of(idField, nameField, classificationField,
                correctAnswerField, currentStateField)) {
            field.clear();
        }
        for (TextArea area : List.of(contextArea, statementArea, optionsArea,
                justificationArea, bibliographyArea)) {
            area.clear();
        }
        newStateCombo.setValue(null);
        editButton.setDisable(true);
    }

    private TitledPane buildFilterPane() {
        filterState.getItems().setAll(QuestionState.values());
        filterDifficulty.getItems().setAll(DifficultyLevel.values());
        filterState.setPromptText("Estado");
        filterDifficulty.setPromptText("Dificultad");
        filterCompetence.setPromptText("Competencia");
        filterTopic.setPromptText("Tema");
        filterSubtopic.setPromptText("Subtema");
        filterText.setPromptText("Texto en nombre, contexto o pregunta");

        Button searchButton = new Button("Buscar");
        searchButton.setOnAction(e -> search(idField.getText()));
        Button clearButton = new Button("Limpiar");
        clearButton.setOnAction(e -> clearFilters());

        HBox.setHgrow(filterText, Priority.ALWAYS);
        HBox first = new HBox(8, filterState, filterDifficulty, filterCompetence);
        HBox second = new HBox(8, filterTopic, filterSubtopic);
        HBox third = new HBox(8, filterText, searchButton, clearButton);
        VBox box = new VBox(6, first, second, third);
        box.setPadding(new Insets(5));

        TitledPane pane = new TitledPane("FILTROS", box);
        pane.setCollapsible(false);
        return pane;
    }

    private void createQuestion() {
        Question created = new QuestionEditorView(stage, controller, null).showAndWait();
        if (created != null) {
            clearFiltersSilently();
            search(created.getId());
        }
    }

    private void editQuestion() {
        String id = idField.getText();
        if (id == null || id.isBlank()) {
            showMessage(Alert.AlertType.WARNING, "Primero cargue una pregunta");
            return;
        }
        Optional<Question> current = controller.findById(id);
        if (current.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "La pregunta ya no existe en el banco");
            return;
        }
        if (current.get().getState() != QuestionState.BORRADOR) {
            showMessage(Alert.AlertType.WARNING, "Solo se pueden modificar preguntas en estado Borrador");
            showQuestion(current.get());
            return;
        }
        Question edited = new QuestionEditorView(stage, controller, current.get()).showAndWait();
        if (edited != null) {
            search(edited.getId());
        }
    }

    // Una pregunta nueva debe verse aunque algun filtro la excluya
    private void clearFiltersSilently() {
        filterState.setValue(null);
        filterDifficulty.setValue(null);
        filterCompetence.clear();
        filterTopic.clear();
        filterSubtopic.clear();
        filterText.clear();
    }

    private Scene buildScene() {
        Label title = new Label("GESTIÓN DE PREGUNTAS");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox layout = new VBox(12, title, buildFilterPane(), buildSelectorPane(), buildFormPane());
        layout.setPadding(new Insets(15));

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        return new Scene(scroll, 600, 760);
    }

    private TitledPane buildSelectorPane() {
        Button loadButton = new Button("Cargar pregunta");
        loadButton.setOnAction(e -> loadSelectedQuestion());
        Button newButton = new Button("Nueva pregunta");
        newButton.setOnAction(e -> createQuestion());

        questionCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(questionCombo, Priority.ALWAYS);

        HBox row = new HBox(10, new Label("Pregunta:"), questionCombo, loadButton, newButton);
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
        grid.addRow(row++, new Label("Clasificación:"), classificationField);
        grid.addRow(row++, new Label("Contexto:"), contextArea);
        grid.addRow(row++, new Label("Pregunta:"), statementArea);
        grid.addRow(row++, new Label("Opciones:"), optionsArea);
        grid.addRow(row++, new Label("Respuesta correcta:"), correctAnswerField);
        grid.addRow(row++, new Label("Justificación:"), justificationArea);
        grid.addRow(row++, new Label("Bibliografía:"), bibliographyArea);
        grid.addRow(row++, new Label("Estado actual:"), currentStateField);
        grid.addRow(row++, new Label("Nuevo estado:"), newStateCombo);

        Button updateButton = new Button("Actualizar estado");
        updateButton.setOnAction(e -> updateState());
        editButton = new Button("Modificar pregunta");
        editButton.setDisable(true);
        editButton.setOnAction(e -> editQuestion());
        grid.add(new HBox(10, updateButton, editButton), 1, row);

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
        contextArea.setText(question.getContext());
        classificationField.setText(classificationOf(question));
        justificationArea.setText(question.getJustification());
        bibliographyArea.setText(question.getBibliography());
        // RF-06: solo las preguntas en Borrador se pueden modificar
        editButton.setDisable(question.getState() != QuestionState.BORRADOR);

        StringBuilder options = new StringBuilder();
        for (int i = 0; i < distractors.getOptions().size(); i++) {
            options.append(distractors.getLabeledOption(i)).append(System.lineSeparator());
        }
        optionsArea.setText(options.toString().trim());

        correctAnswerField.setText(distractors.getCorrectLetter() + ". " + distractors.getCorrectAnswer());
        currentStateField.setText(question.getState().getLabel());
        newStateCombo.setValue(question.getState());
    }

    private static String classificationOf(Question question) {
        String difficulty = question.getDifficulty() == null ? "-" : question.getDifficulty().getLabel();
        return String.join(" / ", dash(question.getCompetence()), dash(question.getTopic()),
                dash(question.getSubtopic())) + "  (Dificultad: " + difficulty + ")";
    }

    private static String dash(String value) {
        return value == null || value.isBlank() ? "-" : value;
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
