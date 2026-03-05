//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public final class DatabaseService {
    private final Path storagePath;

    public DatabaseService(Path storagePath) {
        this.storagePath = storagePath;
    }

    public synchronized AppData loadOrCreate() {
        if (Files.exists(this.storagePath, new LinkOption[0])) {
            try {
                Throwable var1 = null;
                Object var17 = null;

                try {
                    ObjectInputStream in = new ObjectInputStream(Files.newInputStream(this.storagePath));

                    AppData var10000;
                    try {
                        Object obj = in.readObject();
                        var10000 = (AppData)obj;
                    } finally {
                        if (in != null) {
                            in.close();
                        }

                    }

                    return var10000;
                } catch (Throwable var15) {
                    if (var1 == null) {
                        var1 = var15;
                    } else if (var1 != var15) {
                        var1.addSuppressed(var15);
                    }

                    throw var1;
                }
            } catch (Exception var16) {
                try {
                    Path backup = this.storagePath.resolveSibling(this.storagePath.getFileName().toString() + ".corrupt.bak");
                    Files.move(this.storagePath, backup);
                } catch (IOException var13) {
                }

                return new AppData();
            }
        } else {
            return new AppData();
        }
    }

    public synchronized void save(AppData data) {
        try {
            Files.createDirectories(this.storagePath.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile creare la cartella dati.", e);
        }

        try {
            Throwable e = null;
            Object var3 = null;

            try {
                ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(this.storagePath));

                try {
                    out.writeObject(data);
                } finally {
                    if (out != null) {
                        out.close();
                    }

                }

            } catch (Throwable var15) {
                if (e == null) {
                    e = var15;
                } else if (e != var15) {
                    e.addSuppressed(var15);
                }

                throw e;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Errore nel salvataggio dei dati.", e);
        }
    }
}
