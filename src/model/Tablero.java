package model;

public class Tablero {

    private final int DIM = 10;
    private int[][] celdas;

    public Tablero() {
        celdas = new int[DIM][DIM];
    }

    public int[][] getCeldas() {
        return celdas;
    }

    /**
     * Verifica si un barco de cierto tamaño puede colocarse sin salirse ni chocar con otros
     */
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

    /**
     * Coloca un barco en las celdas del tablero usando el "tamaño" como ID
     * 1 = barco tamaño 1
     * 2 = barco tamaño 2
     * 3 = barco tamaño 3
     * 4 = barco tamaño 4
     */
    public boolean colocarBarco(int fila, int col, int tamano, boolean horizontal) {

        if (!sePuedeColocar(fila, col, tamano, horizontal))
            return false;

        if (horizontal) {
            for (int i = 0; i < tamano; i++) {
                celdas[fila][col + i] = tamano;  // identificador del barco
            }
        } else {
            for (int i = 0; i < tamano; i++) {
                celdas[fila + i][col] = tamano;  // identificador del barco
            }
        }

        return true;
    }
}



