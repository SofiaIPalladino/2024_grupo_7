package test;

import modeloDatos.*;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import persistencia.IPersistencia;
import persistencia.PersistenciaBIN;
import persistencia.EmpresaDTO;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class TestPersistencia {
    private IPersistencia persistencia;
    private EmpresaDTO empresa;

    @Before
    public void setUp() {
        persistencia = new PersistenciaBIN();
        empresa = new EmpresaDTO();
        llenaEmpresa(empresa); // Inicializa los datos de prueba
    }

    @After
    public void tearDown() {
        File archivo = new File("empresaPrueba.bin");
        if (archivo.exists()) {
            archivo.delete(); // Elimina el archivo de prueba después de cada test
        }
    }

    @Test
    public void testCrearArchivo() {
        try {
            persistencia.abrirOutput("empresaPrueba.bin");
            File archivo = new File("empresaPrueba.bin");
            Assert.assertTrue("Debería existir el archivo de la empresa", archivo.exists());
            persistencia.cerrarOutput();
        } catch (IOException e) {
            Assert.fail("No debería fallar: " + e.getMessage());
        }
    }

    @Test
    public void testEscrituraEmpresaVacia() {
        try {
            persistencia.abrirOutput("empresaPrueba.bin");
            EmpresaDTO empresaVacia = new EmpresaDTO();
            persistencia.escribir(empresaVacia);
            persistencia.cerrarOutput();
        } catch (IOException e) {
            Assert.fail("Error en la escritura vacía: " + e.getMessage());
        }
    }

    @Test
    public void testEscrituraConEmpresa() {
        try {
            persistencia.abrirOutput("empresaPrueba.bin");
            persistencia.escribir(empresa);
            persistencia.cerrarOutput();
        } catch (IOException e) {
            Assert.fail("Error en la escritura de la empresa: " + e.getMessage());
        }
    }

    @Test
    public void despersistirSinArchivo() {
        try {
            persistencia.abrirInput("archivoInexistente.bin");
            persistencia.leer(); // Intento de lectura
            Assert.fail("Debería dar error ya que no existe ese archivo");
        } catch (IOException e) {
            Assert.assertTrue("Debería lanzarse una FileNotFoundException", e instanceof java.io.FileNotFoundException);
        } catch (ClassNotFoundException e) {
            Assert.fail("Error inesperado: " + e.getMessage());
            Assert.fail("No debería fallar: " + e.getMessage());
        }
    }

    @Test
    public void despersistirConArchivo() {
        Pedido pedidoLeido = null;
        try {
            persistencia.abrirOutput("empresaPrueba.bin");
            persistencia.escribir(empresa);
            persistencia.cerrarOutput();

            persistencia.abrirInput("empresaPrueba.bin");
            EmpresaDTO empresaLeida = (EmpresaDTO) persistencia.leer();
            persistencia.abrirInput("pedidos.bin");
            pedidoLeido = (Pedido) persistencia.leer();
            persistencia.cerrarInput();

            Assert.assertNotNull("La empresa leída no debería ser nula", empresaLeida);

            Assert.assertEquals("La cantidad de clientes no coincide", this.empresa.getClientes().size(), empresaLeida.getClientes().size());
            for (String nombreUsuario : this.empresa.getClientes().keySet()) {
                Cliente clienteOriginal = this.empresa.getClientes().get(nombreUsuario);
                Cliente clienteLeido = empresaLeida.getClientes().get(nombreUsuario);
                Assert.assertNotNull("El cliente leído no debería ser nulo", clienteLeido);
                Assert.assertEquals("El nombre de usuario de cliente no coincide", clienteOriginal.getNombreUsuario(),clienteLeido.getNombreUsuario());
                Assert.assertEquals("El nombre de cliente no coincide", clienteOriginal.getNombreReal(),clienteLeido.getNombreReal());
                Assert.assertEquals("La contrasenia de cliente no coincide", clienteOriginal.getPass(),clienteLeido.getPass());
            }

            Assert.assertEquals("La cantidad de vehiculos no coincide", this.empresa.getVehiculos().size(), empresaLeida.getVehiculos().size());
            for(String patente : this.empresa.getVehiculos().keySet()){
                Vehiculo vehiculoOriginal = this.empresa.getVehiculos().get(patente);
                Vehiculo vehiculoLeido = empresaLeida.getVehiculos().get(patente);
                Assert.assertEquals("La patente no coincide", vehiculoOriginal.getPatente(),vehiculoLeido.getPatente());
                Assert.assertEquals("La cantidad de plazas no coincide", vehiculoOriginal.getCantidadPlazas(),vehiculoLeido.getCantidadPlazas());
            }

            Assert.assertEquals("La cantidad de choferes no coincide", this.empresa.getChoferes().size(), empresaLeida.getChoferes().size());
            for(String dni : this.empresa.getChoferes().keySet()){
                Chofer choferOriginal = this.empresa.getChoferes().get(dni);
                Chofer choferLeido = empresaLeida.getChoferes().get(dni);
                Assert.assertNotNull("El chofer leído no debería ser nulo", choferLeido);
                Assert.assertEquals("El DNI del chofer no coincide", choferOriginal.getDni(),choferLeido.getDni());
                Assert.assertEquals("El nombre del chofer no coincide", choferOriginal.getNombre(),choferLeido.getNombre());
                Assert.assertEquals("El sueldo bruto del chofer no coincide", choferOriginal.getSueldoBruto(),choferLeido.getSueldoBruto(),0.001);
                Assert.assertEquals("El sueldo neto del chofer no coincide", choferOriginal.getSueldoNeto(),choferLeido.getSueldoNeto(),0.001);
            }

        } catch (IOException | ClassNotFoundException e) {
            Assert.fail("No debería dar error ya que el archivo existe: " + e.getMessage());
            e.printStackTrace();
        }
        assertNotNull("El pedido leído no debería ser nulo", pedidoLeido);
    }


    public void llenaEmpresa(EmpresaDTO empresa) {
        Cliente cliente1 = new Cliente("Sofia123", "123456", "Sofia");
        empresa.getClientes().put(cliente1.getNombreUsuario(), cliente1);
        System.out.println("Cliente agregado: " + cliente1.getNombreUsuario());

        Chofer chofer1 = new ChoferTemporario("87654321", "Carlos P");
        empresa.getChoferes().put(chofer1.getDni(), chofer1);
        System.out.println("Chofer agregado: " + chofer1.getDni());

        Vehiculo vehiculo1 = new Auto("ABC123", 4, true);
        empresa.getVehiculos().put(vehiculo1.getPatente(), vehiculo1);
        System.out.println("Vehículo agregado: " + vehiculo1.getPatente());

        empresa.getChoferesDesocupados().add(chofer1);
        empresa.getVehiculosDesocupados().add(vehiculo1);
        System.out.println("Chofer y vehículo agregados a la lista desocupados.");

        Pedido pedido1 = new Pedido(cliente1, 3, true, false, 10, "ZONA_PELIGROSA");
        empresa.getPedidos().put(cliente1, pedido1); // Asegura que el pedido se almacene
        System.out.println("Pedido agregado para cliente: " + cliente1.getNombreUsuario());

        Viaje viaje1 = new Viaje(pedido1, chofer1, vehiculo1);
        empresa.getViajesIniciados().put(cliente1, viaje1);
        System.out.println("Viaje iniciado para cliente: " + cliente1.getNombreUsuario());

        empresa.getViajesTerminados().add(viaje1);
        System.out.println("Viaje terminado agregado.");
    }

}