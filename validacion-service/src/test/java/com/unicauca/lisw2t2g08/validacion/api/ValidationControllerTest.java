package com.unicauca.lisw2t2g08.validacion.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de contrato de {@code POST /validaciones}.
 *
 * El borrador base usa exactamente los valores de {@code DraftFactory.valid()}
 * del monolito, para que las pruebas de la API y las del pipeline validen el
 * mismo caso feliz.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void aceptaUnBorradorValido() throws Exception {
        postJson(validRequest().toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void rechazaUnBorradorSinContexto() throws Exception {
        ObjectNode request = validRequest();
        request.remove("context");

        postJson(request.toString())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]", containsString("RF-08")));
    }

    @Test
    void rechazaUnaOpcionConExpresionProhibida() throws Exception {
        ObjectNode request = validRequest();
        ((ArrayNode) request.get("options")).set(4, objectMapper.getNodeFactory().textNode("Todas las anteriores"));

        postJson(request.toString())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]", containsString("RF-12")));
    }

    @Test
    void rechazaUnBorradorSinRespuestaCorrectaIndicada() throws Exception {
        ObjectNode request = validRequest();
        request.remove("correctIndex");

        postJson(request.toString())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]", containsString("RF-11")));
    }

    @Test
    void rechazaUnJsonMalFormado() throws Exception {
        postJson("{ \"name\": \"sin cerrar\" ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Solicitud mal formada"));
    }

    @Test
    void rechazaUnCuerpoVacio() throws Exception {
        postJson("")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Solicitud mal formada"));
    }

    @Test
    void rechazaUnNivelDeDificultadDesconocido() throws Exception {
        ObjectNode request = validRequest();
        request.put("difficulty", "EXPERTO");

        postJson(request.toString())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Solicitud mal formada"));
    }

    @Test
    void responde422YNunca500AnteUnCuerpoVacioDeDatos() throws Exception {
        postJson("{}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]", containsString("RF-08")));
    }

    private ResultActions postJson(String body) throws Exception {
        return mockMvc.perform(post("/validaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ObjectNode validRequest() {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("name", "Ventaja del patron Pipeline");
        request.put("context", "El patron Pipeline encadena filtros que procesan datos en secuencia.");
        request.put("directQuestion", "Cual es la ventaja principal de este patron?");
        ArrayNode options = request.putArray("options");
        options.add("Permite componer validaciones independientes");
        options.add("Obliga a reescribir todo el sistema");
        options.add("Elimina la necesidad de pruebas");
        options.add("Impide reutilizar componentes");
        options.add("Requiere una base de datos distribuida");
        request.put("correctIndex", 0);
        request.put("justification", "El patron permite componer validaciones independientes.");
        request.put("bibliography", "Gamma et al. Design Patterns.");
        request.put("competence", "Diseno de software");
        request.put("topic", "Patrones de diseno");
        request.put("subtopic", "Pipeline");
        request.put("difficulty", "MEDIA");
        return request;
    }
}
