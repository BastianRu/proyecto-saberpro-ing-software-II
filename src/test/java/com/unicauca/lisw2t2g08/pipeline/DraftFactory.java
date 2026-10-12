package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.List;

/** Fabrica de borradores validos para las pruebas del pipeline. */
final class DraftFactory {

    private DraftFactory() {
    }

    static QuestionDraft valid() {
        QuestionDraft draft = new QuestionDraft();
        draft.setContext("El patron Pipeline encadena filtros que procesan datos en secuencia.");
        draft.setDirectQuestion("Cual es la ventaja principal de este patron?");
        draft.setOptions(List.of(
                "Permite componer validaciones independientes",
                "Obliga a reescribir todo el sistema",
                "Elimina la necesidad de pruebas",
                "Impide reutilizar componentes",
                "Requiere una base de datos distribuida"));
        draft.setCorrectIndex(0);
        draft.setName("Ventaja del patron Pipeline");
        draft.setJustification("El patron permite componer validaciones independientes.");
        draft.setBibliography("Gamma et al. Design Patterns.");
        draft.setCompetence("Diseno de software");
        draft.setTopic("Patrones de diseno");
        draft.setSubtopic("Pipeline");
        draft.setDifficulty(DifficultyLevel.MEDIA);
        return draft;
    }

    static QuestionDraft withOptions(List<String> options, int correctIndex) {
        QuestionDraft draft = valid();
        draft.setOptions(options);
        draft.setCorrectIndex(correctIndex);
        return draft;
    }
}
