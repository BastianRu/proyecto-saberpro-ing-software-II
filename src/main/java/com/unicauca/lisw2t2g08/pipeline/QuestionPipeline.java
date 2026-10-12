package com.unicauca.lisw2t2g08.pipeline;

import com.unicauca.lisw2t2g08.domain.QuestionDraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Patron Tuberias y Filtros aplicado a HE 3 - Validacion estructural.
 *
 * Encadena los filtros en orden: ContextFilter (RF-08) -> DirectQuestionFilter
 * (RF-09) -> DistractorCountFilter (RF-10) -> ForbiddenExpressionFilter
 * (RF-12) -> DistractorFormatFilter (RF-13) -> CorrectAnswerFilter (RF-11) -> RequiredFieldsFilter (RF-05).
 * Si un filtro falla, la tuberia se detiene y propaga su ValidationException.
 */
public class QuestionPipeline {

    private final List<Filter> filters = new ArrayList<>();

    public QuestionPipeline() {
        filters.add(new ContextFilter());
        filters.add(new DirectQuestionFilter());
        filters.add(new DistractorCountFilter());
        filters.add(new ForbiddenExpressionFilter());
        filters.add(new DistractorFormatFilter());
        filters.add(new CorrectAnswerFilter());
        filters.add(new RequiredFieldsFilter());
    }

    /**
     * Permite construir tuberias a la medida (por ejemplo, con un subconjunto
     * de filtros).
     */
    public QuestionPipeline(List<Filter> customFilters) {
        filters.addAll(customFilters);
    }

    /**
     * @param draft borrador de pregunta nueva.
     * @return el borrador validado.
     * @throws ValidationException con los errores del primer filtro que falle.
     */
    public QuestionDraft run(QuestionDraft draft) {
        QuestionDraft current = draft;
        for (Filter filter : filters) {
            current = filter.process(current);
        }
        return current;
    }
}
