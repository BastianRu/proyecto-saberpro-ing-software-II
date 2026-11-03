package com.unicauca.lisw2t2g08.validacion.api;

import com.unicauca.lisw2t2g08.validacion.pipeline.QuestionPipeline;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el pipeline de validacion estructural como API REST.
 *
 * {@code POST /validaciones} ejecuta los filtros sobre el borrador recibido.
 * Si todos pasan responde 200; si uno falla, {@link ApiExceptionHandler}
 * traduce la excepcion del filtro al contrato del servicio.
 */
@RestController
public class ValidationController {

    private final QuestionPipeline pipeline;

    public ValidationController(QuestionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @PostMapping("/validaciones")
    public ValidationResponse validate(@RequestBody QuestionDraftRequest request) {
        pipeline.run(request.toDraft());
        return ValidationResponse.success();
    }
}
