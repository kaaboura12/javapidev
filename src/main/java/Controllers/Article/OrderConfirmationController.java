package Controllers.Article;

import Models.Article;
import Models.Commande;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OrderConfirmationController implements Initializable {
    @FXML private ImageView logoImage;
    @FXML private Label orderNumberLabel;
    @FXML private Label dateLabel;
    @FXML private Label articleLabel;
    @FXML private Label quantityLabel;
    @FXML private Label unitPriceLabel;
    @FXML private Label totalLabel;
    @FXML private Button downloadInvoiceBtn;
    @FXML private Button closeBtn;

    private Commande commande;
    private Article article;
    private String formattedDate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("Erreur de chargement du logo: " + e.getMessage());
        }
    }

    public void initData(Commande commande, Article article) {
        this.commande = commande;
        this.article = article;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.formattedDate = LocalDateTime.now().format(formatter);
        
        orderNumberLabel.setText(commande.getNumero());
        dateLabel.setText(formattedDate);
        articleLabel.setText(article.getTitre());
        quantityLabel.setText(String.valueOf(commande.getQuantite()));
        unitPriceLabel.setText(String.format("%.2f Dt", article.getPrix()));
        totalLabel.setText(String.format("%.2f Dt", commande.getTotal()));
    }

    @FXML
    private void handleDownloadInvoice() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la facture");
        fileChooser.setInitialFileName("facture_" + commande.getNumero() + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(downloadInvoiceBtn.getScene().getWindow());
        if (file != null) {
            generateInvoice(file);
        }
    }

    private void generateInvoice(File file) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Définition des couleurs
            BaseColor darkBlue = new BaseColor(30, 30, 48);    // #1e1e30
            BaseColor coral = new BaseColor(255, 126, 95);     // #ff7e5f
            BaseColor lightCoral = new BaseColor(254, 180, 123); // #feb47b
            BaseColor lightGray = new BaseColor(30, 30, 48); // #e0e0e0

            // Add logo
            try {
                byte[] logoBytes = getClass().getResourceAsStream("/images/logo.png").readAllBytes();
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoBytes);
                logo.scaleToFit(150, 150);
                logo.setAlignment(Element.ALIGN_LEFT);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout du logo: " + e.getMessage());
            }

            // Définition des polices
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, coral);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, coral);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, lightGray);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, lightGray);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, lightGray);

            // En-tête de l'entreprise
            Paragraph companyInfo = new Paragraph();
            companyInfo.setAlignment(Element.ALIGN_RIGHT);
            companyInfo.add(new Phrase("ARTXCAPE\n", headerFont));
            companyInfo.add(new Phrase("Votre marketplace d'art préférée\n", smallFont));
            companyInfo.add(new Phrase("Email: artxcape@gmail.com\n", smallFont));
            companyInfo.add(new Phrase("Tél: +216 99 276 059\n", smallFont));
            document.add(companyInfo);

            // Espace
            document.add(new Paragraph("\n"));

            // Titre FACTURE
            Paragraph title = new Paragraph("FACTURE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Rectangle décoratif
            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorFill(coral);
            canvas.rectangle(50, document.getPageSize().getHeight() - 180, document.getPageSize().getWidth() - 100, 2);
            canvas.fill();

            // Informations de la facture
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(20);
            infoTable.setSpacingAfter(30);

            // Cellule gauche
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Phrase("Facture N°: " + commande.getNumero() + "\n", headerFont));
            leftCell.addElement(new Phrase("Date: " + formattedDate + "\n", normalFont));
            infoTable.addCell(leftCell);

            // Cellule droite (vide pour l'alignement)
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(rightCell);

            document.add(infoTable);

            // Tableau des articles
            PdfPTable table = new PdfPTable(new float[]{3, 1, 2, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);
            table.setSpacingAfter(20);

            // En-têtes du tableau
            PdfPCell[] headers = new PdfPCell[]{
                new PdfPCell(new Phrase("Article", headerFont)),
                new PdfPCell(new Phrase("Quantité", headerFont)),
                new PdfPCell(new Phrase("Prix unitaire", headerFont)),
                new PdfPCell(new Phrase("Total", headerFont))
            };

            for (PdfPCell header : headers) {
                header.setBackgroundColor(darkBlue);
                header.setBorderColor(coral);
                header.setPadding(10);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            // Données du tableau
            PdfPCell[] cells = new PdfPCell[]{
                new PdfPCell(new Phrase(article.getTitre(), normalFont)),
                new PdfPCell(new Phrase(String.valueOf(commande.getQuantite()), normalFont)),
                new PdfPCell(new Phrase(String.format("%.2f Dt", article.getPrix()), normalFont)),
                new PdfPCell(new Phrase(String.format("%.2f Dt", commande.getTotal()), boldFont))
            };

            for (PdfPCell cell : cells) {
                cell.setBackgroundColor(BaseColor.WHITE);
                cell.setBorderColor(coral);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            document.add(table);

            // Total
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingBefore(20);

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total à payer:", headerFont));
            totalLabelCell.setBorder(Rectangle.NO_BORDER);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("%.2f Dt", commande.getTotal()), headerFont));
            totalValueCell.setBorder(Rectangle.NO_BORDER);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(totalValueCell);

            document.add(totalTable);

            // Rectangle décoratif bas
            canvas.setColorFill(coral);
            canvas.rectangle(50, 120, document.getPageSize().getWidth() - 100, 2);
            canvas.fill();

            // Pied de page
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Phrase("Merci de votre confiance !\n", headerFont));
            footer.add(new Phrase("ARTXCAPE - Votre marketplace d'art préférée\n", smallFont));
            document.add(footer);

            document.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la facture: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    private StackPane findContentArea() {
        return (StackPane) closeBtn.getScene().lookup("#contentArea");
    }
} 