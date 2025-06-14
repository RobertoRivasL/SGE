package informviva.gest.controlador;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Venta;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Qualifier;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaControlador {

    private final VentaServicio ventaServicio;
    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;

    public VentaControlador(
            VentaServicio ventaServicio,
            ClienteServicio clienteServicio,
            @Qualifier("productoServicioOptimizado") ProductoServicio productoServicio
    ) {
        this.ventaServicio = ventaServicio;
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public String listarVentas(Model model) {
        model.addAttribute("ventas", ventaServicio.obtenerTodasLasVentas());
        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaVenta(Model model) {
        if (!model.containsAttribute("ventaDTO")) {
            model.addAttribute("ventaDTO", new VentaDTO());
        }
        model.addAttribute("clientes", clienteServicio.obtenerTodos());
        model.addAttribute("productos", productoServicio.listarActivos());
        return "ventas/nueva";
    }

    @PostMapping("/crear")
    public String crearVenta(@Valid @ModelAttribute("ventaDTO") VentaDTO ventaDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.ventaDTO", bindingResult);
            redirectAttributes.addFlashAttribute("ventaDTO", ventaDTO);
            return "redirect:/ventas/nueva";
        }

        try {
            ventaServicio.crearNuevaVenta(ventaDTO);
            redirectAttributes.addFlashAttribute("success", "Venta creada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la venta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("ventaDTO", ventaDTO);
            return "redirect:/ventas/nueva";
        }
        return "redirect:/ventas";
    }

    @GetMapping("/{id}")
    public String verDetalleVenta(@PathVariable Long id, Model model) {
        Venta venta = ventaServicio.obtenerVentaPorId(id);
        if (venta == null) {
            return "redirect:/error/404";
        }
        model.addAttribute("venta", venta);
        return "ventas/detalle";
    }
}