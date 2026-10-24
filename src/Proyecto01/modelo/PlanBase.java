package Proyecto01.modelo;

public abstract class PlanBase implements Plan {
    protected String nombre;
    protected double precioBase;
    protected String beneficios;
    protected int duracionDias;
    protected int idPlan;

    public PlanBase(String nombre, double precioBase, String beneficios, int duracionDias) {
        this.nombre = nombre;
        this.precioBase = precioBase;
        this.beneficios = beneficios;
        this.duracionDias = duracionDias;
        this.idPlan = 0;
    }

    @Override
    public String getNombre() { return nombre; }
    @Override
    public int getDuracionDias() { return duracionDias; }
    @Override
    public String getBeneficios() { return beneficios; }

    @Override
    public int getIdPlan() { return idPlan; }

    @Override
    public void setIdPlan(int idPlan) { this.idPlan = idPlan; }

    @Override
    public String toString() {
        return nombre + " (₡" + calcularPrecio() + ")";
    }
}


