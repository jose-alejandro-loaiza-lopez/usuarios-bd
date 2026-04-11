package co.uceva.usuariosservice.infrastructure.security;

public class AesEngine {

    // Tabla S-Box estándar para AES (Rijndael)
    private static final int[] SBOX = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };

    private static final int[] INV_SBOX = {
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
            0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
            0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
            0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
            0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
            0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
            0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
            0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
            0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
            0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
            0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
            0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
            0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
            0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
            0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    };

    // La matriz de estado (4x4 bytes)
    private byte[][] state = new byte[4][4];

    // Método principal que llamaremos después
    public byte[] encrypt(byte[] input, byte[] key) {
        // 1. Generar todas las llaves de ronda a partir de la llave original
        keyExpansion(key);

        // 2. Cargar los 16 bytes de entrada en la matriz de estado (4x4)
        initState(input);

        // --- RONDA INICIAL ---
        // Simplemente mezclamos el mensaje original con la primera llave
        addRoundKey(0);

        // --- RONDAS INTERMEDIAS (1 a 9) ---
        for (int round = 1; round <= 9; round++) {
            subBytes();      // Confusión
            shiftRows();     // Difusión horizontal
            mixColumns();    // Difusión vertical
            addRoundKey(round); // Mezcla con la llave de esta ronda
        }

        // --- RONDA FINAL (Ronda 10) ---
        // IMPORTANTE: En la última ronda NO se ejecuta MixColumns
        subBytes();
        shiftRows();
        addRoundKey(10);

        // 3. Extraer los datos de la matriz y devolverlos como un arreglo de bytes
        return getStateAsByteArray();
    }

    public byte[] decrypt(byte[] cipherText, byte[] key) {
        keyExpansion(key);
        initState(cipherText);

        // Ronda inicial de descifrado (Ronda 10)
        addRoundKey(10);
        invShiftRows();
        invSubBytes();

        // Rondas 9 a 1
        for (int round = 9; round >= 1; round--) {
            addRoundKey(round);
            invMixColumns();
            invShiftRows();
            invSubBytes();
        }

        // Ronda final (Ronda 0)
        addRoundKey(0);

        return getStateAsByteArray();
    }

    private void subBytes() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int value = state[i][j] & 0xFF; // Convertir byte firmado a unsigned
                state[i][j] = (byte) SBOX[value];
            }
        }
    }

    // Método para llenar la matriz por columnas
    private void initState(byte[] input) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[j][i] = input[i * 4 + j];
            }
        }
    }

    private void shiftRows() {
        byte[] temp = new byte[4];

        // Fila 0: No se mueve

        // Fila 1: Desplazamiento de 1 a la izquierda
        temp[0] = state[1][1];
        temp[1] = state[1][2];
        temp[2] = state[1][3];
        temp[3] = state[1][0];
        System.arraycopy(temp, 0, state[1], 0, 4);

        // Fila 2: Desplazamiento de 2 a la izquierda
        temp[0] = state[2][2];
        temp[1] = state[2][3];
        temp[2] = state[2][0];
        temp[3] = state[2][1];
        System.arraycopy(temp, 0, state[2], 0, 4);

        // Fila 3: Desplazamiento de 3 a la izquierda
        temp[0] = state[3][3];
        temp[1] = state[3][0];
        temp[2] = state[3][1];
        temp[3] = state[3][2];
        System.arraycopy(temp, 0, state[3], 0, 4);
    }

    private void mixColumns() {
        for (int i = 0; i < 4; i++) {
            byte a = state[0][i];
            byte b = state[1][i];
            byte c = state[2][i];
            byte d = state[3][i];

            state[0][i] = (byte) (gm(a, 2) ^ gm(b, 3) ^ a(c) ^ a(d));
            state[1][i] = (byte) (a(a) ^ gm(b, 2) ^ gm(c, 3) ^ a(d));
            state[2][i] = (byte) (a(a) ^ a(b) ^ gm(c, 2) ^ gm(d, 3));
            state[3][i] = (byte) (gm(a, 3) ^ a(b) ^ a(c) ^ gm(d, 2));
        }
    }

    // Función auxiliar para simplificar el casteo a int (evita ruido visual)
    private int a(byte b) { return b & 0xFF; }

    // Multiplicación en el Campo de Galois (GF(2^8))
    private int gm(byte b, int factor) {
        int res = 0;
        int a = b & 0xFF;
        for (int i = 0; i < 8; i++) {
            if ((factor & 1) != 0) res ^= a;
            boolean hiBitSet = (a & 0x80) != 0;
            a <<= 1;
            if (hiBitSet) a ^= 0x11B;
            factor >>= 1;
        }
        return res & 0xFF;
    }

    // Constantes de ronda para la expansión de llaves
    private static final int[] RCON = {
            0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36
    };

    // Almacén para las llaves de todas las rondas (11 llaves de 16 bytes = 176 bytes)
    private byte[] roundKeys = new byte[176];

    private void subWord(byte[] w) {
        for (int i = 0; i < 4; i++) w[i] = (byte) SBOX[w[i] & 0xFF];
    }

    private void rotWord(byte[] w) {
        byte temp = w[0];
        w[0] = w[1];
        w[1] = w[2];
        w[2] = w[3];
        w[3] = temp;
    }

    private void keyExpansion(byte[] key) {
        // La primera llave es la llave original
        System.arraycopy(key, 0, roundKeys, 0, 16);

        byte[] temp = new byte[4];
        for (int i = 16; i < 176; i += 4) {
            System.arraycopy(roundKeys, i - 4, temp, 0, 4);

            if (i % 16 == 0) {
                rotWord(temp);
                subWord(temp);
                temp[0] ^= (byte) RCON[i / 16];
            }

            for (int j = 0; j < 4; j++) {
                roundKeys[i + j] = (byte) (roundKeys[i - 16 + j] ^ temp[j]);
            }
        }
    }

    private void addRoundKey(int round) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Extraemos el byte correcto de la llave expandida
                state[j][i] ^= roundKeys[round * 16 + i * 4 + j];
            }
        }
    }

    // Helper para sacar los datos de la matriz al final
    private byte[] getStateAsByteArray() {
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * 4 + j] = state[j][i];
            }
        }
        return output;
    }

    private void invSubBytes() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) INV_SBOX[state[i][j] & 0xFF];
            }
        }
    }

    private void invShiftRows() {
        byte[] temp = new byte[4];
        // Fila 1: 1 a la derecha
        temp[1] = state[1][0]; temp[2] = state[1][1]; temp[3] = state[1][2]; temp[0] = state[1][3];
        System.arraycopy(temp, 0, state[1], 0, 4);
        // Fila 2: 2 a la derecha
        temp[2] = state[2][0]; temp[3] = state[2][1]; temp[0] = state[2][2]; temp[1] = state[2][3];
        System.arraycopy(temp, 0, state[2], 0, 4);
        // Fila 3: 3 a la derecha
        temp[3] = state[3][0]; temp[0] = state[3][1]; temp[1] = state[3][2]; temp[2] = state[3][3];
        System.arraycopy(temp, 0, state[3], 0, 4);
    }

    private void invMixColumns() {
        for (int i = 0; i < 4; i++) {
            byte a = state[0][i]; byte b = state[1][i]; byte c = state[2][i]; byte d = state[3][i];
            state[0][i] = (byte) (gm(a, 14) ^ gm(b, 11) ^ gm(c, 13) ^ gm(d, 9));
            state[1][i] = (byte) (gm(a, 9) ^ gm(b, 14) ^ gm(c, 11) ^ gm(d, 13));
            state[2][i] = (byte) (gm(a, 13) ^ gm(b, 9) ^ gm(c, 14) ^ gm(d, 11));
            state[3][i] = (byte) (gm(a, 11) ^ gm(b, 13) ^ gm(c, 9) ^ gm(d, 14));
        }
    }
}