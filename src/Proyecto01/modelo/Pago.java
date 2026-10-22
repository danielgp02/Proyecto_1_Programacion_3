package Proyecto01.modelo;

import java.time.LocalDate;
//Pago individual sobre una membresia
public class Pago {

    private LocalDate fecha;
    private double monto;

    //Registra pago con la fecha actual del sistema
    public Pago(double monto){
        this.fecha = LocalDate.now();
        this.monto = monto;
    }

    //Obtiene la fecha en la que se realizo el pago y la retorna
    public LocalDate getFecha(){
        return fecha;
    }

    //Establece la fecha del pago (útil al cargar desde la base de datos)
    //@param fecha fecha del pago
    public void setFecha(LocalDate fecha){
        this.fecha = fecha;
    }

    //Obtiene el monto del pago y lo retorna
    public double getMonto(){
        return monto;
    }

    @Override
    public String toString() {
        return fecha + " - ₡" + monto;
    }

}
