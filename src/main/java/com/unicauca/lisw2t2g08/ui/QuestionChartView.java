package com.unicauca.lisw2t2g08.ui;

import java.util.EnumMap;
import java.util.Map;

import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.infra.Observer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Segunda vista observadora: grafica en pastel con el porcentaje de preguntas
 * por estado. Se redibuja sola cuando el modelo notifica un cambio.
 */
public class QuestionChartView implements Observer {

    private final Stage stage;
    private final PieChart chart = new PieChart();
    private final Map<QuestionState, PieChart.Data> slices = new EnumMap<>(QuestionState.class);
    private final Label emptyLabel = new Label("Sin preguntas");

    public QuestionChartView(Stage stage) {
        this.stage = stage;
        stage.setTitle("Vista gráfica");
        stage.setScene(buildScene());
    }

    public Stage getStage() {
        return stage;
    }

    private Scene buildScene() {
        Label title = new Label("Distribución de preguntas por estado");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (QuestionState state : QuestionState.values()) {
            PieChart.Data slice = new PieChart.Data(state.getLabel(), 0);
            slices.put(state, slice);
            chart.getData().add(slice);
        }
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        VBox layout = new VBox(10, title, chart, emptyLabel);
        layout.setPadding(new Insets(15));
        Scene scene = new Scene(layout, 420, 440);
        // Los colores por estado se definen en la hoja de estilos
        scene.getStylesheets().add(
                QuestionChartView.class.getResource("question-chart.css").toExternalForm());
        return scene;
    }

    /**
     * Recibe las estadisticas publicadas por el sujeto y redibuja el pastel.
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
                PieChart.Data slice = slices.get(state);
                slice.setPieValue(statistics.getCount(state));
                slice.setName(String.format("%s: %.1f%% (%d)",
                        state.getLabel(), statistics.getPercentage(state), statistics.getCount(state)));
            }
            boolean empty = statistics.getTotal() == 0;
            emptyLabel.setVisible(empty);
            emptyLabel.setManaged(empty);
        });
    }
}
