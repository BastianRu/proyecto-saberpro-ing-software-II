package com.unicauca.lisw2t2g08.ui;

import com.unicauca.lisw2t2g08.domain.User;
import com.unicauca.lisw2t2g08.infra.Subject;

import javafx.stage.Stage;

/**
 * Abre y cierra las tres ventanas del banco de preguntas: la vista principal
 * del MVC y las dos vistas observadoras.
 *
 * Las ventanas se crean la primera vez que se abren y las vistas observadoras
 * se suscriben al sujeto en ese momento (patron Observer). La vista principal
 * no conoce a las observadoras: solo habla con el controlador.
 */
public class QuestionBankWindows {

    private final QuestionController controller;
    private final Subject subject;

    private QuestionManagementView managementView;
    private QuestionStatisticsView statisticsView;
    private QuestionChartView chartView;

    /** Usuario con sesión iniciada, usado para auditar los cambios de estado. */
    private User currentUser;

    /**
     * @param controller controlador del MVC usado por la vista principal.
     * @param subject    sujeto observable que notifica los cambios de estado.
     */
    public QuestionBankWindows(QuestionController controller, Subject subject) {
        this.controller = controller;
        this.subject = subject;
    }

    /**
     * Establece el usuario de la sesión activa para que los cambios de estado
     * queden registrados a su nombre en el historial (HU04-01).
     *
     * @param currentUser usuario que inició sesión; puede ser null.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Muestra las tres ventanas, creandolas si es la primera vez.
     */
    public void show() {
        if (managementView == null) {
            managementView = new QuestionManagementView(new Stage(), controller);
            statisticsView = new QuestionStatisticsView(new Stage());
            chartView = new QuestionChartView(new Stage());

            subject.addObserver(statisticsView);
            subject.addObserver(chartView);
        }
        managementView.setCurrentUser(currentUser);

        // Primer render (o refresco) con el estado actual del banco de preguntas
        managementView.reload();
        subject.notifyAllObservers(controller.getStatistics());

        showAt(managementView.getStage(), 30, 30);
        showAt(statisticsView.getStage(), 600, 30);
        showAt(chartView.getStage(), 600, 290);
    }

    /**
     * Oculta las ventanas abiertas, por ejemplo al cerrar sesion.
     */
    public void close() {
        if (managementView == null) {
            return;
        }
        managementView.getStage().hide();
        statisticsView.getStage().hide();
        chartView.getStage().hide();
    }

    private void showAt(Stage stage, double x, double y) {
        if (!stage.isShowing()) {
            stage.setX(x);
            stage.setY(y);
            stage.show();
        }
        stage.toFront();
    }
}
