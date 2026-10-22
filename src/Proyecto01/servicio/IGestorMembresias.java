package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Plan;
import Proyecto01.modelo.Usuario;

import java.util.List;

//Contrato para manejar las membresias y los pagos
public interface IGestorMembresias {

    //Asigna un plan a un usuario y cobra el precio del plan
    //Retorna la membresia creada
    Membresia asignarPlanYCobrar(Usuario usuario, Plan plan);

    //Registra un pago de renovacion sobre la membresia de un usuario
    void registrarPago(Usuario usuario);

    //Busca la membresia de un usuario
    //Retorna la membresia del usuario o null si no tiene membresia
    Membresia buscarMembresiaPorUsuario(Usuario usuario);


    //Elimina la memebresia asociada un socio, si existe, y cancela el plan asociado
    void eliminarMembresiaDeUsuario(Usuario usuario);

    //Lista todas las membresias registradas en el sistema
    //Retorna una lista con las membresias
    List<Membresia> listarMembresias();

    //Lista las membresias vencidas (fecha de vencimiento anterior a hoy)
    //Retorna una lista con las membresias morosas
    List<Membresia> listarMembresiasVencidas();


}
