package org.example.demo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.awt.Desktop;

public class HelloController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField universityField;

    @FXML
    private Button findButton;

    private String selectedImagePath;

    @FXML
    void findPhoto(ActionEvent event) {
        List<String> lstFile = Arrays.asList("*.png", "*.jpg", "*.jpeg");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", lstFile));
        File f = fc.showOpenDialog(null);

        Optional.ofNullable(f)
                .ifPresent(file -> selectedImagePath = file.getAbsolutePath());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void onGenerateButtonClick() {
        String validationMessage = isInputValid();
        if (validationMessage != null) {
            showErrorAlert(validationMessage);
            return;
        }

        String lengthValidationMessage = checkFieldLengths();
        if (lengthValidationMessage != null) {
            showErrorAlert(lengthValidationMessage);
            return;
        }

        String digitValidationMessage = checkForDigits();
        if (digitValidationMessage != null) {
            showErrorAlert(digitValidationMessage);
            return;
        }

        generatePdfDocument();
    }

    private void generatePdfDocument() {
        Document document = null;
        String pdfFilePath = "YourCV.pdf";
        try {
            document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream("YourCV.pdf"));

            document.open();

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                Path path = Paths.get(selectedImagePath);
                Image img = Image.getInstance(path.toAbsolutePath().toString());

                float targetWidth = 150f;
                float targetHeight = 150f;

                if (img.getWidth() > targetWidth || img.getHeight() > targetHeight) {
                    img.scaleToFit(targetWidth, targetHeight);
                }

                img.setAbsolutePosition(document.right() - targetWidth, document.top() - targetHeight);
                document.add(img);
            }

            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            Font labelfont = FontFactory.getFont(FontFactory.COURIER_BOLD, 16, BaseColor.BLACK);

            String fullName = nameField.getText() + " " + surnameField.getText();
            addFieldToDocument(document, "Full name: ", labelfont);
            addFieldToDocument(document, fullName, font);
            addFieldToDocument(document, "City: ", labelfont);
            addFieldToDocument(document, cityField.getText(), font);
            addFieldToDocument(document, "Education: ", labelfont);
            addFieldToDocument(document, universityField.getText(), font);

            Alert successAlert = new Alert(AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText(null);
            successAlert.setContentText("PDF file was successfully created.");

            ButtonType openButton = new ButtonType("Open");
            successAlert.getButtonTypes().add(openButton);

            Optional<ButtonType> result = successAlert.showAndWait();

            if (result.isPresent() && result.get() == openButton) {
                openPdfFile(pdfFilePath);
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            showErrorAlert("Error occurred: " + e.getMessage());
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void addFieldToDocument(Document document, String value, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(value, font);
        document.add(paragraph);
    }

    private void openPdfFile(String filePath) {
        try {
            File pdfFile = new File(filePath);
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error occurred while opening PDF file: " + e.getMessage());
        }
    }

    private String isInputValid() {
        if (nameField.getText().isEmpty()) {
            return "Please enter the Name field";
        }
        if (surnameField.getText().isEmpty()) {
            return "Please enter the Surname field";
        }
        if (cityField.getText().isEmpty()) {
            return "Please enter the City field";
        }
        if (universityField.getText().isEmpty()) {
            return "Please enter the University field";
        }
        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            return "Please insert an image";
        }
        return null;
    }

    private String checkFieldLengths() {
        if (nameField.getText().length() > 30) {
            return "Name field length should not exceed 30 characters";
        }
        if (surnameField.getText().length() > 30) {
            return "Surname field length should not exceed 30 characters";
        }
        if (cityField.getText().length() > 30) {
            return "City field length should not exceed 30 characters";
        }
        if (universityField.getText().length() > 30) {
            return "University field length should not exceed 30 characters";
        }
        return null;
    }

    private String checkForDigits() {
        if (containsDigits(nameField.getText())) {
            return "Name field should not contain digits";
        }
        if (containsDigits(surnameField.getText())) {
            return "Surname field should not contain digits";
        }
        if (containsDigits(cityField.getText())) {
            return "City field should not contain digits";
        }
        return null;
    }

    private boolean containsDigits(String input) {
        return input.chars().anyMatch(Character::isDigit);
    }
}
