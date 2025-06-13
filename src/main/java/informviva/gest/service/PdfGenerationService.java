package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import informviva.gest.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
// -------------------------------------------------

@Service
public class PdfGenerationService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generateUsersPdf(List<Usuario> users) {
        Context context = new Context();
        context.setVariable("usuarios", users);

        // --- Modifica esta línea con la ruta correcta ---
        // La ruta es relativa al directorio 'templates/'
        String htmlContent = templateEngine.process("admin/usuarios.export.pdf/usuarios_pdf", context);
        // -------------------------------------------------

        System.out.println("--- HTML Generado para PDF ---");
        System.out.println(htmlContent);
        System.out.println("-----------------------------");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(htmlContent));

            document.close();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return new byte[0];
        }

        return outputStream.toByteArray();
    }
}