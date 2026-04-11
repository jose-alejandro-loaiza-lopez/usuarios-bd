package co.uceva.usuariosservice.infrastructure.security;

public class AesWrapper {

    private final AesEngine engine;

    public AesWrapper() {
        this.engine = new AesEngine(); // Instanciamos tu motor recién creado
    }

    // --- 1. LÓGICA DE PADDING (PKCS#7) ---

    private byte[] addPadding(byte[] input) {
        int paddingLength = 16 - (input.length % 16);
        byte[] padded = new byte[input.length + paddingLength];
        System.arraycopy(input, 0, padded, 0, input.length);

        // El valor del byte de relleno es igual a la cantidad de bytes que faltan
        for (int i = input.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }
        return padded;
    }

    private byte[] removePadding(byte[] input) {
        int paddingLength = input[input.length - 1] & 0xFF;
        byte[] unpadded = new byte[input.length - paddingLength];
        System.arraycopy(input, 0, unpadded, 0, unpadded.length);
        return unpadded;
    }

    // --- 2. MODO CBC: CIFRADO ---

    public byte[] encryptCBC(byte[] plaintext, byte[] key, byte[] iv) {
        byte[] padded = addPadding(plaintext);
        byte[] ciphertext = new byte[padded.length];
        byte[] currentIv = iv.clone(); // Clonamos para no modificar el original
        byte[] block = new byte[16];

        for (int i = 0; i < padded.length; i += 16) {
            // 1. Extraer el bloque de 16 bytes
            System.arraycopy(padded, i, block, 0, 16);

            // 2. Operación XOR con el IV (o el bloque cifrado anterior)
            for (int j = 0; j < 16; j++) {
                block[j] ^= currentIv[j];
            }

            // 3. Cifrar con tu motor AES
            byte[] encryptedBlock = engine.encrypt(block, key);

            // 4. Guardar en el arreglo final
            System.arraycopy(encryptedBlock, 0, ciphertext, i, 16);

            // 5. El bloque cifrado se convierte en el IV del siguiente ciclo
            currentIv = encryptedBlock.clone();
        }
        return ciphertext;
    }

    // --- 3. MODO CBC: DESCIFRADO ---

    public byte[] decryptCBC(byte[] ciphertext, byte[] key, byte[] iv) {
        byte[] paddedPlaintext = new byte[ciphertext.length];
        byte[] currentIv = iv.clone();
        byte[] block = new byte[16];

        for (int i = 0; i < ciphertext.length; i += 16) {
            System.arraycopy(ciphertext, i, block, 0, 16);

            // 1. Descifrar con tu motor AES
            byte[] decryptedBlock = engine.decrypt(block, key);

            // 2. Operación XOR con el IV (o el bloque cifrado anterior)
            for (int j = 0; j < 16; j++) {
                decryptedBlock[j] ^= currentIv[j];
            }

            // 3. Guardar el texto plano con padding
            System.arraycopy(decryptedBlock, 0, paddedPlaintext, i, 16);

            // 4. El IV para el siguiente bloque es el bloque CIFRADO actual
            currentIv = block.clone();
        }
        return removePadding(paddedPlaintext);
    }
}