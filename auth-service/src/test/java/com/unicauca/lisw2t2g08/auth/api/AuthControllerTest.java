package com.unicauca.lisw2t2g08.auth.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Prueba de punta a punta del contrato REST, con SQLite en memoria y Argon2 real

@SpringBootTest(properties = "auth.db.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    private void register(String login, String password) throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"" + login + "\",\"fullName\":\"Nombre Prueba\","
                        + "\"role\":\"AUTOR_PREGUNTAS\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarYHacerLoginDevuelveTokenFirmado() throws Exception {
        register("ana", "Clave123!");

        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"ana\",\"password\":\"Clave123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("ana"))
                .andExpect(jsonPath("$.role").value("AUTOR_PREGUNTAS"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void registroNoExponeElHashDeLaClave() throws Exception {
        register("beto", "Clave123!");

        mvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].login", hasItem("beto")))
                .andExpect(content().string(not(containsString("argon2"))))
                .andExpect(content().string(not(containsString("hashed"))));
    }

    @Test
    void loginConClaveIncorrectaEs401() throws Exception {
        register("carla", "Clave123!");

        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"carla\",\"password\":\"Mala123!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDuplicadoEs409() throws Exception {
        register("dani", "Clave123!");

        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"dani\",\"fullName\":\"X\",\"role\":\"DOCENTE\",\"password\":\"Clave123!\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void claveDebilEs400ConLaListaDeErrores() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"eva\",\"fullName\":\"X\",\"role\":\"DOCENTE\",\"password\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void rolInvalidoEs400() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"fer\",\"fullName\":\"X\",\"role\":\"SUPERMAN\",\"password\":\"Clave123!\"}"))
                .andExpect(status().isBadRequest());
    }
}
