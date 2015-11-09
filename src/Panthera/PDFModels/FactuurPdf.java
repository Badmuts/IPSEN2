package Panthera.PDFModels;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Panthera.DAO.SettingsDAO;
import Panthera.Models.Debiteur;
import Panthera.Models.Factuur;
import Panthera.Models.Factuurregel;
import Panthera.Models.Settings;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.omg.IOP.Encoding;


public class FactuurPdf {
    /**
     * attributen
     */
    private Factuur factuur;
    private ArrayList<Factuurregel> factuurregels = new ArrayList<>();
    private Debiteur debiteur;
    private Document document;
    private Paragraph preface;
    private Double totaalbedrag = 0.0;
    private String FILE;
    private PdfWriter writer;
    private Rectangle rect;
    private ArrayList<Settings> settings = new ArrayList<>();


    /**
     * constructors
     * @param factuur
     * @param factuurregels
     * @param debiteur
     */
    public FactuurPdf(Factuur factuur, ArrayList<Factuurregel> factuurregels, Debiteur debiteur) {
        this.factuur = factuur;
        this.factuurregels = factuurregels;
        this.debiteur = debiteur;

        try {
                FILE = "" + factuur.getFactuurnummer()+"-"+ debiteur.getNaam() +".pdf";
                factuur.setPDF(this);
                String pdfPath = FILE;
                factuur.setPdfPath(FILE);
                this.document = new Document();
                this.writer = PdfWriter.getInstance(document, new FileOutputStream(FILE));
                rect = new Rectangle(30, 30, 550, 800);
                writer.setBoxSize("art", rect);
                document.open();
                addTitlePage(document);
                document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * deze methode maakt de titelpagina aan hierin staan de gegevens van het lid dat producten heeft besteld
     * @param document
     * @throws Exception
     */
    private void addTitlePage(Document document) throws Exception {
        preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(debiteur.getAanhef() + " " + debiteur.getVoornaam() + " " + debiteur.getTussenvoegsel() + " " + debiteur.getNaam()));
        preface.add(new Paragraph(debiteur.getAdres()));
        preface.add(new Paragraph(debiteur.getWoonplaats() + " " + debiteur.getPostcode()));
        addEmptyLine(preface, 2);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        preface.add(new Paragraph("Factuurdatum: " + df.format(factuur.getFactuurdatum())));
        preface.add(new Paragraph("Vervaldatum: " + df.format(factuur.getVervaldatum())));
        preface.add(new Paragraph(String.valueOf("Lidnnummer: " + debiteur.getId())));
        addEmptyLine(preface, 3);
        preface.add(new Paragraph("Betreft: Wijnbestelling Benefiet Wijnfestijn Oud Poelgeest 01 november 2015"));
        addEmptyLine(preface, 2);
        document.add(preface);
        createTable();
        showOpmerking();
        onEndPage();
    }


    /**
     * Deze methode laat de opmerking zien die is ingevuld bij het aanmaken van een factuur

     * @throws Exception
     */
    public void showOpmerking() throws  Exception {
        Paragraph opmerking = new Paragraph();
        addEmptyLine(opmerking, 2);
        opmerking.add(new Paragraph(factuur.getOpmerking()));
        document.add(opmerking);
    }

    /**
     * Deze methode maakt een table aan en vult deze met de properties uit het model dat je wilt weergeven
     * @throws Exception
     */
    private void createTable() throws Exception {
        PdfPTable table = new PdfPTable(4);
        createHeader(table);
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        otherSymbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", otherSymbols);
        for(Factuurregel factuurregel: factuurregels) {
            table.addCell(String.valueOf(factuurregel.getAantal()));
            table.addCell(String.valueOf(factuurregel.getProduct().getProductnummer()));
            table.addCell(factuurregel.getProduct().getNaam());
            table.addCell("\u20ac " + String.valueOf(df.format(calculatePrice(factuurregel))));
        }
        table.setWidthPercentage(100);
        createFooter(table, this.factuurregels);
        document.add(table);
    }

    /**
     * deze methode maakt de footer aan waarin het totaalbedrag van de factuur staat
     * @param table
     * @param factuurregels
     */
    public void createFooter(PdfPTable table, ArrayList<Factuurregel> factuurregels) {
        char symbol = '\u20ac';
        table.addCell("");
        table.addCell("");
        table.addCell("");
        table.addCell(symbol + " " + calculateTotalPrice(factuurregels));
    }



    /**
     * deze methode berekent per wijn op de factuur de prijs die daar bij hoort
     * @param regel
     * @return
     */
    public double calculatePrice(Factuurregel regel) {
        double prijs;
        prijs = regel.getAantal() * regel.getPrijs();
        DecimalFormat df = new DecimalFormat("0.00");
        df.format(prijs);
        return prijs;
    }

    /**
     * deze methode berekend de totaalprijs van alle producten op de factuur
     * @param factuurregels
     * @return
     */
    public String calculateTotalPrice(ArrayList<Factuurregel> factuurregels) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        otherSymbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", otherSymbols);
        double prijs = 0.00;
            for(Factuurregel factuurregel: factuurregels) {
                prijs += factuurregel.getPrijs() * factuurregel .getAantal();
            }
        return df.format(prijs);
    }

    /**
     * deze methode maakt de header aan van de tabel waarin de factuurregels staan
     * @param table
     */
    public void createHeader(PdfPTable table) {
        table.addCell("Aantal");
        table.addCell("Productnummer");
        table.addCell("Product");
        table.addCell("Prijs");
    }


    /**
     * deze methode maakt de footer van de pdf aan
     * @throws Exception
     */
    public void onEndPage() throws Exception {
        this.settings = new SettingsDAO().getAllSettings();
        LineSeparator ls = new LineSeparator();
        document.add(new Chunk(ls));
        rect = writer.getBoxSize("art");
        for (Settings setting : settings) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(setting.getBedrijfsnaam()),
                    document.leftMargin() - 1, document.bottom() + 90, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Bankrekening "+setting.getIban()),
                    document.leftMargin() - 1, document.bottom() + 70, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Inschrijfnummer KvK Rijnland: "+setting.getKvK()),
                    document.leftMargin() - 1, document.bottom() + 50, 0);
        }
    }

    /**
     * deze methode is er om gemakkelijk een enter toe te voegen in het pdfdocument
     * @param paragraph
     * @param number
     */
    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}