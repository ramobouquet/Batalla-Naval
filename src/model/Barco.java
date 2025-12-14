package model;

public class Barco {
    private int tamaño;
    private boolean colocado;

    public Barco(int tamaño) {
        this.tamaño = tamaño;
        this.colocado = false;
    }

    public int getTamaño() {
        return tamaño;
    }

    public boolean estaColocado() {
        return colocado;
    }

    public void setColocado(boolean colocado) {
        this.colocado = colocado;
    }
}
