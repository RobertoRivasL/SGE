package informviva.gest.service.procesador;


import informviva.gest.dto.ClienteDTO;
import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.importacion.ImportacionValidador;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ProcesadorCliente implements ProcesadorEntidad<Cliente> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorCliente.class);

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ImportacionValidador validador;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public String getTipoEntidad() {
        return "cliente";
    }

    @Override
    @Transactional
    public Cliente mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Cliente cliente = new Cliente();

            cliente.setNombre(obtenerValorString(fila, "nombre"));
            cliente.setApellido(obtenerValorString(fila, "apellido"));
            cliente.setEmail(obtenerValorString(fila, "email"));
            cliente.setRut(obtenerValorString(fila, "rut"));
            cliente.setTelefono(obtenerValorString(fila, "telefono"));
            cliente.setDireccion(obtenerValorString(fila, "direccion"));
            cliente.setCategoria(obtenerValorString(fila, "categoria"));
            cliente.setFechaRegistro(LocalDateTime.now());

            return cliente;
        } catch (Exception e) {
            logger.error("Error mapeando cliente en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Cliente cliente) {
        return clienteServicio.existeClienteConRut(cliente.getRut());
    }

    @Override
    @Transactional
    public void guardarEntidad(Cliente cliente) {
        ClienteDTO clienteDTO = modelMapper.map(cliente, ClienteDTO.class);
        clienteServicio.guardar(clienteDTO);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Cliente cliente, int numeroFila) {
        // Crear mapa temporal para usar el validador existente
        Map<String, Object> fila = Map.of(
                "nombre", cliente.getNombre(),
                "apellido", cliente.getApellido(),
                "email", cliente.getEmail(),
                "rut", cliente.getRut(),
                "telefono", cliente.getTelefono() != null ? cliente.getTelefono() : "",
                "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "",
                "categoria", cliente.getCategoria() != null ? cliente.getCategoria() : ""
        );

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}