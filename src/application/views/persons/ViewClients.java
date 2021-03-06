package application.views.persons;

import application.models.ModelPerson;
import application.models.ModelTransaction;
import application.views.View;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.function.Predicate;

public class ViewClients extends View {

    private TableView<ModelPerson> tblClients = new TableView<>();
    private TextField txtName = new TextField();
    private ComboBox<String> cbxType = new ComboBox<>(FXCollections.observableArrayList("pravno", "fizičko"));
    private Button btnInsert = new Button("Unos");

    public ViewClients(Group group) {
        super(group);
    }

    public TableView<ModelPerson> getTblClients() {
        return tblClients;
    }

    public TextField getTxtName() {
        return txtName;
    }

    public ComboBox<String> getCbxType() {
        return cbxType;
    }

    public Button getBtnInsert() {
        return btnInsert;
    }

    @Override
    public void load() {
        BorderPane borderPane = new BorderPane();

        TableColumn<ModelPerson, String> tableColumnName = new TableColumn<>("Naziv");
        tableColumnName.setMinWidth(200);
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnName.setResizable(false);

        TableColumn<ModelPerson, ModelPerson.Type> tableColumnType = new TableColumn<>("Lice");
        tableColumnType.setMinWidth(100);
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnType.setCellFactory(tc -> new TableCell<ModelPerson, ModelPerson.Type>() {
            @Override
            protected void updateItem(ModelPerson.Type item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    if (item.toString().contains("LEGAL")) {
                        setText("pravno");
                    }
                    else if (item.toString().contains("NATURAL")) {
                        setText("fizičko");
                    }
                    else {
                        setText(null);
                    }
                }
            }
        });
        tableColumnType.setResizable(false);

        TableColumn<ModelPerson, Double> tableColumnIncome = new TableColumn<>("Prihodi");
        tableColumnIncome.setMinWidth(100);
        tableColumnIncome.setCellFactory(tc -> new TableCell<ModelPerson, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    Double amount = 0.0;
                    for (ModelTransaction mt : ModelTransaction.getTransactions()) {
                        if (mt.getType() == ModelTransaction.Type.CLAIM
                                && mt.getPerson() == getTableRow().getItem()) {
                            amount += mt.getAmount();
                        }
                    }
                    setText(amount.toString());
                }
            }
        });
        tableColumnIncome.setResizable(false);

        TableColumn<ModelPerson, Double> tableColumnPayments = new TableColumn<>("Uplate");
        tableColumnPayments.setMinWidth(100);
        tableColumnPayments.setCellFactory(tc -> new TableCell<ModelPerson, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    Double amount = 0.0;
                    for (ModelTransaction mt : ModelTransaction.getTransactions()) {
                        if ((mt.getType() == ModelTransaction.Type.CASH_PAYMENT ||
                                mt.getType() == ModelTransaction.Type.CASHLESS_PAYMENT) &&
                                mt.getPerson() == getTableRow().getItem()) {
                            amount += mt.getAmount();
                        }
                    }
                    setText(amount.toString());
                }
            }
        });
        tableColumnPayments.setResizable(false);

        ObjectProperty<Predicate<ModelPerson>> clientFilter = new SimpleObjectProperty<>();
        clientFilter.bind(Bindings.createObjectBinding(() ->
                person -> person.getType() == ModelPerson.Type.LEGAL_CLIENT ||
                    person.getType() == ModelPerson.Type.NATURAL_CLIENT));
        FilteredList<ModelPerson> clients = new FilteredList<>(ModelPerson.getPersons());
        clients.predicateProperty().bind(Bindings.createObjectBinding(clientFilter::get));
        tblClients.setItems(clients);

        tblClients.getColumns().addAll(tableColumnName, tableColumnType, tableColumnIncome, tableColumnPayments);

        tblClients.widthProperty().addListener((source, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblClients.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((observable, oldValue, newValue) -> header.setReordering(false));
        });

        borderPane.setCenter(tblClients);

        HBox inputs = new HBox();

        txtName.setPrefWidth(200);
        cbxType.setPrefWidth(100);
        inputs.getChildren().addAll(txtName, cbxType, btnInsert);

        borderPane.setBottom(inputs);

        group.getChildren().add(borderPane);

    }

}
