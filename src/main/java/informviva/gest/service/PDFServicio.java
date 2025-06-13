package informviva.gest.service;

import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import informviva.gest.model.Cliente;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Service
public class PDFServicio {

    public void exportarListadoClientes(HttpServletResponse response, List<Cliente> listaClientes) throws IOException {
        // Crear el documento PDF
        Document document = new Document(PageSize.A4.rotate()); // Horizontal
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Título del documento
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("Listado de Clientes", tituloFont);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);

        // Crear la tabla
        PdfPTable tabla = new PdfPTable(5); // 5 columnas
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2f, 3f, 3f, 3f, 3f});
        tabla.setSpacingBefore(10);

        // Encabezados
        agregarEncabezadoTabla(tabla);

        // Datos
        for (Cliente cliente : listaClientes) {
            tabla.addCell(String.valueOf(cliente.getId()));
            tabla.addCell(cliente.getNombre());
            tabla.addCell(cliente.getApellido());
            tabla.addCell(cliente.getEmail());
            tabla.addCell(cliente.getTelefono());
        }

        document.add(tabla);
        document.close();
    }

    private void agregarEncabezadoTabla(PdfPTable tabla) {
        Font fuenteEncabezado = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        Color colorFondo = Color.DARK_GRAY;

        agregarCeldaEncabezado(tabla, "ID", fuenteEncabezado, colorFondo);
        agregarCeldaEncabezado(tabla, "Nombre", fuenteEncabezado, colorFondo);
        agregarCeldaEncabezado(tabla, "Apellido", fuenteEncabezado, colorFondo);
        agregarCeldaEncabezado(tabla, "Email", fuenteEncabezado, colorFondo);
        agregarCeldaEncabezado(tabla, "Teléfono", fuenteEncabezado, colorFondo);
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(5);
        tabla.addCell(celda);
    }
}
