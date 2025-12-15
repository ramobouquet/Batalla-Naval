package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import model.Tablero;

import java.util.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.FileWriter;
import java.io.IOException;

public class GameController {

    @FXML private Button btnBarco4;
    @FXML private Button btnBarco3;
    @FXML private Button btnBarco2;
    @FXML private Button btnBarco1;
    @FXML private ToggleButton toggleOrientacion;
    @FXML private Button btnIniciar;
    @FXML private Label lblTurno;
    @FXML private Pane capaBarcosJugador;
    @FXML private GridPane gridJugador;
    @FXML private GridPane gridEnemigo;
    @FXML
    private void iniciarJuego() {
        estado = EstadoJuego.COLOCANDO_BARCOS;
        btnIniciar.setDisable(true);
        lblTurno.setText("Coloca tus barcos");

        mostrarAlerta(
                "Coloca todos tus barcos.\n" +
                        "Cuando termines, comenzará la batalla."
        );
    }


    private String nombreJugador = "Jugador";

    private Tablero tableroJugador = new Tablero();
    private Tablero tableroEnemigo = new Tablero();

    private final int MAX_4 = 1;
    private final int MAX_3 = 2;
    private final int MAX_2 = 3;
    private final int MAX_1 = 4;

    private int usados4 = 0;
    private int usados3 = 0;
    private int usados2 = 0;
    private int usados1 = 0;

    private boolean aviso4Mostrado = false;
    private boolean aviso3Mostrado = false;
    private boolean aviso2Mostrado = false;
    private boolean aviso1Mostrado = false;

    private int barcoSeleccionado = 0;

    private static final int TAMANO_CELDA = 45;
    private static final int DIM = 10;

    private enum EstadoJuego {
        COLOCANDO_BARCOS,
        TURNO_JUGADOR,
        TURNO_ENEMIGO,
        FIN_JUEGO
    }

    private EstadoJuego estado = EstadoJuego.COLOCANDO_BARCOS;
    private List<int[]> disparosIA = new ArrayList<>();

    private Image imgBarco4;
    private Image imgBarco3;
    private Image imgBarco2;
    private Image imgBarco1;
    private Image imgAgua;
    private Image imgAguaDisparo;
    private Image imgImpacto;

    // =========================
    // INITIALIZE
    // =========================
    public void initialize() {

        pedirNombreJugador();

        mostrarAlerta(
                "Bienvenido a Batalla Naval\n\n" +
                        "1. Coloca todos tus barcos\n" +
                        "2. Luego ataca al enemigo por turnos\n" +
                        "3. El primero en hundir todos los barcos gana"
        );

        cargarImagenes();
        prepararDisparosIA();
        configurarBotones();

        lblTurno.setText("Coloca tus barcos");

        generarTableroJugador();
        generarTableroEnemigo();
        colocarBarcosEnemigo();
    }

    // =========================
    // NOMBRE DEL JUGADOR
    // =========================
    private void pedirNombreJugador() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Jugador");
        dialog.setHeaderText("Ingrese su nombre");
        dialog.setContentText("Nombre:");

        dialog.showAndWait().ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                nombreJugador = nombre;
            }
        });
    }

    // =========================
    // ARCHIVO PLANO
    // =========================
    private void guardarResultado(String resultado) {
        try (FileWriter writer = new FileWriter("resultado_batalla_naval.txt")) {
            writer.write("=== BATALLA NAVAL ===\n");
            writer.write("Jugador: " + nombreJugador + "\n");
            writer.write("Resultado: " + resultado + "\n");
            writer.write("Barcos enemigos hundidos: " + tableroEnemigo.getBarcosHundidos() + "\n");
            writer.write("Barcos del jugador hundidos: " + tableroJugador.getBarcosHundidos() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // IMÁGENES
    // =========================
    private void cargarImagenes() {
        imgBarco4 = new Image(getClass().getResource("/images/barco4.png").toExternalForm());
        imgBarco3 = new Image(getClass().getResource("/images/barco3.png").toExternalForm());
        imgBarco2 = new Image(getClass().getResource("/images/barco2.png").toExternalForm());
        imgBarco1 = new Image(getClass().getResource("/images/barco1.png").toExternalForm());
        imgAgua = new Image(getClass().getResource("/images/agua.png").toExternalForm());
        imgAguaDisparo = new Image(getClass().getResource("/images/agua_disparo.png").toExternalForm());
        imgImpacto = new Image(getClass().getResource("/images/impacto.png").toExternalForm());
    }

    private void prepararDisparosIA() {
        for (int f = 0; f < DIM; f++)
            for (int c = 0; c < DIM; c++)
                disparosIA.add(new int[]{f, c});
        Collections.shuffle(disparosIA);
    }

    // =========================
    // BOTONES
    // =========================
    private void configurarBotones() {
        btnBarco4.setOnAction(e -> seleccionarBarco(4, usados4 < MAX_4, btnBarco4));
        btnBarco3.setOnAction(e -> seleccionarBarco(3, usados3 < MAX_3, btnBarco3));
        btnBarco2.setOnAction(e -> seleccionarBarco(2, usados2 < MAX_2, btnBarco2));
        btnBarco1.setOnAction(e -> seleccionarBarco(1, usados1 < MAX_1, btnBarco1));

        toggleOrientacion.selectedProperty().addListener(
                (obs, oldVal, newVal) ->
                        toggleOrientacion.setText(newVal ? "Horizontal" : "Vertical")
        );
    }

    private void seleccionarBarco(int tamano, boolean disponible, Button boton) {
        if (!disponible) {
            mostrarAlerta("Ya no tienes barcos de ese tamaño.");
            return;
        }
        barcoSeleccionado = tamano;
        limpiarSeleccionBotones();
        boton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }



    // =========================
    // TABLEROS
    // =========================
    private void generarTableroJugador() {
        for (int f = 0; f < DIM; f++)
            for (int c = 0; c < DIM; c++) {
                ImageView iv = crearCelda();
                final int fila = f, col = c;
                iv.setOnMouseClicked(e -> colocarBarco(fila, col));
                gridJugador.add(iv, c, f);
            }
    }

    private void generarTableroEnemigo() {
        for (int f = 0; f < DIM; f++)
            for (int c = 0; c < DIM; c++) {
                ImageView iv = crearCelda();
                final int fila = f, col = c;
                iv.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        e -> manejarDisparo(fila, col, iv));
                gridEnemigo.add(iv, c, f);
            }
    }

    private ImageView crearCelda() {
        ImageView iv = new ImageView(imgAgua);
        iv.setFitWidth(TAMANO_CELDA);
        iv.setFitHeight(TAMANO_CELDA);
        return iv;
    }

    // =========================
    // COLOCAR BARCOS
    // =========================
    private void colocarBarco(int fila, int col) {
        if (estado != EstadoJuego.COLOCANDO_BARCOS || barcoSeleccionado == 0) return;

        boolean horizontal = toggleOrientacion.isSelected();
        if (!tableroJugador.colocarBarco(fila, col, barcoSeleccionado, horizontal)) {
            mostrarAlerta("No se puede colocar aquí.");
            return;
        }

        Image imagen = switch (barcoSeleccionado) {
            case 4 -> imgBarco4;
            case 3 -> imgBarco3;
            case 2 -> imgBarco2;
            default -> imgBarco1;
        };

        ImageView barco = new ImageView(imagen);
        barco.setLayoutX(col * TAMANO_CELDA + 10);
        barco.setLayoutY(fila * TAMANO_CELDA + 10);
        barco.setFitWidth(horizontal ? TAMANO_CELDA * barcoSeleccionado : TAMANO_CELDA);
        barco.setFitHeight(horizontal ? TAMANO_CELDA : TAMANO_CELDA * barcoSeleccionado);
        barco.setMouseTransparent(true);
        capaBarcosJugador.getChildren().add(barco);

        switch (barcoSeleccionado) {
            case 4 -> usados4++;
            case 3 -> usados3++;
            case 2 -> usados2++;
            case 1 -> usados1++;
        }

        barcoSeleccionado = 0;
        limpiarSeleccionBotones();

        controlarDisponibilidad();
    }

    private void controlarDisponibilidad() {

        if (usados4 >= MAX_4) {
            btnBarco4.setDisable(true);
        }

        if (usados3 >= MAX_3) {
            btnBarco3.setDisable(true);
        }

        if (usados2 >= MAX_2) {
            btnBarco2.setDisable(true);
        }

        if (usados1 >= MAX_1) {
            btnBarco1.setDisable(true);
        }

        if (usados4 == MAX_4 &&
                usados3 == MAX_3 &&
                usados2 == MAX_2 &&
                usados1 == MAX_1) {

            mostrarAlerta(
                    "Todos los barcos han sido colocados.\n" +
                            "¡Comienza la batalla!"
            );

            estado = EstadoJuego.TURNO_JUGADOR;
            lblTurno.setText("Tu turno");
        }
    }


    // =========================
    // ENEMIGO
    // =========================
    private void colocarBarcosEnemigo() {
        colocarAleatorio(4, 1);
        colocarAleatorio(3, 2);
        colocarAleatorio(2, 3);
        colocarAleatorio(1, 4);
    }

    private void colocarAleatorio(int tamano, int cantidad) {
        int colocados = 0;
        while (colocados < cantidad) {
            int f = (int)(Math.random() * DIM);
            int c = (int)(Math.random() * DIM);
            boolean h = Math.random() < 0.5;
            if (tableroEnemigo.colocarBarco(f, c, tamano, h))
                colocados++;
        }
    }

    private void manejarDisparo(int fila, int col, ImageView celda) {
        if (estado != EstadoJuego.TURNO_JUGADOR || tableroEnemigo.yaDisparado(fila, col)) return;

        boolean impacto = tableroEnemigo.disparar(fila, col);
        celda.setImage(impacto ? imgImpacto : imgAguaDisparo);

        if (tableroEnemigo.todosHundidos()) {
            finalizarJuego(true);
            return;
        }

        estado = EstadoJuego.TURNO_ENEMIGO;
        PauseTransition p = new PauseTransition(Duration.seconds(1));
        p.setOnFinished(e -> turnoEnemigo());
        p.play();
    }

    private void turnoEnemigo() {
        if (disparosIA.isEmpty()) return;

        int[] d = disparosIA.remove(0);
        boolean impacto = tableroJugador.disparar(d[0], d[1]);

        ImageView marca = new ImageView(impacto ? imgImpacto : imgAguaDisparo);
        marca.setFitWidth(TAMANO_CELDA);
        marca.setFitHeight(TAMANO_CELDA);
        marca.setLayoutX(d[1] * TAMANO_CELDA + 10);
        marca.setLayoutY(d[0] * TAMANO_CELDA + 10);
        capaBarcosJugador.getChildren().add(marca);

        if (tableroJugador.todosHundidos()) {
            finalizarJuego(false);
            return;
        }

        estado = EstadoJuego.TURNO_JUGADOR;
        lblTurno.setText("Tu turno");
    }

    // =========================
    // FIN DEL JUEGO
    // =========================
    private void finalizarJuego(boolean ganoJugador) {
        estado = EstadoJuego.FIN_JUEGO;
        guardarResultado(ganoJugador ? "GANÓ" : "PERDIÓ");
        mostrarAlerta(ganoJugador
                ? "¡Ganaste " + nombreJugador + "!"
                : "Has perdido 😢");
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarSeleccionBotones() {
        btnBarco4.setStyle("");
        btnBarco3.setStyle("");
        btnBarco2.setStyle("");
        btnBarco1.setStyle("");
    }
}
