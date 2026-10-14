import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Convert the compact, unchanged app artwork into jpackage's PNG input. */
public final class IconAsset {
    private IconAsset() { }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) throw new IllegalArgumentException("Usage: IconAsset source.jpg new-icon.png");
        Path source = Path.of(args[0]), target = Path.of(args[1]);
        if (Files.exists(target)) throw new IOException("Refusing to overwrite existing icon");
        var image = ImageIO.read(source.toFile());
        if (image == null || image.getWidth() != 256 || image.getHeight() != 256)
            throw new IOException("Expected the 256 x 256 repository artwork");
        if (!ImageIO.write(image, "PNG", target.toFile())) throw new IOException("No PNG writer available");
    }
}
