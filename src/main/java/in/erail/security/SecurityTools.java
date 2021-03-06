package in.erail.security;

/**
 *
 * @author vinay
 */
import in.erail.glue.annotation.StartService;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.Logger;

public class SecurityTools {

  private Logger mLog;
  private Vertx mVertx;
  private SecureRandom mRandom;
  private final CompletableFuture<SecretKeySpec> mKeySpec = new CompletableFuture<>();
  private final CompletableFuture<String> mGlobalUniqueString = new CompletableFuture<>();

  @StartService
  public void startup() {

    setRandom(new SecureRandom());

    if (!getVertx().isClustered()) {
      mGlobalUniqueString.complete("A" + mRandom.nextInt());
      return;
    }

    generateKey()
            .flatMap(v -> addValueToClusterMap("key", v))
            .subscribe((key) -> {
              mKeySpec.complete(new SecretKeySpec(key, "AES"));
              String unique = Base64.getEncoder().encodeToString(Arrays.copyOfRange(key, 0, 5));
              mGlobalUniqueString.complete(unique.replace("=", ""));
              getLog().info(() -> String.format("GlobalUniqueString:[%s]", unique));
            });
  }

  protected Single<byte[]> addValueToClusterMap(String pKey, byte[] pValue) {
    return getVertx()
            .sharedData()
            .<String, byte[]>rxGetClusterWideMap("_in.erail.security")
            .flatMapMaybe(m -> m.rxPutIfAbsent(pKey, pValue))
            .toSingle(pValue);
  }

  protected Single<byte[]> generateKey() {
    KeyGenerator keygen;
    try {
      keygen = KeyGenerator.getInstance("AES");
    } catch (NoSuchAlgorithmException ex) {
      return Single.error(ex);
    }
    keygen.init(128);
    return Single.just(keygen.generateKey().getEncoded());
  }

  /**
   * Unique string across cluster. Changes on each restart of cluster.
   *
   * @return Globally unique string
   */
  public String getGlobalUniqueString() {
    try {
      return mGlobalUniqueString.get();
    } catch (InterruptedException | ExecutionException ex) {
      getLog().error("Global Unique not working", ex);
    }
    return "ERROR_GLOBAL_KEY";
  }

  private byte[] concatenateByteArrays(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  public String encrypt(String value) {

    try {
      byte[] riv = new byte[16];
      mRandom.nextBytes(riv);
      IvParameterSpec iv = new IvParameterSpec(riv);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), iv);
      byte[] encrypted = cipher.doFinal(value.getBytes());

      return Base64.getEncoder().encodeToString(concatenateByteArrays(encrypted, riv));
    } catch (InvalidAlgorithmParameterException
            | InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException
            | InterruptedException
            | ExecutionException ex) {
      getLog().error(ex);
    }

    return null;
  }

  public String decrypt(String encrypted) {

    try {
      byte[] data = Base64.getDecoder().decode(encrypted);
      IvParameterSpec iv = new IvParameterSpec(data, data.length - 16, 16);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), iv);

      byte[] original = cipher.doFinal(data, 0, data.length - 16);

      return new String(original);

    } catch (NoSuchAlgorithmException
            | NoSuchPaddingException
            | InvalidKeyException
            | InvalidAlgorithmParameterException
            | IllegalBlockSizeException
            | BadPaddingException
            | InterruptedException
            | ExecutionException ex) {
      getLog().error(ex);
    }

    return null;
  }

  public Logger getLog() {
    return mLog;
  }

  public void setLog(Logger pLog) {
    this.mLog = pLog;
  }

  public SecureRandom getRandom() {
    return mRandom;
  }

  public void setRandom(SecureRandom pRandom) {
    this.mRandom = pRandom;
  }

  public SecretKeySpec getKeySpec() throws InterruptedException, ExecutionException {
    return mKeySpec.get();
  }

  public Vertx getVertx() {
    return mVertx;
  }

  public void setVertx(Vertx pVertx) {
    this.mVertx = pVertx;
  }

}
