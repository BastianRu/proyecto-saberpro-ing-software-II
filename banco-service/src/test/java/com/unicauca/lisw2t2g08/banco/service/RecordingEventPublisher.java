package com.unicauca.lisw2t2g08.banco.service;

import java.util.ArrayList;
import java.util.List;

import com.unicauca.lisw2t2g08.banco.events.IEventPublisher;
import com.unicauca.lisw2t2g08.banco.events.QuestionCreatedEvent;
import com.unicauca.lisw2t2g08.banco.events.QuestionStateChangedEvent;

/** Publicador que guarda los eventos en memoria; permite simular un broker caído. Solo pruebas. */
public class RecordingEventPublisher implements IEventPublisher {

    public final List<QuestionCreatedEvent> created = new ArrayList<>();
    public final List<QuestionStateChangedEvent> stateChanged = new ArrayList<>();
    public boolean failing;

    @Override
    public void publish(QuestionCreatedEvent event) {
        if (failing) {
            throw new IllegalStateException("broker caído");
        }
        created.add(event);
    }

    @Override
    public void publish(QuestionStateChangedEvent event) {
        if (failing) {
            throw new IllegalStateException("broker caído");
        }
        stateChanged.add(event);
    }

    public void clear() {
        created.clear();
        stateChanged.clear();
    }
}
