package com.unicauca.lisw2t2g08.ui;

import java.util.EnumMap;
import java.util.Map;

import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.infra.Observer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Primera vista observadora: muestra cuantas preguntas hay en cada estado. Se
 * refresca sola cuando el modelo notifica un cambio.
 */
public class QuestionStatisticsView implements Observer {

    private final Stage stage;
    private final Map<QuestionState, Label> valueLabels = new EnumMap<>(QuestionState.class);
    private final Label totalLabel = boldLabel("0");

    public QuestionStatisticsView(Stage stage) {
        this.stage = stage;
        stage.setTitle("Vista de estadísticas");
        stage.setScene(buildScene());
    }

    public Stage getStage() {
        return stage;
    }

    private Scene buildScene() {
        Label title = new Label("Preguntas por estado");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);
        int row = 0;
        for (QuestionState state : QuestionState.values()) {
            Label value = boldLabel("0");
            valueLabels.put(state, value);
            grid.addRow(row++, new Label(state.getLabel() + ":"), value);
        }
        grid.addRow(row, new Label("Total:"), totalLabel);

        VBox layout = new VBox(12, title, grid);
        layout.setPadding(new Insets(20));
        return new Scene(layout, 320, 220);
    }

    /**
     * Recibe las estadisticas publicadas por el sujeto y refresca las etiquetas.
     *
     * @param data instancia de QuestionStatistics enviada por el modelo.
     */
    @Override
    public void update(Object data) {
        if (!(data instanceof QuestionStatistics statistics)) {
            return;
        }
        Platform.runLater(() -> {
            for (QuestionState state : QuestionState.values()) {
                valueLabels.get(state).setText(String.valueOf(statistics.getCount(state)));
            }
            totalLabel.setText(String.valueOf(statistics.getTotal()));
        });
    }

    private static Label boldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }
}
