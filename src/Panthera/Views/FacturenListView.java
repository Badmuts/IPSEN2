package Panthera.Views;

import java.util.Date;

import Panthera.Panthera;
import Panthera.Controllers.FacturenController;


import Panthera.Factories.CheckBoxCellFactory;
import Panthera.Models.Debiteur;
import Panthera.Models.Factuur;
import Panthera.Panthera;
import com.itextpdf.text.pdf.PdfDocument;

import Panthera.Controllers.InkoopfactuurController;
import Panthera.Models.Factuur;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by Brandon on 23-Sep-15.
 */
public class FacturenListView extends BorderPane implements Viewable {

    private FacturenController facturenController;
    private Stage stage = Panthera.getInstance().getStage();
    private TableView<Factuur> table;
    private ObservableList<Factuur> facturen = FXCollections.observableArrayList();
    private FilteredList<Factuur> filteredData;
    private TextField filterField;
    private HBox topContainer = new HBox(10);
    private InkoopfactuurController inkoopfactuurController;

    public FacturenListView(FacturenController facturenController)  {
        setPadding(new Insets(22));
        topContainer.setPadding(new Insets(0, 0, 22, 0));
        this.facturenController = facturenController;
        this.inkoopfactuurController = new InkoopfactuurController();
        createHeader();
        createTableView();

        new Thread(() -> {
            this.facturen = this.facturenController.cmdGetFacturen();
            table.setItems(facturen);
            filterFacturen();
        }).start();

    }


    private void filterFacturen() {
        this.filteredData = new FilteredList<Factuur>(this.facturen, p -> true);

        this.filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.filteredData.setPredicate(factuur -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (factuur.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (factuur.getDebiteur().getVoornaam().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (factuur.getDebiteur().getNaam().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (Integer.toString(factuur.getFactuurnummer()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Factuur> sortedData = new SortedList<>(this.filteredData);
        sortedData.comparatorProperty().bind(this.table.comparatorProperty());

        this.table.setItems(sortedData);
    }

    private void createHeader() {
        createTitle();
        createTextField();
        createAddFactuurButton();
        createRemoveFactuurButton();
        CreateVerzendFactuurButton();
        createUpdateFactuurButton();
        createGenerateInkoopfactuurButton();
        createTextField();

        setTop(topContainer);
    }
    
    /**
     * Creates the button to generate an inkoopfactuur.
     * bit of a long name.
     * @author Roy
     */
    private void createGenerateInkoopfactuurButton() {
    	System.out.println("test");
    	Button button = new Button("Inkoopfactuur Opstellen");
    	button.setOnAction(e -> {
            inkoopfactuurController.generateInkoopfactuur(facturen);
        });
    	topContainer.getChildren().add(button);
    }

    private void createRemoveFactuurButton() {
        Button button = new Button("Factuur verwijderen");
        button.setOnAction(event -> facturenController.cmcDeleteFactuur(facturen));
        button.getStyleClass().addAll("btn", "btn-danger");
        topContainer.getChildren().add(button);
    }


    private void CreateVerzendFactuurButton() {
        Button button = new Button("Verzend factuur");
        button.setOnAction(event -> facturenController.cmdSendFactuur(facturen));
    }

    private void createUpdateFactuurButton() {
        Button button = new Button("Update factuur");
        button.setOnAction(event -> facturenController.cmdUpdateStatus(facturen, "Betaald"));
        topContainer.getChildren().add(button);
    }

    private void createTableView() {
        this.table = new TableView<>();
//        this.table.setColumnResizePolicy(param -> true);
        TableColumn<Factuur, CheckBox> checkbox = new TableColumn(" ");
        checkbox.setCellValueFactory(param -> {
                       CheckBox checkBox = new CheckBox();
                        Bindings.bindBidirectional(checkBox.selectedProperty(), param.getValue().checkedProperty());
                        return new SimpleObjectProperty<>(checkBox);
        });

        TableColumn factuurnummer = new TableColumn("Factuurnummer");
        factuurnummer.setCellValueFactory(new PropertyValueFactory<Factuur, Integer>("factuurnummer"));
        TableColumn<Factuur, String> voornaam = new TableColumn("Voornaam");
        voornaam.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getDebiteur().getVoornaam()));
        TableColumn<Factuur, String> tussenvoegsel = new TableColumn("Tussenvoegsel");
        tussenvoegsel.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getDebiteur().getTussenvoegsel()));
        TableColumn<Factuur, String> achternaam = new TableColumn("Achternaam");
        achternaam.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getDebiteur().getNaam()));
        TableColumn factuurdatum = new TableColumn("Factuurdatum");
        factuurdatum.setCellValueFactory(new PropertyValueFactory<Factuur, Date>("factuurdatum"));
        TableColumn factuurexpdate = new TableColumn("Vervaldatum");
        factuurexpdate.setCellValueFactory(new PropertyValueFactory<Factuur, Date>("vervaldatum"));
        TableColumn<Factuur, Double> bedrag = new TableColumn("Bedrag");
        bedrag.setCellValueFactory(new PropertyValueFactory<Factuur, Double>(""));
        TableColumn status = new TableColumn("Status");
        status.setCellValueFactory(new PropertyValueFactory<Factuur, String>("status"));

        TableColumn<Factuur, CheckBox> checkboxBetaald = new TableColumn("Betaald");
        checkboxBetaald.setCellValueFactory(param -> {
            CheckBox checkBox = new CheckBox();
            Bindings.bindBidirectional(checkBox.selectedProperty(), param.getValue().betaaldProperty());
            return new SimpleObjectProperty<>(checkBox);
        });

        addClicklistener();
        this.table.getColumns().addAll(checkbox, factuurnummer, voornaam, tussenvoegsel, achternaam, factuurdatum, factuurexpdate, status);
        createSelectAllButton();
        setCenter(this.table);


    }

    private void addClicklistener() {
        table.setRowFactory( tv -> {
            TableRow<Factuur> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Factuur rowData = row.getItem();

                    for (int i= 1; i < facturen.size(); i++) {
                        if (rowData.getId() == facturen.get(i).getId()) {
                            System.out.println(rowData.getId() + "    " + facturen.get(i).getId());
                           String pdfFile = facturen.get(i).getPdfPath();
                            System.out.println(facturen.get(i).getPdfPath());

                            //String pdfFile = "C:\\Users\\Brandon\\Desktop\\20151025-Wijk.pdf";


                            System.out.println("pdfpath " + pdfFile);
                            if (pdfFile.toString().endsWith(".pdf")) {
                                try {
                                    Runtime.getRuntime().exec(new String[] {"rundll32", "url.dll,FileProtocolHandler",  pdfFile});
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            i++;
                        }
                    }
                }
            });
            return row ;
        });
    }

    public void createSelectAllButton() {

        CheckBox cb = new CheckBox("Select all");
        cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val) {
                if (new_val) {
                    for (Factuur factuur : facturen) {
                        factuur.checkedProperty().set(new_val);
                    }
                }
                else {
                    for (Factuur factuur : facturen) {
                        factuur.checkedProperty().set(false);
                    }
                }
            }
        });

        topContainer.getChildren().add(cb);
    }

    private void createTextField() {
        this.filterField = new TextField();
        filterField.promptTextProperty().setValue("Zoeken...");
        topContainer.getChildren().add(this.filterField);
        setAlignment(filterField, Pos.CENTER_RIGHT);
    }
    private void createTitle() {
        Text title = new Text("Facturen");
        title.getStyleClass().add("h1");
        topContainer.getChildren().add(title);
        topContainer.setAlignment(Pos.CENTER_RIGHT);
    }

    private void createAddFactuurButton() {
        Button button = new Button("Factuur toevoegen");
        button.setOnAction(event -> {
            try {
                this.facturenController.cmdShowFactuurAddView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        button.getStyleClass().addAll("btn", "btn-primary");
        topContainer.getChildren().add(button);
    }


    @Override
    public void show() {
//        this.stage.setScene(new Scene(this, 800, 600));
//        this.stage.show();
    }
}
