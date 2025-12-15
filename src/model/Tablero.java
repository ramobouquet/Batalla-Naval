package model;

public class Tablero {

    private static final int DIM = 10;
    private int[][] celdas;
    private boolean[][] disparos;
    private int barcosHundidos = 0;


    public Tablero() {
        celdas = new int[DIM][DIM];
        disparos = new boolean[DIM][DIM];

    }

    public int[][] getCeldas() {
        return celdas;
    }

    // ===================== COLOCACIÓN =====================

    public boolean sePuedeColocar(int fila, int col, int tamano, boolean horizontal) {

        if (horizontal) {
            if (col + tamano > DIM) return false;
            for (int i = 0; i < tamano; i++) {
                if (celdas[fila][col + i] != 0) return false;
            }
        } else {
            if (fila + tamano > DIM) return false;
            for (int i = 0; i < tamano; i++) {
                if (celdas[fila + i][col] != 0) return false;
            }
        }

        return true;
    }

    public boolean colocarBarco(int fila, int col, int tamano, boolean horizontal) {

        if (!sePuedeColocar(fila, col, tamano, horizontal))
            return false;

        if (horizontal) {
            for (int i = 0; i < tamano; i++) {
                celdas[fila][col + i] = tamano;
            }
        } else {
            for (int i = 0; i < tamano; i++) {
                celdas[fila + i][col] = tamano;
            }
        }

        return true;
    }

    // ===================== DISPAROS =====================

    public boolean yaDisparado(int fila, int col) {
        return celdas[fila][col] == -1 || celdas[fila][col] == -2;
    }

    public boolean disparar(int fila, int col) {
        disparos[fila][col] = true;

        if (celdas[fila][col] != 0) {
            int idBarco = celdas[fila][col];
            celdas[fila][col] = -idBarco; // marca impacto

            if (barcoHundido(idBarco)) {
                barcosHundidos++;
            }
            return true;
        }
        return false;
    }
    private boolean barcoHundido(int idBarco) {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                if (celdas[i][j] == idBarco) {
                    return false;
                }
            }
        }
        return true;
    }
    public int getBarcosHundidos() {
        return barcosHundidos;
    }




    // ===================== FIN DEL JUEGO =====================

    public boolean todosHundidos() {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                if (celdas[i][j] > 0)
                    return false;
            }
        }
        return true;
    }
}




