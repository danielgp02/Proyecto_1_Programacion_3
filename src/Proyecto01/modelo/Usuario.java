package Proyecto01.modelo;

public class Usuario extends Persona {

    protected int numeroUsuario;
    protected int idUsuario;

    // Indica si el socio tiene su pago al día (true) o presenta morosidad (false)
    protected boolean pagoAlDia;

    // Nombre y teléfono de la persona de contacto ante una emergencia del socio
    protected String contactoEmergencia;

    // Condiciones médicas relevantes del socio (ej. asma, diabetes, alergias)
    protected String condicionesMedicas;

    public Usuario() {
    }

    public Usuario(String nombreCompleto, int edad, String correoElectronico, int telefono, int numeroSocio, int idUsuario) {
        super(nombreCompleto, edad, correoElectronico, telefono);
        this.numeroUsuario = numeroSocio;
        this.idUsuario = idUsuario;
    }

    public Usuario(String nombreCompleto, int edad, String correoElectronico, int telefono, int numeroSocio, int idUsuario, boolean pagoAlDia) {
        super(nombreCompleto, edad, correoElectronico, telefono);
        this.numeroUsuario = numeroSocio;
        this.idUsuario = idUsuario;
        this.pagoAlDia = pagoAlDia;
    }

    // Constructor completo: incluye contacto de emergencia y condiciones médicas
    public Usuario(String nombreCompleto, int edad, String correoElectronico, int telefono,
                   int numeroUsuario, int idUsuario, boolean pagoAlDia,
                   String contactoEmergencia, String condicionesMedicas) {
        super(nombreCompleto, edad, correoElectronico, telefono);
        this.numeroUsuario = numeroUsuario;
        this.idUsuario = idUsuario;
        this.pagoAlDia = pagoAlDia;
        this.contactoEmergencia = contactoEmergencia;
        this.condicionesMedicas = condicionesMedicas;
    }

    public void setNumeroSocio(int numeroSocio) {
        this.numeroUsuario = numeroSocio;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getNumeroUsuario() {

        return numeroUsuario;

    }

    public boolean isPagoAlDia() {
        return pagoAlDia;
    }

    public void setPagoAlDia(boolean pagoAlDia) {
        this.pagoAlDia = pagoAlDia;
    }

    // Retorna el nombre y teléfono del contacto de emergencia del socio
    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
    }

    // Retorna las condiciones médicas reportadas por el socio
    public String getCondicionesMedicas() {
        return condicionesMedicas;
    }

    public void setCondicionesMedicas(String condicionesMedicas) {
        this.condicionesMedicas = condicionesMedicas;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreCompleto='" + nombreCompleto + '\'' +
                ", numeroSocio=" + numeroUsuario +
                ", edad=" + edad +
                ", idUsuario=" + idUsuario +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", telefono=" + telefono +
                ", pagoAlDia=" + pagoAlDia +
                ", contactoEmergencia='" + contactoEmergencia + '\'' +
                ", condicionesMedicas='" + condicionesMedicas + '\'' +
                '}';
    }

    /**
     * Dos socios son el mismo si comparten número de socio.
     * Sin esto, editar creaba otro objeto y buscarMembresiaPorUsuario devolvía null.
     *
     * @param o otro objeto
     * @return true si es un Usuario con el mismo número de socio
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario)) {
            return false;
        }
        Usuario otro = (Usuario) o;
        return numeroUsuario == otro.numeroUsuario;
    }

    /**
     * @return código hash basado en el número de socio, coherente con equals
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(numeroUsuario);
    }
}