package com.unicauca.lisw2t2g08.ui;

import java.util.ArrayList;
import java.util.List;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.pipeline.ValidationException;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Formulario modal para crear (RF-04) o modificar (RF-06) una pregunta con los
 * campos de RF-05. No valida nada: arma un QuestionDraft y se lo pasa al
 * controlador; los errores de la validacion estructural se muestran tal cual.
 */
public class QuestionEditorView {

    private static final int OPTION_COUNT = 5;

    private final Stage stage;
    private final QuestionController controller;
    private final Question editing;

    private final TextField nameField = new TextField();
    private final TextField competenceField = new TextField();
    private final TextField topicField = new TextField();
    private final TextField subtopicField = new TextField();
    private final ComboBox<DifficultyLevel> difficultyCombo = new ComboBox<>();
    private final TextArea contextArea = area(4);
    private final TextArea directQuestionArea = area(2);
    private final List<TextField> optionFields = new ArrayList<>();
    private final ToggleGroup correctGroup = new ToggleGroup();
    private final TextArea justificationArea = area(3);
    private final TextArea bibliographyArea = area(2);

    private Question saved;

    /**
     * @param owner      ventana propietaria del dialogo.
     * @param controller controlador del MVC.
     * @param editing    pregunta a modificar, o null para crear una nueva.
     */
    public QuestionEditorView(Stage owner, QuestionController controller, Question editing) {
        this.controller = controller;
        this.editing = editing;
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(editing == null ? "Nueva pregunta" : "Modificar pregunta " + editing.getId());
        difficultyCombo.getItems().setAll(DifficultyLevel.values());
        stage.setScene(buildScene());
        if (editing != null) {
            fillFrom(editing);
        }
    }

    /**
     * Muestra el formulario y espera a que se cierre.
     *
     * @return la pregunta guardada, o null si se cancelo.
     */
    public Question showAndWait() {
        stage.showAndWait();
        return saved;
    }

    private Scene buildScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        ColumnConstraints labels = new ColumnConstraints();
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, fields);

        int row = 0;
        grid.addRow(row++, new Label("Nombre:"), nameField);
        grid.addRow(row++, new Label("Competencia:"), competenceField);
        grid.addRow(row++, new Label("Tema:"), topicField);
        grid.addRow(row++, new Label("Subtema:"), subtopicField);
        grid.addRow(row++, new Label("Dificultad:"), difficultyCombo);
        grid.addRow(row++, new Label("Contexto:"), contextArea);
        grid.addRow(row++, new Label("Pregunta directa:"), directQuestionArea);

        grid.add(new Label("Opciones (marque la respuesta correcta; las otras cuatro son distractores):"),
                0, row++, 2, 1);
        for (int i = 0; i < OPTION_COUNT; i++) {
            RadioButton correct = new RadioButton(String.valueOf((char) ('A' + i)) + ".");
            correct.setToggleGroup(correctGroup);
            correct.setUserData(i);
            TextField option = new TextField();
            HBox.setHgrow(option, Priority.ALWAYS);
            optionFields.add(option);
            grid.addRow(row++, correct, option);
        }

        grid.addRow(row++, new Label("Justificación:"), justificationArea);
        grid.addRow(row++, new Label("Bibliografía:"), bibliographyArea);
        nameField.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Guardar");
        save.setDefaultButton(true);
        save.setOnAction(e -> save());
        Button cancel = new Button("Cancelar");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> stage.close());
        HBox buttons = new HBox(10, save, cancel);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox layout = new VBox(10, scroll, buttons);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return new Scene(layout, 620, 700);
    }

    private void fillFrom(Question question) {
        nameField.setText(question.getName());
        competenceField.setText(question.getCompetence());
        topicField.setText(question.getTopic());
        subtopicField.setText(question.getSubtopic());
        difficultyCombo.setValue(question.getDifficulty());
        contextArea.setText(question.getContext());
        directQuestionArea.setText(question.getStatement());
        List<String> options = question.getDistractors().getOptions();
        for (int i = 0; i < OPTION_COUNT && i < options.size(); i++) {
            optionFields.get(i).setText(options.get(i));
        }
        int correct = question.getDistractors().getCorrectIndex();
        if (correct < OPTION_COUNT) {
            correctGroup.getToggles().get(correct).setSelected(true);
        }
        justificationArea.setText(question.getJustification());
        bibliographyArea.setText(question.getBibliography());
    }

    private QuestionDraft buildDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName(nameField.getText());
        draft.setCompetence(competenceField.getText());
        draft.setTopic(topicField.getText());
        draft.setSubtopic(subtopicField.getText());
        draft.setDifficulty(difficultyCombo.getValue());
        draft.setContext(contextArea.getText());
        draft.setDirectQuestion(directQuestionArea.getText());
        // Las opciones vacias se envian tal cual para que la validacion las reporte
        List<String> options = new ArrayList<>();
        for (TextField field : optionFields) {
            options.add(field.getText());
        }
        draft.setOptions(options);
        draft.setCorrectIndex(correctGroup.getSelectedToggle() == null
                ? -1 : (Integer) correctGroup.getSelectedToggle().getUserData());
        draft.setJustification(justificationArea.getText());
        draft.setBibliography(bibliographyArea.getText());
        return draft;
    }

    private void save() {
        QuestionDraft draft = buildDraft();
        try {
            saved = (editing == null)
                    ? controller.createQuestion(draft)
                    : controller.updateQuestion(editing.getId(), draft);
            stage.close();
        } catch (ValidationException ex) {
            showError(String.join(System.lineSeparator(), ex.getErrors()));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(stage);
        alert.setHeaderText("No se pudo guardar la pregunta");
        alert.showAndWait();
    }

    private static TextArea area(int rows) {
        TextArea area = new TextArea();
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        return area;
    }
}
