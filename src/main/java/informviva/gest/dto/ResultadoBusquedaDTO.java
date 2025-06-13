package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import informviva.gest.model.Cliente;

import informviva.gest.model.Producto;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoBusquedaDTO {
    private List<Producto> productos;
    private List<Cliente> clientes;
    private int totalProductos;
    private int totalClientes;
}