package Panthera.Views;

import Panthera.Controllers.MailController;
import Panthera.Panthera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by Daan on 30-Sep-15.
 */
public class MailListView extends BorderPane implements Viewable {
    private final Stage stage = Panthera.getInstance().getStage();
    private final MailController mailController;

    public MailListView(MailController mailController) {
        this.mailController = mailController;
        Button button = new Button("Verstuur dankwoord");
        button.setOnAction(event -> mailController.cmdShowDankwoordView());
        setCenter(button);
    }

    @Override
    public void show() {
        this.stage.setScene(new Scene(this));
        this.stage.show();
    }
}
