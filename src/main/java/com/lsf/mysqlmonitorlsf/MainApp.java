package com.lsf.mysqlmonitorlsf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private MainController controller;
    public MainApp() {
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("main.fxml"));
        AnchorPane rootLayout = loader.load();
        this.controller = loader.getController();
        Scene scene = new Scene(rootLayout, (double)1000.0F, (double)600.0F);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MySQL监控工具");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest((event) -> {
            this.controller.setGenerallogOff();
            this.controller.closeConn();
            System.exit(0);
        }
        );
        primaryStage.show();
        primaryStage.centerOnScreen();
    }
}