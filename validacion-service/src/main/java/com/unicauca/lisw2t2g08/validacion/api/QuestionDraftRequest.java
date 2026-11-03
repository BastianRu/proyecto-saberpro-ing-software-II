package com.unicauca.lisw2t2g08.validacion.api;

import com.unicauca.lisw2t2g08.validacion.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.validacion.domain.QuestionDraft;

import java.util.List;
import java.util.Objects;

/**
 * Cuerpo de la peticion {@code POST /validaciones}.
 *
 * Reproduce los once campos que el monolito espera en un borrador de pregunta.
 * Un valor desconocido en {@code difficulty} hace que Jackson rechace la
 * peticion (HTTP 400); un {@code difficulty} ausente llega como {@code null} y
 * lo reporta el propio pipeline.
 */
public record QuestionDraftRequest(
        String name,
        String context,
        String directQuestion,
        List<String> options,
        Integer correctIndex,
        String justification,
        String bibliography,
        String competence,
        String topic,
        String subtopic,
        DifficultyLevel difficulty) {

    /**
     * Convierte la peticion en el borrador mutable que consume el pipeline.
     *
     * Los textos nulos se normalizan a cadena vacia para que los filtros, que
     * asumen datos ya presentes en algunos casos, reporten el error de negocio
     * correspondiente en lugar de lanzar una {@link NullPointerException}.
     * Una lista de opciones nula se convierte en lista vacia y un
     * {@code correctIndex} ausente se interpreta como {@code -1} (respuesta
     * correcta no indicada), igual que en el dominio del monolito.
     *
     * @return borrador listo para atravesar la tuberia de validacion.
     */
    public QuestionDraft toDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName(text(name));
        draft.setContext(text(context));
        draft.setDirectQuestion(text(directQuestion));
        draft.setOptions(normalizeOptions(options));
        draft.setCorrectIndex(correctIndex == null ? -1 : correctIndex);
        draft.setJustification(text(justification));
        draft.setBibliography(text(bibliography));
        draft.setCompetence(text(competence));
        draft.setTopic(text(topic));
        draft.setSubtopic(text(subtopic));
        draft.setDifficulty(difficulty);
        return draft;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static List<String> normalizeOptions(List<String> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .map(option -> Objects.requireNonNullElse(option, ""))
                .toList();
    }
}
