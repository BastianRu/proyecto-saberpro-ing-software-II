package com.unicauca.lisw2t2g08.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.unicauca.lisw2t2g08.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;

/**
 * Pruebas con Mockito: en vez de escribir el doble a mano, Mockito genera
 * dinamicamente el doble de la abstraccion IQuestionRepository.
 *
 * Es posible porque el servicio depende de interfaces (DIP), no de clases
 * concretas, por lo que son falsificables sin tocar la logica de negocio. Se
 * verifican el resultado, las interacciones y el contenido de la auditoria.
 */
class QuestionServiceMockitoTest {

    @Test
    @DisplayName("Con Mockito el servicio actualiza el estado, audita y retorna true")
    void shouldUpdateAuditAndReturnTrueWithMockito() {
        IQuestionRepository repository = mock(IQuestionRepository.class);
        Question question = borrador();
        when(repository.findById("P-500")).thenReturn(Optional.of(question));
        when(repository.update(any(Question.class))).thenReturn(true);

        QuestionService service = new QuestionService(repository);
        boolean changed = service.changeState(
                "P-500", QuestionState.PENDIENTE_REVISION, "juan");

        assertTrue(changed);
        assertEquals(QuestionState.PENDIENTE_REVISION, question.getState());
        verify(repository).update(question);
        verify(repository).saveStateChange(any(QuestionStateChange.class));
    }

    @Test
    @DisplayName("Con Mockito: una pregunta inexistente no actualiza ni audita")
    void shouldNotUpdateWhenQuestionDoesNotExist() {
        IQuestionRepository repository = mock(IQuestionRepository.class);
        when(repository.findById("P-999")).thenReturn(Optional.empty());

        QuestionService service = new QuestionService(repository);
        boolean changed = service.changeState("P-999", QuestionState.ARCHIVADA, "juan");

        assertFalse(changed);
        verify(repository, never()).update(any(Question.class));
        verify(repository, never()).saveStateChange(any(QuestionStateChange.class));
    }

    @Test
    @DisplayName("Con Mockito: un ArgumentCaptor verifica el contenido de la auditoría")
    void shouldCaptureAuditedChange() {
        IQuestionRepository repository = mock(IQuestionRepository.class);
        Question question = borrador();
        when(repository.findById("P-500")).thenReturn(Optional.of(question));
        when(repository.update(any(Question.class))).thenReturn(true);

        QuestionService service = new QuestionService(repository);
        service.changeState("P-500", QuestionState.PENDIENTE_REVISION, "juan");

        ArgumentCaptor<QuestionStateChange> captor =
                ArgumentCaptor.forClass(QuestionStateChange.class);
        verify(repository).saveStateChange(captor.capture());
        QuestionStateChange audited = captor.getValue();
        assertEquals("P-500", audited.getQuestionId());
        assertEquals(QuestionState.BORRADOR, audited.getFromState());
        assertEquals(QuestionState.PENDIENTE_REVISION, audited.getToState());
        assertEquals("juan", audited.getUsername());
    }

    @Test
    @DisplayName("Con Mockito: una transición inválida no toca el repositorio")
    void shouldNotPersistInvalidTransition() {
        IQuestionRepository repository = mock(IQuestionRepository.class);
        when(repository.findById("P-500")).thenReturn(Optional.of(borrador()));

        QuestionService service = new QuestionService(repository);

        assertThrows(IllegalStateException.class,
                () -> service.changeState("P-500", QuestionState.PUBLICADA, "juan"));
        verify(repository, never()).update(any(Question.class));
        verify(repository, never()).saveStateChange(any(QuestionStateChange.class));
    }

    private Question borrador() {
        return new Question("P-500", "Pregunta con Mockito",
                "¿Se puede probar con Mockito?",
                new QuestionDistractors(List.of("A", "B", "C"), 0),
                QuestionState.BORRADOR);
    }
}
