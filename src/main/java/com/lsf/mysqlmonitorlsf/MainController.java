package com.lsf.mysqlmonitorlsf;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

public class MainController {
    @FXML
    private TextField textField_host;
    @FXML
    private TextField textField_port;
    @FXML
    private TextField textField_user;
    @FXML
    private TextField textField_password;
    @FXML
    private Button btn_logOn;
    @FXML
    private Button btn_update;
    @FXML
    private CheckBox cb_showPass;
    @FXML
    private Button testConn;
    @FXML
    private TableColumn<ResultTask, String> tableCol_sql;
    @FXML
    private TableColumn<ResultTask, String> tableCol_date;
    @FXML
    private TableColumn<ResultTask, Integer> tableCol_id;
    @FXML
    private TextField textField_filter;
    @FXML
    private TableView tableView;
    private String date;
    private String dbpass;
    private ObservableList<ResultTask> list = FXCollections.observableArrayList();
    private Connection conn;
    @FXML
    private Button btn_clear;
    @FXML
    private Label label_date;
    @FXML
    private Label label_state;
    private ChangeListener<String> changeListener;
    private int index;

    // 日志记录是否开启
    private boolean  global_general_log = false;

    public MainController() {
    }

    @FXML
    public void initialize() {
        this.initConfigComponents();
    }

    private void initConfigComponents() {
        this.testConn.setOnAction((event) -> this.conn = this.conn());
        this.btn_clear.setDisable(true);
        this.btn_update.setDisable(true);
        this.btn_logOn.setDisable(true);
        this.tableCol_id.setCellValueFactory(new PropertyValueFactory("index"));
        this.tableCol_id.setStyle("-fx-alignment: CENTER;");
        this.tableCol_sql.setCellValueFactory(new PropertyValueFactory("sql"));
        this.tableCol_date.setCellValueFactory(new PropertyValueFactory("date"));
        this.cb_showPass.setSelected(true);
        this.dbpass = ((String)this.textField_password.textProperty().get()).trim();
        this.textField_password.textProperty().addListener((observable, oldValue, newValue) -> {
            if (this.cb_showPass.isSelected()) {
                this.dbpass = newValue;
            } else {
                this.dbpass = oldValue;
            }

        });
        this.cb_showPass.setOnAction((event) -> {
            this.textField_password.setEditable(true);
            if (this.cb_showPass.isSelected()) {
                this.textField_password.setEditable(true);
                this.textField_password.setText(this.dbpass);
            } else {
                this.textField_password.setEditable(false);
                this.textField_password.setText(Util.getEcho(this.dbpass));
            }

        });
        this.btn_logOn.setOnAction((event) -> {
            if (this.conn != null) {
                this.date = Util.ftime();
                this.label_date.setText("下断时间：" + this.date);
                this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "断点成功"));

                try {
                    this.conn.prepareStatement("SET global general_log=on").executeUpdate();
                    this.conn.prepareStatement("SET GLOBAL log_output='table'").executeUpdate();
                    this.global_general_log = true;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                this.index = 0;
                this.btn_clear.setDisable(false);
                this.btn_update.setDisable(false);
            } else {
                this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "数据库未连接或连接超时"));
                this.showAlert(AlertType.ERROR, "错误", "数据库未连接或连接超时");
                this.btn_clear.setDisable(true);
                this.btn_update.setDisable(true);
            }

        });
        FilteredList<ResultTask> filteredList = new FilteredList(this.list, (p) -> true);
        this.changeListener = (observable, oldValue, newValue) -> {
            filteredList.setPredicate((resultTask) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    return resultTask.getSql().toLowerCase().contains(newValue);
                } else {
                    return true;
                }
            });
            this.tableView.setItems(filteredList);
        };
        this.btn_update.setOnAction((event) -> {
            if (this.tableView.getItems().size() > 0) {
                this.clear(this.changeListener);
            }

            this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "查询成功"));

            try {
                Statement stmt = this.conn.createStatement();
                String logSql = "select * from mysql.general_log where command_type =\"Query\" OR command_type =\"Execute\" order by event_time desc limit 2";
                logSql = "select date_format(event_time,'%Y-%m-%d %H:%i:%S') as event_date ,argument from general_log where command_type='Query' and argument not like '/* mysql-conne%%' and argument not like 'SET auto%%'and argument not like 'SET sql_mo%%'and argument not like 'select event_time,argument from%%' and event_time>'" + this.date + "'";
                ResultSet log = stmt.executeQuery(logSql);

                while(log.next()) {
                    String sql = log.getString("argument");
                    String event_time = log.getString("event_date");
                    if (!sql.equals(logSql)) {
                        ++this.index;
                        ResultTask resultTask = new ResultTask();
                        resultTask.setIndex(this.index);
                        resultTask.setDate(event_time);
                        resultTask.setSql(sql);
                        this.list.add(resultTask);
                    }
                }
            } catch (SQLException e) {
                this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "查询出错，" + e.getMessage()));
            }

            this.tableView.setItems(this.list);
        });
        this.btn_clear.setOnAction((event) -> {
            this.clear(this.changeListener);
            this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "清除成功"));
        });
        this.textField_filter.textProperty().addListener(this.changeListener);
        this.tableView.setRowFactory((param) -> {
            TableRow<ResultTask> row = new TableRow();
            row.setOnMouseClicked((event) -> {
                if (!row.isEmpty()) {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(((ResultTask)row.getItem()).getSql());
                        clipboard.setContent(content);
                    } else if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
                        row.setEditable(true);
                    } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                        Tooltip tt = new Tooltip();
                        tt.setStyle("-fx-font: normal bold 13 Langdon; -fx-base: #AE3522; -fx-text-fill: orange;");
                        tt.setText(((ResultTask)row.getItem()).getSql());
                        tt.setWrapText(true);
                        tt.setMaxWidth((double)300.0F);
                        row.setTooltip(tt);
                    }
                }

            });
            return row;
        });
    }

    private void clear(ChangeListener<String> changeListener) {
        this.index = 0;
        this.tableView.setItems(this.list);
        this.textField_filter.textProperty().removeListener(changeListener);
        this.textField_filter.clear();
        this.tableView.getItems().clear();
        this.textField_filter.textProperty().addListener(changeListener);
    }

    public Connection conn() {
        String dbHost = ((String)this.textField_host.textProperty().get()).trim();
        int dbPort = Integer.parseInt(((String)this.textField_port.textProperty().get()).trim());
        String dbUser = ((String)this.textField_user.textProperty().get()).trim();
        this.btn_clear.setDisable(true);
        this.btn_update.setDisable(true);
        this.btn_logOn.setDisable(true);
        this.clear(this.changeListener);
        Connection conn = Util.getConn(dbHost, dbPort, dbUser, this.dbpass);
        if (conn != null) {
            this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "数据库连接成功"));
            this.showAlert(AlertType.INFORMATION, "提示", "数据库连接成功");
            this.btn_logOn.setDisable(false);
        } else {
            this.label_state.setText(String.format("[%s]：%s", Util.ftime(), "数据库连接失败"));
            this.showAlert(AlertType.ERROR, "错误", "数据库连接失败");
        }

        return conn;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText((String)null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static {
        try {
            Tooltip obj = new Tooltip();
            Class<?> clazz = obj.getClass().getDeclaredClasses()[1];
            if (!clazz.getName().contains("TooltipBehavior")) {
                clazz = obj.getClass().getDeclaredClasses()[0];
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, Boolean.TYPE);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(new Duration((double)250.0F), new Duration((double)50000.0F), new Duration((double)200.0F), false);
            Field fieldBehavior = obj.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(obj, tooltipBehavior);
        } catch (Exception e) {
            System.out.println("error:" + e);
        }

    }

    // 退出程序时调用
    public void setGenerallogOff() {

        if (this.global_general_log) {
            try {
                this.conn.prepareStatement("SET global general_log=off").executeUpdate();
            } catch (SQLException e) {
                System.out.println("'SET global general_log=off' Worry!");
            }
        }
    }

    // 关闭数据库连接
    public void closeConn(){

    }
}