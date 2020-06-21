package com.programmer74.katolk.client;

import com.programmer74.katolk.client.gui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {
  public static void main(String[] args) {
    launch(args);
  }
  //http://paletton.com/#uid=52+0u0kllllaFw0g0qFqFg0w0aF
  @Override
  public void start(Stage stage) throws Exception {
    String fxmlFile = "/fxml/login.fxml";
    FXMLLoader loader = new FXMLLoader();
    Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
    stage.setTitle("Katolk Client");
    stage.setScene(new Scene(root));
    stage.show();
    LoginController controller = loader.getController();
    controller.setStage(stage);
    controller.performPostConstruct();
  }
}
