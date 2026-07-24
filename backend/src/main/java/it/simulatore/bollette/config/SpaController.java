package it.simulatore.bollette.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Inoltra le rotte del frontend React a index.html.
 * <p>
 * React Router gestisce la navigazione lato client (/bollette, /confronto, /parametri,
 * /offerte, /storico, /simulatore). Il server deve restituire sempre index.html per i
 * percorsi che non sono API o file statici, altrimenti un refresh su /confronto produce
 * un 404. E' l'equivalente del {@code try_files} di nginx.
 * <p>
 * Il pattern esclude i prefissi tecnici (api, actuator, swagger-ui, api-docs, assets) e
 * qualsiasi percorso che contiene un punto (i file statici: .js, .css, .svg...).
 */
@RestController
public class SpaController {

    @GetMapping(value = {
            "/",
            "/{path:^(?!api|actuator|swagger-ui|api-docs|assets)[^\\.]*$}",
            "/{path:^(?!api|actuator|swagger-ui|api-docs|assets)[^\\.]*$}/**"
    })
    public ModelAndView forward() {
        return new ModelAndView("forward:/index.html");
    }
}
