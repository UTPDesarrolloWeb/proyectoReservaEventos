package com.evently.backend.service;

import com.evently.backend.model.Pago;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PDFService {

    public byte[] generarBoletaPago(Pago pago) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fuentes
            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18,
                    Font.BOLD, BaseColor.DARK_GRAY);
            Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 12,
                    Font.BOLD, BaseColor.DARK_GRAY);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 11,
                    Font.NORMAL, BaseColor.BLACK);
            Font fontVerde = new Font(Font.FontFamily.HELVETICA, 12,
                    Font.BOLD, new BaseColor(46, 139, 87));

            // Título
            Paragraph titulo = new Paragraph("EVENTLY", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph(
                    "Comprobante de Pago", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);

            // Línea separadora
            document.add(new Chunk(new LineSeparator()));
            document.add(Chunk.NEWLINE);

            // Datos del cliente
            document.add(new Paragraph("DATOS DEL CLIENTE", fontSubtitulo));
            document.add(new Paragraph("Nombre: " +
                    pago.getReserva().getCliente().getNombre() + " " +
                    pago.getReserva().getCliente().getApellido(), fontNormal));
            document.add(new Paragraph("Email: " +
                    pago.getReserva().getCliente().getEmail(), fontNormal));
            document.add(Chunk.NEWLINE);

            // Datos del evento
            document.add(new Paragraph("DATOS DEL EVENTO", fontSubtitulo));
            document.add(new Paragraph("Evento: " +
                    pago.getReserva().getEvento().getTitulo(), fontNormal));
            document.add(new Paragraph("Fecha: " +
                    pago.getReserva().getEvento().getFechaEvento(), fontNormal));
            document.add(new Paragraph("Lugar: " +
                    pago.getReserva().getEvento().getLugar(), fontNormal));
            document.add(new Paragraph("Entradas: " +
                    pago.getReserva().getCantidadEntradas(), fontNormal));
            document.add(Chunk.NEWLINE);

            // Datos del pago
            document.add(new Paragraph("DATOS DEL PAGO", fontSubtitulo));
            document.add(new Paragraph("Transacción: " +
                    pago.getTransaccionId(), fontNormal));
            document.add(new Paragraph("Método de pago: " +
                    pago.getMetodoPago(), fontNormal));
            document.add(new Paragraph("Fecha de pago: " +
                    pago.getFechaPago(), fontNormal));
            document.add(new Paragraph("Monto total: S/. " +
                    pago.getMonto(), fontNormal));
            document.add(Chunk.NEWLINE);

            // Estado
            document.add(new Chunk(new LineSeparator()));
            document.add(Chunk.NEWLINE);
            Paragraph estado = new Paragraph(
                    "** PAGO COMPLETADO **", fontVerde);
            estado.setAlignment(Element.ALIGN_CENTER);
            document.add(estado);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al generar la boleta: " + e.getMessage());
        }
    }
}