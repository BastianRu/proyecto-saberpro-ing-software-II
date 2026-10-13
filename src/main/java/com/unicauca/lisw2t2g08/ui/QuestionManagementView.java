package com.unicauca.lisw2t2g08.ui;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.domain.User;

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
 * consultar preguntas con filtros, crear y modificar borradores, ver su detalle,
 * cambiar su estado dentro de las transiciones validas (HU04) y consultar el
 * historial de cambios de estado. No calcula nada, todo lo delega en el
 * controlador; las vistas observadoras se enteran del cambio a traves del modelo.
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
    private final TextArea historyArea = readOnlyArea(4);

    // Filtros de consulta (RF-07)
    private final ComboBox<QuestionState> filterState = new ComboBox<>();
    private final ComboBox<DifficultyLevel> filterDifficulty = new ComboBox<>();
    private final TextField filterCompetence = new TextField();
    private final TextField filterTopic = new TextField();
    private final TextField filterSubtopic = new TextField();
    private final TextField filterText = new TextField();
    private Button editButton;

    // Usuario de la sesion, para registrar los cambios de estado (HU04-01)
    private String username = "sistema";

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
     * Establece el usuario de la sesion activa para que los cambios de estado
     * queden registrados a su nombre en el historial de auditoria (HU04-01).
     *
     * @param user usuario que inicio sesion; puede ser null.
     */
    public void setCurrentUser(User user) {
        this.username = (user == null || user.getLogin() == null || user.getLogin().isBlank())
                ? "sistema" : user.getLogin();
    }

    /**
     * Reconstruye el listado con el estado actual del banco de preguntas.
     */
    public void reload() {
        search(idField.getText());
    }

    private void search(String keepSelectedId) {
        List<Question> questions = controller.searchQuestions(currentQuery());
        questionCombo.getItems().setAll(questions);
        if (questions.isEmpty()) {
            clearDetail();
            return;
        }
        Question selected = questions.stream()
                .filter(question -> question.getId().equals(keepSelectedId))
                .findFirst()
                .orElse(questions.get(0));
        questionCombo.setValue(selected);
        showQuestion(selected);
    }

    private QuestionCriteria currentQuery() {
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
                justificationArea, bibliographyArea, historyArea)) {
            area.clear();
        }
        newStateCombo.getItems().clear();
        newStateCombo.setValue(null);
        editButton.setDisable(true);
    }

    private Scene buildScene() {
        Label title = new Label("Gestión de preguntas");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox root = new VBox(12, title, buildFilters(), buildDetailForm());
        root.setPadding(new Insets(15));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        return new Scene(scroll, 720, 640);
    }

    private TitledPane buildFilters() {
        filterState.getItems().setAll(QuestionState.values());
        filterState.setPromptText("Estado");
        filterDifficulty.getItems().setAll(DifficultyLevel.values());
        filterDifficulty.setPromptText("Dificultad");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Estado:"), 0, 0);
        grid.add(filterState, 1, 0);
        grid.add(new Label("Dificultad:"), 2, 0);
        grid.add(filterDifficulty, 3, 0);
        grid.add(new Label("Competencia:"), 0, 1);
        grid.add(filterCompetence, 1, 1);
        grid.add(new Label("Tema:"), 2, 1);
        grid.add(filterTopic, 3, 1);
        grid.add(new Label("Subtema:"), 0, 2);
        grid.add(filterSubtopic, 1, 2);
        grid.add(new Label("Texto:"), 2, 2);
        grid.add(filterText, 3, 2);

        Button searchButton = new Button("Buscar");
        searchButton.setOnAction(e -> search(idField.getText()));
        Button clearButton = new Button("Limpiar filtros");
        clearButton.setOnAction(e -> clearFilters());
        grid.add(searchButton, 0, 3);
        grid.add(clearButton, 1, 3);

        TitledPane pane = new TitledPane("Filtros de búsqueda (RF-07)", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildDetailForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));

        int row = 0;
        grid.add(new Label("Pregunta:"), 0, row);
        grid.add(questionCombo, 1, row);
        HBox.setHgrow(questionCombo, Priority.ALWAYS);
        Button newButton = new Button("Nueva");
        newButton.setOnAction(e -> createQuestion());
        Button loadButton = new Button("Cargar");
        loadButton.setOnAction(e -> loadSelectedQuestion());
        grid.add(new HBox(6, newButton, loadButton), 2, row);
        row++;

        row = addReadOnly(grid, row, "Id:", idField);
        row = addReadOnly(grid, row, "Nombre:", nameField);
        row = addReadOnly(grid, row, "Clasificación:", classificationField);
        row = addReadOnly(grid, row, "Contexto:", contextArea);
        row = addReadOnly(grid, row, "Pregunta:", statementArea);
        row = addReadOnly(grid, row, "Opciones:", optionsArea);
        row = addReadOnly(grid, row, "Respuesta correcta:", correctAnswerField);
        row = addReadOnly(grid, row, "Justificación:", justificationArea);
        row = addReadOnly(grid, row, "Bibliografía:", bibliographyArea);
        row = addReadOnly(grid, row, "Estado actual:", currentStateField);
        grid.add(new Label("Nuevo estado:"), 0, row);
        grid.add(newStateCombo, 1, row);
        row++;
        row = addReadOnly(grid, row, "Historial:", historyArea);

        Button updateButton = new Button("Actualizar estado");
        updateButton.setOnAction(e -> updateState());
        editButton = new Button("Modificar pregunta");
        editButton.setDisable(true);
        editButton.setOnAction(e -> editQuestion());
        grid.add(new HBox(10, updateButton, editButton), 1, row);

        TitledPane pane = new TitledPane("Pregunta seleccionada", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private int addReadOnly(GridPane grid, int row, String label, javafx.scene.Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private void loadSelectedQuestion() {
        Question selected = questionCombo.getValue();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Primero seleccione una pregunta");
            return;
        }
        Optional<Question> found = controller.findById(selected.getId());
        if (found.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "La pregunta ya no existe en el banco");
            return;
        }
        showQuestion(found.get());
    }

    private void createQuestion() {
        Question created = new QuestionEditorView(stage, controller, null).showAndWait();
        if (created == null) {
            return;
        }
        clearFiltersSilently();
        search(created.getId());
    }

    private void clearFiltersSilently() {
        filterState.setValue(null);
        filterDifficulty.setValue(null);
        filterCompetence.clear();
        filterTopic.clear();
        filterSubtopic.clear();
        filterText.clear();
    }

    private void editQuestion() {
        Question selected = questionCombo.getValue();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Primero seleccione una pregunta");
            return;
        }
        if (selected.getState() != QuestionState.BORRADOR) {
            showMessage(Alert.AlertType.WARNING, "Solo se pueden modificar preguntas en estado Borrador");
            return;
        }
        Question edited = new QuestionEditorView(stage, controller, selected).showAndWait();
        if (edited != null) {
            search(edited.getId());
        }
    }

    private void updateState() {
        Question selected = questionCombo.getValue();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Primero seleccione una pregunta");
            return;
        }
        QuestionState target = newStateCombo.getValue();
        if (target == null) {
            showMessage(Alert.AlertType.WARNING, "Seleccione el nuevo estado");
            return;
        }
        try {
            controller.changeState(selected.getId(), target, username);
            search(selected.getId());
        } catch (IllegalStateException | IllegalArgumentException e) {
            showMessage(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void showQuestion(Question question) {
        idField.setText(question.getId());
        nameField.setText(question.getName());
        classificationField.setText(classificationOf(question));
        contextArea.setText(question.getContext());
        statementArea.setText(question.getStatement());
        optionsArea.setText(optionsOf(question));
        correctAnswerField.setText(question.getDistractors().getCorrectLetter()
                + ". " + question.getCorrectAnswer());
        justificationArea.setText(question.getJustification());
        bibliographyArea.setText(question.getBibliography());
        currentStateField.setText(question.getState().getLabel());
        // HU04-02: solo se ofrecen el estado actual y las transiciones validas
        newStateCombo.getItems().setAll(question.getState().selectableTransitions());
        newStateCombo.setValue(question.getState());
        editButton.setDisable(question.getState() != QuestionState.BORRADOR);
        showHistory(question.getId());
    }

    private String optionsOf(Question question) {
        List<String> options = question.getDistractors().getOptions();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            text.append(question.getDistractors().getLabeledOption(i));
            if (i < options.size() - 1) {
                text.append(System.lineSeparator());
            }
        }
        return text.toString();
    }

    private String classificationOf(Question question) {
        String difficulty = (question.getDifficulty() == null)
                ? "-" : question.getDifficulty().getLabel();
        return question.getCompetence() + " / " + question.getTopic()
                + " / " + question.getSubtopic() + "  (Dificultad: " + difficulty + ")";
    }

    /**
     * Muestra el historial de cambios de estado de la pregunta seleccionada,
     * con el usuario y la fecha de cada cambio (HU04-01).
     *
     * @param questionId identificador de la pregunta.
     */
    private void showHistory(String questionId) {
        List<QuestionStateChange> history = controller.stateHistory(questionId);
        if (history.isEmpty()) {
            historyArea.setText("Sin cambios de estado registrados.");
            return;
        }
        StringBuilder text = new StringBuilder();
        for (QuestionStateChange change : history) {
            text.append(change.getChangedAt().toString().replace('T', ' '))
                    .append("  ").append(change.getDescription())
                    .append(System.lineSeparator());
        }
        historyArea.setText(text.toString().trim());
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
