module org.example.onlinemultiplayercheckers {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.xerial.sqlitejdbc;

    exports org.example.onlinemultiplayercheckers;
    exports org.example.onlinemultiplayercheckers.model;
    exports org.example.onlinemultiplayercheckers.network;
    exports org.example.onlinemultiplayercheckers.db;
    exports org.example.onlinemultiplayercheckers.ui;
}