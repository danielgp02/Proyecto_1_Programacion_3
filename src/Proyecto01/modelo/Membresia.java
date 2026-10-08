package Proyecto01.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Clase que representa una membresía de un usuario en el gimnasio
//Tiene información sobre el plan de membresía, la fecha de vencimiento y los pagos realizados
public class Membresia {

    private Usuario usuario;
    private Plan plan;
    private LocalDate fechaVencimiento;
    private List<Pago> historialPagos;

    public Membresia(Usuario usuario, Plan plan) {
        this.usuario = usuario;
        this.plan = plan;
        this.historialPagos = new ArrayList<>();
        registrarPago();
    }

    // Getters and setters
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public List<Pago> getPagos() {
        return historialPagos;
    }

    public void setPagos(List<Pago> pagos) {
        this.historialPagos = pagos;
    }
    public void registrarPago() {
        LocalDate base = (fechaVencimiento != null && fechaVencimiento.isAfter(LocalDate.now()))
                ? fechaVencimiento : LocalDate.now();
        this.fechaVencimiento = base.plusDays(plan.getDuracionDias());
        this.historialPagos.add(new Pago(plan.calcularPrecio()));
    }

    public boolean estaAlDia() {
        return !LocalDate.now().isAfter(fechaVencimiento);
    }
}
