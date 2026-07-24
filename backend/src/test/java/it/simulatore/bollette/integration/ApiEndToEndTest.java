package it.simulatore.bollette.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end sull'intera pila HTTP (controller + serializzazione JSON + servizi + DB H2):
 * carica la bolletta FuturEnergy seedata, esegue il confronto e verifica la quadratura,
 * come richiesto dal passo di verifica finale.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiEndToEndTest {

    @Autowired private TestRestTemplate rest;

    @Test
    void flussoCompleto_inserisciFuturEnergy_confronta_verifica() {
        // 1. La bolletta FuturEnergy e' presente
        JsonNode bollette = rest.getForObject("/api/bollette-concorrenti", JsonNode.class);
        JsonNode futur = null;
        for (JsonNode b : bollette) {
            if ("FuturEnergy Rinnovabile".equals(b.path("nomeFornitore").asText())) { futur = b; break; }
        }
        assertThat(futur).isNotNull();
        long bollettaId = futur.path("id").asLong();

        // 2. Quadratura degli importi fatturati
        JsonNode quad = rest.getForObject("/api/bollette-concorrenti/" + bollettaId + "/quadratura", JsonNode.class);
        assertThat(quad.path("valida").asBoolean()).isTrue();

        // 3. Offerta del gestore con voci
        JsonNode offerte = rest.getForObject("/api/offerte", JsonNode.class);
        long offertaId = -1;
        for (JsonNode o : offerte) {
            if ("Luce Trioraria Web".equals(o.path("nomeOfferta").asText())) { offertaId = o.path("id").asLong(); break; }
        }
        assertThat(offertaId).isPositive();

        // 4. Confronto via POST
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("bollettaConcorrenteId", bollettaId,
                "offertaGestoreId", offertaId, "salva", true);
        ResponseEntity<JsonNode> resp = rest.postForEntity("/api/confronti",
                new HttpEntity<>(body, h), JsonNode.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode r = resp.getBody();
        assertThat(r).isNotNull();
        assertThat(r.path("concorrenteTotale").asDouble()).isEqualTo(118.34);
        assertThat(r.path("categorie")).hasSize(5);
        assertThat(r.path("mesi")).hasSize(1);
        assertThat(r.path("risparmioBimestrale").isNumber()).isTrue();
        assertThat(r.path("aliquotaIvaApplicata").asDouble()).isEqualTo(0.10);
        // Il dettaglio riga espone l'origine (nazionale/gestore/offerta)
        assertThat(r.path("mesi").get(0).path("righe").get(0).path("origine").asText()).isNotEmpty();

        // 5. Il confronto e' finito nello storico
        JsonNode storico = rest.getForObject("/api/confronti", JsonNode.class);
        assertThat(storico.isArray()).isTrue();
        assertThat(storico.size()).isGreaterThan(0);
    }
}
