import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.FileOutputStream;

public class TestPDF {
    public static void main(String[] args) {
        try {
            String path = "test_output.pdf";
            PdfWriter writer = new PdfWriter(new FileOutputStream(path));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Hello FinTrack Pro! iText is working."));
            document.close();
            System.out.println("Success! PDF created at: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
