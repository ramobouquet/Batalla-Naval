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
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javafx.scene.Node;


public class GameController {

    @FXML private Button btnBarco4;
    @FXML private Button btnBarco3;
    @FXML private Button btnBarco2;
    @FXML private Button btnBarco1;
    @FXML private ToggleButton toggleOrientacion;

    private Tablero tableroJugador = new Tablero();
    private Tablero tableroEnemigo = new Tablero();


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
    private boolean aviso4Mostrado = false;
    private boolean aviso3Mostrado = false;
    private boolean aviso2Mostrado = false;
    private boolean aviso1Mostrado = false;
    private int barcoSeleccionado = 0;
    @FXML private GridPane gridJugador;
    @FXML private GridPane gridEnemigo;
    private static final int TAMANO_CELDA = 45;
    private static final int DIM = 10; // 10x10
    private enum EstadoJuego {
        COLOCANDO_BARCOS,
        TURNO_JUGADOR,
        TURNO_ENEMIGO,
        FIN_JUEGO
    }

    private EstadoJuego estado = EstadoJuego.COLOCANDO_BARCOS;
    private List<int[]> disparosIA = new ArrayList<>();

    // Imágenes
    private Image imgBarco4;
    private Image imgBarco3;
    private Image imgBarco2;
    private Image imgBarco1;
    private Image imgAgua;
    private Image imgAguaDisparo;
    private Image imgImpacto;


    public void initialize() {

        mostrarAlerta(
                "Bienvenido a Batalla Naval\n\n" +
                        "1. Coloca todos tus barcos\n" +
                        "2. Luego ataca al enemigo por turnos\n" +
                        "3. El primero en hundir todos los barcos gana"
        );
        imgBarco4 = new Image(getClass().getResource("/images/barco4.png").toExternalForm());
        imgBarco3 = new Image(getClass().getResource("/images/barco3.png").toExternalForm());
        imgBarco2 = new Image(getClass().getResource("/images/barco2.png").toExternalForm());
        imgBarco1 = new Image(getClass().getResource("/images/barco1.png").toExternalForm());
        imgAgua   = new Image(getClass().getResource("/images/agua.png").toExternalForm());
        imgAguaDisparo = new Image(getClass().getResource("/images/agua_disparo.png").toExternalForm());
        imgImpacto     = new Image(getClass().getResource("/images/impacto.png").toExternalForm());
        for (int f = 0; f < DIM; f++) {
            for (int c = 0; c < DIM; c++) {
                disparosIA.add(new int[]{f, c});
            }
        }
        Collections.shuffle(disparosIA);

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
        colocarBarcosEnemigo();

    }



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

        if (usados4 >= MAX_4 && !aviso4Mostrado) {
            btnBarco4.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 4.\nAhora coloca barcos de tamaño 3.");
            aviso4Mostrado = true;
            barcoSeleccionado = 0;
        }

        if (usados3 >= MAX_3 && !aviso3Mostrado) {
            btnBarco3.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 3.\nAhora coloca barcos de tamaño 2.");
            aviso3Mostrado = true;
            barcoSeleccionado = 0;
        }

        if (usados2 >= MAX_2 && !aviso2Mostrado) {
            btnBarco2.setDisable(true);
            mostrarAlerta("No tienes más barcos de tamaño 2.\nAhora coloca barcos de tamaño 1.");
            aviso2Mostrado = true;
            barcoSeleccionado = 0;
        }

        if (usados1 >= MAX_1 && !aviso1Mostrado) {
            btnBarco1.setDisable(true);
            mostrarAlerta(
                    "Todos los barcos han sido ubicados.\n" +
                            "¡Comienza la batalla!"
            );

            aviso1Mostrado = true;
            barcoSeleccionado = 0;

            estado = EstadoJuego.TURNO_JUGADOR;
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

    private void colocarBarcosEnemigo() {
        colocarAleatorio(4, 1);
        colocarAleatorio(3, 2);
        colocarAleatorio(2, 3);
        colocarAleatorio(1, 4);
    }

    private void colocarAleatorio(int tamano, int cantidad) {
        int colocados = 0;

        while (colocados < cantidad) {
            int fila = (int)(Math.random() * 10);
            int col = (int)(Math.random() * 10);
            boolean horizontal = Math.random() < 0.5;

            if (tableroEnemigo.colocarBarco(fila, col, tamano, horizontal)) {
                colocados++;
            }
        }
    }


    private void manejarDisparo(int fila, int col, ImageView celda) {

        if (estado != EstadoJuego.TURNO_JUGADOR) {
            return; // No es tu turno
        }

        if (tableroEnemigo.yaDisparado(fila, col)) {
            return;
        }

        boolean impacto = tableroEnemigo.disparar(fila, col);

        if (impacto) {
            celda.setImage(imgImpacto);
        } else {
            celda.setImage(imgAguaDisparo);
        }

        if (tableroEnemigo.todosHundidos()) {
            estado = EstadoJuego.FIN_JUEGO;
            mostrarAlerta("¡Ganaste! Hundiste todos los barcos enemigos 🚢🔥");
            return;
        }

        // Cambiar turno
        estado = EstadoJuego.TURNO_ENEMIGO;
        turnoEnemigo();

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

    private void turnoEnemigo() {

        if (disparosIA.isEmpty()) {
            estado = EstadoJuego.TURNO_JUGADOR;
            return;
        }

        int[] disparo = disparosIA.remove(0);
        int fila = disparo[0];
        int col  = disparo[1];

        boolean impacto = tableroJugador.disparar(fila, col);

        for (Node nodo : gridJugador.getChildren()) {
            Integer f = GridPane.getRowIndex(nodo);
            Integer c = GridPane.getColumnIndex(nodo);

            if (f == fila && c == col) {
                ImageView iv = (ImageView) nodo;
                iv.setImage(impacto ? imgImpacto : imgAguaDisparo);
                break;
            }
        }

        if (tableroJugador.todosHundidos()) {
            estado = EstadoJuego.FIN_JUEGO;
            mostrarAlerta("Has perdido 😢\nTodos tus barcos fueron hundidos.");
            return;
        }

        estado = EstadoJuego.TURNO_JUGADOR;
    }



}

