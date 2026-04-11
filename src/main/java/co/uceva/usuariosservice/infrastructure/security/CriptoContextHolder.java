package co.uceva.usuariosservice.infrastructure.security;

public class CriptoContextHolder {
    private static final ThreadLocal<byte[]> AES_KEY = new ThreadLocal<>();
    private static final ThreadLocal<byte[]> IV = new ThreadLocal<>();

    public static void setContext(byte[] key, byte[] iv) {
        AES_KEY.set(key);
        IV.set(iv);
    }

    public static byte[] getKey() { return AES_KEY.get(); }
    public static byte[] getIv() { return IV.get(); }

    public static void clear() {
        AES_KEY.remove();
        IV.remove();
    }
}