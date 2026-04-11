package co.uceva.usuariosservice.infrastructure.security;

import lombok.Getter;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RsaEngine {

    // Getters para que el controlador pueda entregar la llave pública a Flutter
    // Variables de la llave
    @Getter
    private BigInteger n; // Parte de la llave pública y privada
    @Getter
    private BigInteger e; // Exponente público
    private BigInteger d; // Exponente privado (¡SECRETO!)

    // Constructor que genera las llaves automáticamente al instanciar
    public RsaEngine(int bitLength) {
        generateKeys(bitLength);
    }

    // Para cifrar (usando la llave pública e, n)
    public BigInteger encrypt(BigInteger message) {
        return message.modPow(e, n);
    }

    // Para descifrar (usando la llave privada d, n)
    public BigInteger decrypt(BigInteger cipherText) {
        return cipherText.modPow(d, n);
    }

    private void generateKeys(int bitLength) {
        SecureRandom random = new SecureRandom();

        // 1. Generar dos primos gigantes (p y q)
        // Dividimos bitLength entre 2 para que al multiplicarlos den el tamaño total
        BigInteger p = BigInteger.probablePrime(bitLength / 2, random);
        BigInteger q = BigInteger.probablePrime(bitLength / 2, random);

        // 2. Calcular n = p * q
        n = p.multiply(q);

        // 3. Calcular phi = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // 4. Elegir e (65537 es el estándar por su balance entre seguridad y velocidad)
        e = BigInteger.valueOf(65537);

        // Asegurarnos de que 'e' y 'phi' son coprimos (su máximo común divisor es 1)
        // Si por alguna rareza matemática no lo son, sumamos 2 a 'e' hasta que lo sean
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
            e = e.add(BigInteger.valueOf(2));
        }

        // 5. Calcular d (el inverso modular)
        d = e.modInverse(phi);

        System.out.println("Llaves RSA generadas con éxito.");
    }
}