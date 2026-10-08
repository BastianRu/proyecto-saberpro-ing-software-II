package com.unicauca.lisw2t2g08.infra;

import java.util.ArrayList;
import java.util.List;

/**
 * Capa transversal (infra).
 * Sujeto observable del patron Observer. Administra la lista de observadores y
 * se encarga de notificarlos.
 */
public class Subject {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Registra un observador. Se ignoran los nulos y los duplicados.
     *
     * @param observer observador a registrar.
     */
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Elimina un observador previamente registrado.
     *
     * @param observer observador a eliminar.
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * @return numero de observadores registrados.
     */
    public int countObservers() {
        return observers.size();
    }

    /**
     * Notifica a todos los observadores registrados.
     *
     * @param data informacion que se publica a los observadores.
     */
    public void notifyAllObservers(Object data) {
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(data);
        }
    }
}
