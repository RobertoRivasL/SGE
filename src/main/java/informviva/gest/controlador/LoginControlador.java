package informviva.gest.controlador;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginControlador {

    // Muestra la vista login.html
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) { // Añade HttpServletRequest y Model
        // Intenta obtener el objeto _csrf de las request attributes y añadirlo al modelo
        Object csrfToken = request.getAttribute("_csrf");
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
            System.out.println("DEBUG: _csrf object added to model in LoginController"); // Log de depuración
        } else {
            System.out.println("DEBUG: _csrf object is NULL in LoginController"); // Log de depuración
        }
        return "login";
    }
}