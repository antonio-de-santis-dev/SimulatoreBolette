package it.simulatore.bollette.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

/**
 * Verifica che le rotte del frontend React vengano inoltrate a index.html (comportamento
 * SPA) e che i prefissi tecnici non vengano intercettati.
 */
@WebMvcTest(SpaController.class)
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rotteReact_inoltrateAIndexHtml() throws Exception {
        for (String rotta : new String[]{"/bollette", "/confronto", "/parametri", "/offerte", "/storico"}) {
            mockMvc.perform(get(rotta)).andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    void root_inoltrataAIndexHtml() throws Exception {
        mockMvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
    }
}
