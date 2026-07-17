// package com.evently.pago.service;

// import com.evently.pago.dto.EventoDTO;
// import com.evently.pago.dto.ReservaDTO;
// import com.evently.pago.dto.UsuarioDTO;
// import com.evently.pago.model.Pago;
// import com.itextpdf.text.*;
// import com.itextpdf.text.pdf.*;
// import org.springframework.stereotype.Service;
// import java.io.ByteArrayOutputStream;
// import java.time.format.DateTimeFormatter;
// import java.util.Base64;

// @Service
// public class PDFService {

//     public byte[] generarBoletaPago(Pago pago, ReservaDTO reserva, UsuarioDTO cliente, EventoDTO evento) {
//         try {
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
//             // Usamos una orientación horizontal (Landscape) más natural para entradas físicas
//             Document document = new Document(PageSize.A5.rotate(), 20, 20, 20, 20);
//             PdfWriter.getInstance(document, outputStream);
//             document.open();

//             // Colores de la paleta corporativa elegante
//             BaseColor colorPrincipal = new BaseColor(79, 70, 229); // #4f46e5 (Indigo)
//             BaseColor colorTextoSecundario = new BaseColor(107, 114, 128); // Gris

//             // Fuentes
//             Font fontEmpresa = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, colorPrincipal);
//             Font fontTituloTicket = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, colorTextoSecundario);
//             Font fontEvento = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
//             Font fontLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, colorTextoSecundario);
//             Font fontValor = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
//             Font fontValorBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.BLACK);

//             // Tabla contenedora principal (diseño de boleto con dos zonas: datos y QR)
//             PdfPTable mainTable = new PdfPTable(2);
//             mainTable.setWidthPercentage(100);
//             mainTable.setWidths(new float[]{70f, 30f});

//             // --- COLUMNA IZQUIERDA: DATOS ---
//             PdfPCell leftCell = new PdfPCell();
//             leftCell.setBorder(Rectangle.NO_BORDER);
//             leftCell.setPaddingRight(20);

//             // Cabecera del Boleto
//             leftCell.addElement(new Paragraph("EVENTLY", fontEmpresa));
//             Paragraph subtitulo = new Paragraph("ENTRADA DE ACCESO", fontTituloTicket);
//             subtitulo.setSpacingAfter(15);
//             leftCell.addElement(subtitulo);

//             // Título del evento
//             Paragraph evName = new Paragraph(evento.getTitulo().toUpperCase(), fontEvento);
//             evName.setSpacingAfter(15);
//             leftCell.addElement(evName);

//             // Fila 1 de datos: Fecha y Lugar
//             PdfPTable gridTable = new PdfPTable(2);
//             gridTable.setWidthPercentage(100);
//             gridTable.setWidths(new float[]{50f, 50f});

//             PdfPCell c1 = new PdfPCell();
//             c1.setBorder(Rectangle.NO_BORDER);
//             c1.addElement(new Paragraph("FECHA Y HORA", fontLabel));
//             String fechaEventoStr = evento.getFechaEvento() != null ? 
//                 evento.getFechaEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
//             c1.addElement(new Paragraph(fechaEventoStr, fontValorBold));
//             gridTable.addCell(c1);

//             PdfPCell c2 = new PdfPCell();
//             c2.setBorder(Rectangle.NO_BORDER);
//             c2.addElement(new Paragraph("LUGAR", fontLabel));
//             c2.addElement(new Paragraph(evento.getLugar(), fontValor));
//             gridTable.addCell(c2);

//             leftCell.addElement(gridTable);
//             leftCell.addElement(Chunk.NEWLINE);

//             // Fila 2 de datos: Cliente, Cantidad y Ticket ID
//             PdfPTable gridTable2 = new PdfPTable(3);
//             gridTable2.setWidthPercentage(100);
//             gridTable2.setWidths(new float[]{45f, 25f, 30f});

//             PdfPCell c3 = new PdfPCell();
//             c3.setBorder(Rectangle.NO_BORDER);
//             c3.addElement(new Paragraph("CLIENTE", fontLabel));
//             c3.addElement(new Paragraph(cliente.getNombre() + " " + cliente.getApellido(), fontValor));
//             gridTable2.addCell(c3);

//             PdfPCell c4 = new PdfPCell();
//             c4.setBorder(Rectangle.NO_BORDER);
//             c4.addElement(new Paragraph("ENTRADAS", fontLabel));
//             c4.addElement(new Paragraph(String.valueOf(reserva.getCantidadEntradas()), fontValorBold));
//             gridTable2.addCell(c4);

//             PdfPCell c5 = new PdfPCell();
//             c5.setBorder(Rectangle.NO_BORDER);
//             c5.addElement(new Paragraph("TICKET ID", fontLabel));
//             c5.addElement(new Paragraph("#" + reserva.getId(), fontValorBold));
//             gridTable2.addCell(c5);

//             leftCell.addElement(gridTable2);
//             mainTable.addCell(leftCell);

//             // --- COLUMNA DERECHA: QR ---
//             PdfPCell rightCell = new PdfPCell();
//             rightCell.setBorder(Rectangle.LEFT); // Borde vertical tipo pre-corte
//             rightCell.setBorderWidth(1.5f);
//             rightCell.setBorderColor(new BaseColor(220, 225, 230));
//             rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//             rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//             rightCell.setPaddingLeft(15);

//             // Intentar adjuntar el código QR
//             if (reserva.getCodigoQR() != null && !reserva.getCodigoQR().isEmpty()) {
//                 try {
//                     byte[] qrBytes = Base64.getDecoder().decode(reserva.getCodigoQR());
//                     Image qrImage = Image.getInstance(qrBytes);
//                     qrImage.setAlignment(Element.ALIGN_CENTER);
//                     qrImage.scaleAbsolute(100, 100);
//                     rightCell.addElement(qrImage);
//                 } catch (Exception e) {
//                     Paragraph errPara = new Paragraph("[QR CODE ERROR]", fontLabel);
//                     errPara.setAlignment(Element.ALIGN_CENTER);
//                     rightCell.addElement(errPara);
//                 }
//             } else {
//                 Paragraph noQrPara = new Paragraph("[SIN QR]", fontLabel);
//                 noQrPara.setAlignment(Element.ALIGN_CENTER);
//                 rightCell.addElement(noQrPara);
//             }

//             // Nota pequeña de validez debajo del QR
//             Paragraph notaAcceso = new Paragraph("Presentar para ingreso", fontLabel);
//             notaAcceso.setAlignment(Element.ALIGN_CENTER);
//             notaAcceso.setSpacingBefore(10);
//             rightCell.addElement(notaAcceso);

//             mainTable.addCell(rightCell);
//             document.add(mainTable);

//             document.close();
//             return outputStream.toByteArray();

//         } catch (Exception e) {
//             throw new RuntimeException("Error al generar la entrada en PDF: " + e.getMessage());
//         }
//     }
// }
package com.evently.pago.service;

import com.evently.pago.dto.EventoDTO;
import com.evently.pago.dto.ReservaDTO;
import com.evently.pago.dto.UsuarioDTO;
import com.evently.pago.model.Pago;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PDFService {

    // ==========================
    // GEOMETRÍA CENTRALIZADA
    // ==========================
    private static final float PAGE_WIDTH = 720f;
    private static final float PAGE_HEIGHT = 300f;
    private static final float MARGIN = 0f; // controlamos el margen manualmente por celda
    private static final float STUB_WIDTH = 170f; // ancho del talón (stub)
    private static final float MAIN_WIDTH = PAGE_WIDTH - STUB_WIDTH;
    private static final float PERFORATION_RADIUS = 5f;
    private static final float PERFORATION_GAP = 14f;

    public byte[] generarBoletaPago(
            Pago pago,
            ReservaDTO reserva,
            UsuarioDTO cliente,
            EventoDTO evento
    ) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Rectangle pageSize = new Rectangle(PAGE_WIDTH, PAGE_HEIGHT);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();

            // ==========================
            // PALETA DE LA EMPRESA
            // ==========================
            BaseColor primary       = new BaseColor(124, 58, 237);   // --primary
            BaseColor primaryDark   = new BaseColor(109, 40, 217);   // --primary-dark
            BaseColor primaryLight  = new BaseColor(167, 139, 250);  // --primary-light
            BaseColor secondary     = new BaseColor(37, 99, 235);    // --secondary
            BaseColor success       = new BaseColor(16, 185, 129);   // --success
            BaseColor warning       = new BaseColor(245, 158, 11);   // --warning
            BaseColor background    = new BaseColor(248, 250, 252);  // --background
            BaseColor surface       = BaseColor.WHITE;               // --surface
            BaseColor textPrimary   = new BaseColor(15, 23, 42);     // --text-primary
            BaseColor textSecondary = new BaseColor(100, 116, 139);  // --text-secondary
            BaseColor textLight     = new BaseColor(148, 163, 184);  // --text-light
            BaseColor border        = new BaseColor(226, 232, 240);  // --border

            // ==========================
            // FUENTES
            // ==========================
            Font brandFont     = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, BaseColor.WHITE);
            Font brandSubFont  = new Font(Font.FontFamily.HELVETICA, 6.5f, Font.NORMAL, primaryLight);
            Font badgeFont     = new Font(Font.FontFamily.HELVETICA, 7.5f, Font.BOLD, primaryDark);
            Font tituloFont    = new Font(Font.FontFamily.HELVETICA, 19, Font.BOLD, textPrimary);
            Font labelFont     = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, textLight);
            Font valorFont     = new Font(Font.FontFamily.HELVETICA, 10.5f, Font.NORMAL, textPrimary);
            Font valorBold     = new Font(Font.FontFamily.HELVETICA, 10.5f, Font.BOLD, textPrimary);
            Font pagadoFont    = new Font(Font.FontFamily.HELVETICA, 9.5f, Font.BOLD, success);
            Font stubLabelFont = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.WHITE);
            Font stubValueFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            Font serialFont    = new Font(Font.FontFamily.COURIER, 7, Font.NORMAL, textLight);

            PdfContentByte canvas = writer.getDirectContent();

            // ==========================
            // FONDO GENERAL
            // ==========================
            canvas.setColorFill(surface);
            canvas.rectangle(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
            canvas.fill();

            // ==========================
            // CUERPO PRINCIPAL (izquierda)
            // ==========================
            canvas.setColorFill(background);
            canvas.rectangle(0, 0, MAIN_WIDTH, PAGE_HEIGHT);
            canvas.fill();

            // Franja superior de marca (header)
            float headerHeight = 46f;
            canvas.setColorFill(primary);
            canvas.rectangle(0, PAGE_HEIGHT - headerHeight, MAIN_WIDTH, headerHeight);
            canvas.fill();

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("EVENTLY", brandFont), 16, PAGE_HEIGHT - 27, 0);

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("PLATAFORMA OFICIAL DE ENTRADAS", brandSubFont), 16, PAGE_HEIGHT - 38, 0);

            // Serie / código de ticket, esquina superior derecha del cuerpo
            String serie = "EVT-" + String.format("%06d", reserva.getId());
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase(serie, new Font(Font.FontFamily.COURIER, 8, Font.NORMAL, BaseColor.WHITE)),
                    MAIN_WIDTH - 16, PAGE_HEIGHT - 27, 0);

            // Franja de acento inferior (detalle tipo ticket de diseño)
            canvas.setColorFill(primaryLight);
            canvas.rectangle(0, 0, MAIN_WIDTH, 4);
            canvas.fill();
            canvas.setColorFill(secondary);
            canvas.rectangle(0, 4, MAIN_WIDTH * 0.35f, 3);
            canvas.fill();

            // ==========================
            // CONTENIDO DEL CUERPO PRINCIPAL
            // ==========================
            float contentTop = PAGE_HEIGHT - headerHeight - 14;
            float contentLeft = 20;
            float contentWidth = MAIN_WIDTH - 40;

            ColumnText body = new ColumnText(canvas);
            body.setSimpleColumn(contentLeft, 20, contentLeft + contentWidth, contentTop);

            // Badge de "categoría" / tipo de acceso
            PdfPTable badgeTable = new PdfPTable(1);
            badgeTable.setWidthPercentage(38);
            badgeTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell badgeCell = new PdfPCell(new Phrase("ENTRADA GENERAL", badgeFont));
            badgeCell.setBackgroundColor(new BaseColor(237, 233, 254)); // lila muy claro
            badgeCell.setBorder(Rectangle.NO_BORDER);
            badgeCell.setPadding(5);
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeTable.addCell(badgeCell);
            body.addElement(badgeTable);

            Paragraph spacer1 = new Paragraph(6, " ");
            body.addElement(spacer1);

            // Nombre del evento, elemento dominante
            Paragraph nombreEvento = new Paragraph(evento.getTitulo().toUpperCase(), tituloFont);
            nombreEvento.setSpacingAfter(14);
            nombreEvento.setLeading(21f);
            body.addElement(nombreEvento);

            // Grid de datos (2 columnas x 3 filas)
            PdfPTable datos = new PdfPTable(2);
            datos.setWidthPercentage(100);
            datos.setWidths(new float[]{50f, 50f});
            datos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            agregarDatoBloque(datos, "FECHA Y HORA",
                    evento.getFechaEvento() != null
                            ? evento.getFechaEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm"))
                            : "N/A",
                    labelFont, valorBold);

            agregarDatoBloque(datos, "LUGAR", evento.getLugar(), labelFont, valorFont);

            agregarDatoBloque(datos, "TITULAR",
                    cliente.getNombre() + " " + cliente.getApellido(),
                    labelFont, valorFont);

            agregarDatoBloque(datos, "ENTRADAS", String.valueOf(reserva.getCantidadEntradas()), labelFont, valorBold);

            body.addElement(datos);

            Paragraph spacer2 = new Paragraph(4, " ");
            body.addElement(spacer2);

            // Línea separadora sutil + estado de pago
            PdfPTable estadoTable = new PdfPTable(2);
            estadoTable.setWidthPercentage(100);
            estadoTable.setWidths(new float[]{70f, 30f});

            PdfPCell ticketNumCell = new PdfPCell(new Phrase("N° TICKET  #" + reserva.getId(), labelFont));
            ticketNumCell.setBorder(Rectangle.TOP);
            ticketNumCell.setBorderColor(border);
            ticketNumCell.setBorderWidth(1);
            ticketNumCell.setPaddingTop(8);
            estadoTable.addCell(ticketNumCell);

            PdfPCell estadoCell = new PdfPCell(new Phrase("● PAGADO", pagadoFont));
            estadoCell.setBorder(Rectangle.TOP);
            estadoCell.setBorderColor(border);
            estadoCell.setBorderWidth(1);
            estadoCell.setPaddingTop(8);
            estadoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            estadoTable.addCell(estadoCell);

            body.addElement(estadoTable);
            body.go();

            // ==========================
            // TALÓN / STUB (derecha) — look de "ADMIT ONE"
            // ==========================
            float stubX = MAIN_WIDTH;
            canvas.setColorFill(primaryDark);
            canvas.rectangle(stubX, 0, STUB_WIDTH, PAGE_HEIGHT);
            canvas.fill();

            // Texto vertical "ADMIT ONE • EVENTLY" en el borde izquierdo del stub
            ColumnText verticalText = new ColumnText(canvas);
            verticalText.setSimpleColumn(
                    new Phrase("ADMIT ONE  •  EVENTLY  •  ADMIT ONE", stubLabelFont),
                    stubX + 6, 20, stubX + 20, PAGE_HEIGHT - 20, 12, Element.ALIGN_LEFT
            );
            canvas.saveState();
            canvas.beginText();
            canvas.endText();
            // Rotamos el texto 90° usando una matriz de transformación
            ColumnText rotated = new ColumnText(canvas);
            rotated.setCanvas(canvas);
            java.awt.geom.AffineTransform transform =
                    java.awt.geom.AffineTransform.getTranslateInstance(stubX + 14, 30);
            transform.rotate(Math.PI / 2);
            canvas.transform(transform);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("ADMIT ONE  ·  EVENTLY OFICIAL  ·  VALIDO SOLO PARA ESTE EVENTO", stubLabelFont),
                    0, 0, 0);
            canvas.restoreState();

            // QR centrado en el stub
            float qrSize = 108f;
            float qrX = stubX + (STUB_WIDTH - qrSize) / 2 + 8;
            float qrY = PAGE_HEIGHT - 40 - qrSize;

            canvas.setColorFill(surface);
            canvas.roundRectangle(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 8);
            canvas.fill();

            if (reserva.getCodigoQR() != null && !reserva.getCodigoQR().isEmpty()) {
                byte[] bytes = Base64.getDecoder().decode(reserva.getCodigoQR());
                Image qr = Image.getInstance(bytes);
                qr.scaleToFit(qrSize, qrSize);
                qr.setAbsolutePosition(qrX, qrY);
                canvas.addImage(qr);
            } else {
                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                        new Phrase("SIN QR", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, primaryDark)),
                        qrX + qrSize / 2, qrY + qrSize / 2, 0);
            }

            // Número de ticket bajo el QR
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase("#" + reserva.getId(), stubValueFont),
                    stubX + STUB_WIDTH / 2 + 4, qrY - 18, 0);

            // Barcode simulado (barras verticales de ancho variable, look decorativo)
            drawFakeBarcode(canvas, stubX + 20, 14, STUB_WIDTH - 40, 22, reserva.getId());

            // ==========================
            // PERFORACIÓN entre stub y cuerpo principal
            // (círculos del color de fondo de página, simulando huecos)
            // ==========================
            float perfX = stubX;
            float y = PERFORATION_RADIUS;
            canvas.setColorFill(surface);
            while (y < PAGE_HEIGHT) {
                canvas.circle(perfX, y, PERFORATION_RADIUS);
                canvas.fill();
                y += PERFORATION_GAP;
            }

            // Línea punteada sutil sobre la perforación
            canvas.saveState();
            canvas.setColorStroke(border);
            canvas.setLineWidth(1f);
            canvas.setLineDash(3, 3);
            canvas.moveTo(perfX, 0);
            canvas.lineTo(perfX, PAGE_HEIGHT);
            canvas.stroke();
            canvas.restoreState();

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando boleto PDF: " + e.getMessage());
        }
    }

    /**
     * Agrega un bloque etiqueta/valor apilado verticalmente (estilo "campo de formulario"),
     * más legible que el par lado a lado de la versión anterior.
     */
    private void agregarDatoBloque(
            PdfPTable tabla,
            String etiqueta,
            String valor,
            Font etiquetaFont,
            Font valorFont
    ) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPaddingBottom(10);
        celda.setPaddingRight(10);

        Paragraph label = new Paragraph(etiqueta, etiquetaFont);
        label.setSpacingAfter(2);
        celda.addElement(label);
        celda.addElement(new Paragraph(valor, valorFont));

        tabla.addCell(celda);
    }

    /**
     * Dibuja un barcode decorativo (no escaneable, solo estético) para reforzar
     * la sensación de ticket real. Las barras varían de ancho según el ID
     * para que cada ticket se vea "único".
     */
    private void drawFakeBarcode(PdfContentByte canvas, float x, float y, float width, float height, Long seed) {
        canvas.saveState();
        canvas.setColorFill(BaseColor.WHITE);

        long s = (seed == null ? 1 : Math.abs(seed)) + 17;
        float cursor = x;
        while (cursor < x + width) {
            float barWidth = 1f + (s % 3); // 1..3
            canvas.rectangle(cursor, y, barWidth, height);
            canvas.fill();
            cursor += barWidth + 1.5f;
            s = (s * 31 + 7);
        }
        canvas.restoreState();
    }
}