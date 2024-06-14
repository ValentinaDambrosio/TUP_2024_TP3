package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNoSupportedException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class CuentaServiceTest {
    @Mock
    private ClienteDao clienteDao;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CuentaDao cuentaDao;

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCuentaAlreadyExistsException() throws CuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();    
        cuenta.setNumeroCuenta(123456789);
        when(cuentaDao.find(123456789)).thenReturn(cuenta);
        Cliente cliente = new Cliente();
        cliente.setDni(18463521);
        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, cliente.getDni()));
    }

    @Test
    public void testTipoDeCuentaNoSupportedException() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(123456789);
        cuenta.setTipoCuenta(null);
        doReturn(null).when(cuentaDao).find(123456789);
        assertThrows(TipoCuentaNoSupportedException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 18463521));
    }

    @Test
    public void testClienteYaTieneCuentaDeEseTipo() throws TipoCuentaAlreadyExistsException{
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuenta.setMoneda(TipoMoneda.PESOS);
        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        cliente.addCuenta(cuenta);
        cliente.setNombre("Pepe");
        cliente.setApellido("Rino");
        cliente.setFechaNacimiento(LocalDate.of(1987,4,25));
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuenta, cliente.getDni());
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, cliente.getDni()));
    }

    @Test
    public void testCuentaSuccess() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNoSupportedException, ClienteAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3, 25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setNumeroCuenta(123456789);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        cuentaService.darDeAltaCuenta(cuenta, pepeRino.getDni());

        verify(cuentaDao, times(1)).save(cuenta);
    }
}
