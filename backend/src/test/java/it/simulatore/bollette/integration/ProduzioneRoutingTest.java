package it.simulatore.bollette.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di regressione che avrebbero intercettato i bug di produzione
 * (403 "Invalid CORS request" sulle POST e routing SPA) prima del deploy.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProduzioneRoutingTest {

    @Autowired MockMvc mockMvc;

    /** Riproduce il bug: POST same-origin con header Origin non deve dare 403. */
    @Test
    void postConOriginProduzione_nonDeveEssere403() throws Exception {
        mockMvc.perform(post("/api/simulazioni")
                .header("Origin", "https://simulatorebolette.fly.dev")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"test","tipoCliente":"RESIDENTE",
                     "potenzaContrattuale":"KW_3","consumoTotaleKwh":450,
                     "tipoTariffa":"MONORARIA"}
                    """))
            .andExpect(status().is(not(403)));
    }

    /** Le rotte React devono restituire index.html, non 404. */
    @Test
    void rottaSpa_restituisceIndexHtml() throws Exception {
        mockMvc.perform(get("/simulatore"))
            .andExpect(status().isOk());
    }

    /** Gli endpoint API inesistenti devono dare JSON, non HTML. */
    @Test
    void apiInesistente_restituisce404Json() throws Exception {
        mockMvc.perform(get("/api/percorso-inesistente"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
