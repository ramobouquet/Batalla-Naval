package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import model.Tablero;

public class GameController {

    @FXML private Button btnBarco4;
    @FXML private Button btnBarco3;
    @FXML private Button btnBarco2;
    @FXML private Button btnBarco1;
    @FXML private ToggleButton toggleOrientacion;

    private Tablero tableroJugador = new Tablero();

    // Cantidad máxima según reglas
    private final int MAX_4 = 1;
    private final int MAX_3 = 2;
    private final int MAX_2 = 3;
    private final int MAX_1 = 4;

    // Contadores usados
    private int usados4 = 0;
    private int usados3 = 0;
    private int usados2 = 0;
    private int usados1 = 0;

    private int barcoSeleccionado = 0;

    @FXML private GridPane gridJugador;
    @FXML private GridPane gridEnemigo;

    private static final int TAMANO_CELDA = 45;
    private static final int DIM = 10; // 10x10

    // Imágenes
    private Image imgBarco4;
    private Image imgBarco3;
    private Image imgBarco2;
    private Image imgBarco1;
    private Image imgAgua;


    @FXML
    public void initialize() {

        // ===========================
        // CARGAR IMÁGENES
        // ===========================
        imgBarco4 = new Image(getClass().getResource("/images/barco4.png").toExternalForm());
        imgBarco3 = new Image(getClass().getResource("/images/barco3.png").toExternalForm());
        imgBarco2 = new Image(getClass().getResource("/images/barco2.png").toExternalForm());
        imgBarco1 = new Image(getClass().getResource("/images/barco1.png").toExternalForm());
        imgAgua   = new Image(getClass().getResource("/images/agua.png").toExternalForm());


        // ===========================
        // SELECCIÓN DE BARCOS
        // ===========================
        btnBarco4.setOnAction(e -> {
            if (usados4 < MAX_4) barcoSeleccionado = 4;
        });

        btnBarco3.setOnAction(e -> {
            if (usados3 < MAX_3) barcoSeleccionado = 3;
        });

        btnBarco2.setOnAction(e -> {
            if (usados2 < MAX_2) barcoSeleccionado = 2;
        });

        btnBarco1.setOnAction(e -> {
            if (usados1 < MAX_1) barcoSeleccionado = 1;
        });

        toggleOrientacion.selectedProperty().addListener((obs, oldVal, newVal) ->
                toggleOrientacion.setText(newVal ? "Horizontal" : "Vertical")
        );

        generarTableroJugador();
        generarTableroEnemigo();
    }


    // ===========================
    // GENERAR TABLEROS
    // ===========================

    private void generarTableroJugador() {
        for (int fila = 0; fila < DIM; fila++) {
            for (int col = 0; col < DIM; col++) {
                ImageView iv = crearCelda();
                final int f = fila;
                final int c = col;
                iv.setOnMouseClicked(e -> colocarBarco(f, c));
                gridJugador.add(iv, col, fila);
            }
        }
    }

    private void generarTableroEnemigo() {
        for (int fila = 0; fila < DIM; fila++) {
            for (int col = 0; col < DIM; col++) {
                ImageView iv = crearCelda();
                final int f = fila;
                final int c = col;

                iv.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> manejarDisparo(f, c, iv));

                gridEnemigo.add(iv, col, fila);
            }
        }
    }


    // ===========================
    // COLOCAR BARCO
    // ===========================

    private void colocarBarco(int fila, int columna) {
        if (barcoSeleccionado == 0) {
            mostrarAlerta("Debes seleccionar un barco antes de colocarlo.");
            return;
        }

        boolean horizontal = toggleOrientacion.isSelected();
        boolean colocado = tableroJugador.colocarBarco(fila, columna, barcoSeleccionado, horizontal);

        if (!colocado) {
            mostrarAlerta("No se puede colocar aquí.");
            return;
        }

        // Contadores
        switch (barcoSeleccionado) {
            case 4 -> usados4++;
            case 3 -> usados3++;
            case 2 -> usados2++;
            case 1 -> usados1++;
        }

        // Revisar si se acabó ese tipo de barco
        controlarDisponibilidad();

        pintarTableroJugador();
    }


    private void controlarDisponibilidad() {

        if (usados4 >= MAX_4) {
            btnBarco4.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 4.\nPresiona ENTER para ubicar barcos de tamaño 3.");
            barcoSeleccionado = 0;
        }

        if (usados3 >= MAX_3) {
            btnBarco3.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 3.\nPresiona ENTER para ubicar barcos de tamaño 2.");
            barcoSeleccionado = 0;
        }

        if (usados2 >= MAX_2) {
            btnBarco2.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 2.\nPresiona ENTER para ubicar barcos de tamaño 1.");
            barcoSeleccionado = 0;
        }

        if (usados1 >= MAX_1) {
            btnBarco1.setDisable(true);
            mostrarAlerta("Todos los barcos han sido ubicados.");
            barcoSeleccionado = 0;
        }
    }


    // ===========================
    // PINTAR TABLERO
    // ===========================

    private void pintarTableroJugador() {
        int[][] celdas = tableroJugador.getCeldas();

        for (javafx.scene.Node nodo : gridJugador.getChildren()) {
            Integer fila = GridPane.getRowIndex(nodo);
            Integer col  = GridPane.getColumnIndex(nodo);

            if (fila == null || col == null) continue;

            ImageView iv = (ImageView) nodo;

            switch (celdas[fila][col]) {
                case 4 -> iv.setImage(imgBarco4);
                case 3 -> iv.setImage(imgBarco3);
                case 2 -> iv.setImage(imgBarco2);
                case 1 -> iv.setImage(imgBarco1);
                default -> iv.setImage(imgAgua);
            }
        }
    }


    // ===========================
    // CELDAS / DISPAROS
    // ===========================

    private ImageView crearCelda() {
        ImageView iv = new ImageView(imgAgua);
        iv.setFitWidth(TAMANO_CELDA);
        iv.setFitHeight(TAMANO_CELDA);
        return iv;
    }


    private void manejarDisparo(int fila, int col, ImageView iv) {
        System.out.println("Disparo en: " + fila + ", " + col);
        iv.setImage(imgBarco1); // solo prueba, cambiarás esto luego
    }


    // ===========================
    // ALERTA
    // ===========================

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Información");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

