package logbook.internal.listener;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.BiConsumer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import logbook.bean.AppConfig;
import logbook.bean.ShipMst;
import logbook.bean.ShipMstCollection;
import logbook.bean.Spritesmith;
import logbook.internal.kancolle.ShipImageCacheStrategy;
import logbook.internal.logger.LoggerHolder;
import logbook.listener.ContentListenerSpi;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * 画像ファイルを処理します
 *
 */
public class ImageListener implements ContentListenerSpi {

    /**
     * 処理対象URIか判定を行います
     * 
     * @param requestMetaData リクエストに含まれている情報
     * @return 処理対象URIの場合true
     */
    @Override
    public boolean test(RequestMetaData request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/kcs2/resources/") || uri.startsWith("/kcs2/img/");
    }

    /**
     * レスポンスを処理します
     * 
     * @param requestMetaData  リクエストに含まれている情報
     * @param responseMetaData レスポンスに含まれている情報
     */
    @Override
    public void accept(RequestMetaData request, ResponseMetaData response) {
        try {
            String uri = request.getRequestURI();
            // 艦娘画像
            if (uri.startsWith("/kcs2/resources/ship/")) {
                this.handleShipImages(request, response);
            }
            // MAP情報
            else if (uri.startsWith("/kcs2/resources/map/")) {
                this.handleMapImages(request, response, "map");
            }
            // ゲージ情報
            else if (uri.startsWith("/kcs2/resources/gauge/")) {
                this.handleImages(request, response, "gauge");
            }
            // 汎用画像
            else if (uri.startsWith("/kcs2/img/common/")) {
                this.handleImages(request, response, "common");
            }
            // 任務関連画像
            else if (uri.startsWith("/kcs2/img/duty/")) {
                this.handleImages(request, response, "duty");
            } else if (uri.startsWith("/kcs2/img/sally/")) {
                this.handleImages(request, response, "sally");
            } else {
                LoggerHolder.get().debug("処理対象外URIを検知:" + uri);
            }
        } catch (Exception e) {
            LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました", e);
        }
    }

    /**
     * 艦娘画像の処理を実施します
     * 
     * @param request  リクエスト
     * @param response レスポンス
     * @throws IOException 入出力例外が発生した場合
     */
    private void handleShipImages(RequestMetaData request, ResponseMetaData response) throws IOException {
        String uri = URI.create(request.getRequestURI()).getPath();
        String name = null;

        String fileExtension = null;
        if (AppConfig.get().isShipImageCompress()) {
            fileExtension = ".jpg";
        } else {
            fileExtension = ".png";
        }

        if (uri.contains("/banner/"))
            name = "1" + fileExtension;
        if (uri.contains("/banner_dmg/"))
            name = "3" + fileExtension;
        if (uri.contains("/card/"))
            name = "5" + fileExtension;
        if (uri.contains("/card_dmg/"))
            name = "7" + fileExtension;
        if (uri.contains("/full/"))
            name = "17" + fileExtension;
        if (uri.contains("/full_dmg/"))
            name = "19" + fileExtension;

        if (name != null) {
            ShipImageCacheStrategy strategy = AppConfig.get().getShipImageCacheStrategy();
            if (strategy == null || strategy.getFileNames() == null || strategy.getFileNames().contains(name)) {
                this.storeShipImage(name, request, response);
            }
        }
    }

    private void storeShipImage(String name, RequestMetaData request, ResponseMetaData response) throws IOException {
        String uri = URI.create(request.getRequestURI()).getPath();
        int nameIndex = uri.lastIndexOf('/');
        int extIndex = uri.indexOf('_', nameIndex);
        String shipid = uri.substring(nameIndex + 1, extIndex);

        ShipMst shipMst = ShipMstCollection.get()
                .getShipMap()
                .get(Integer.parseInt(shipid));

        if (response.getResponseBody().isPresent()) {
            byte[] imageBytes = response.getResponseBody().get().readAllBytes();
            Path path = ShipMst.getResourcePathDir(shipMst).resolve(name);

            String md5 = calculateMD5(imageBytes);
            if (this.md5Matches(md5, path)) {
                // 一致しているなら更新はない
                return;
            }

            if (AppConfig.get().isShipImageCompress()) {
                // 画像ファイルをjpgに再圧縮する
                imageBytes = this.compressImage(new ByteArrayInputStream(imageBytes));
            }
            this.write(new ByteArrayInputStream(imageBytes), path, md5);
        }
    }

    /**
     * MD5値を計算します
     * 
     * @param bytes
     * @return
     */
    private String calculateMD5(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            LoggerHolder.get().warn("MD5処理中に例外が発生しました", e);
            return "";
        }
    }

    /**
     * MD5が一致するかチェックします。
     * 
     * @param md5  MD5値
     * @param path 保存先画像ファイルのパス
     * @return 一致する場合はtrue、不一致またはファイル不在な場合はfalse
     */
    private boolean md5Matches(String md5, Path path) {
        Path md5Path = this.getMd5Path(path);

        if (!Files.exists(md5Path)) {
            return false;
        }

        try {
            String storedMd5 = Files.readString(md5Path).trim();
            return md5.equals(storedMd5);
        } catch (IOException e) {
            LoggerHolder.get().warn("MD5ファイル読込中に例外が発生しました", e);
            return false;
        }
    }

    /**
     * 汎用画像（ゲージ、コモン、出撃、任務など）を保存し、必要に応じてスプライト分解を行います。
     * 
     * @param request  リクエスト
     * @param response レスポンス
     * @param dirname  保存先のディレクトリ名
     * @throws IOException 入出力例外が発生した場合
     */
    private void handleImages(RequestMetaData request, ResponseMetaData response, String dirname) throws IOException {
        String uri = request.getRequestURI();
        Path dir = Paths.get(AppConfig.get().getResourcesDir(), dirname);
        Path path = dir.resolve(Paths.get(URI.create(uri).getPath()).getFileName());
        if (response.getResponseBody().isPresent()) {
            this.write(response.getResponseBody().get(), path, "");

            String filename = String.valueOf(path.getFileName());
            // pngファイル
            Path pngPath = null;
            // jsonファイル
            Path jsonPath = null;

            // jsonファイルの場合
            if (filename.endsWith(".json")) {
                pngPath = path.resolveSibling(filename.replace(".json", ".png"));
                jsonPath = path;
            }
            // pngファイルの場合
            if (filename.endsWith(".png")) {
                pngPath = path;
                jsonPath = path.resolveSibling(filename.replace(".png", ".json"));
            }
            // 分解した画像の格納先
            Path spriteDir = pngPath.resolveSibling(filename.substring(0, filename.lastIndexOf('.')));

            this.sprite(spriteDir, pngPath, jsonPath);
        }
    }

    /**
     * マップ画像を保存し、必要に応じてスプライト分解を行います。
     * 
     * @param request  リクエスト
     * @param response レスポンス
     * @param dirname  保存先のディレクトリ名
     * @throws IOException 入出力例外が発生した場合
     */
    private void handleMapImages(RequestMetaData request, ResponseMetaData response, String dirname)
            throws IOException {
        String uri = request.getRequestURI();
        Path dir = Paths.get(AppConfig.get().getResourcesDir(), dirname);

        Path fullPath = Paths.get(URI.create(uri).getPath());
        int pathCount = fullPath.getNameCount();
        Path mapFilePath = fullPath.subpath(3, pathCount); // "kcs2/resources/map" を除外
        Path path = dir.resolve(mapFilePath);
        if (response.getResponseBody().isPresent()) {
            this.write(response.getResponseBody().get(), path, "");

            String filename = String.valueOf(path.getFileName());
            // pngファイル
            Path pngPath = null;
            // jsonファイル
            Path jsonPath = null;

            // jsonファイルの場合
            if (filename.endsWith(".json")) {
                pngPath = path.resolveSibling(filename.replace(".json", ".png"));
                jsonPath = path;
            }
            // pngファイルの場合
            if (filename.endsWith(".png")) {
                pngPath = path;
                jsonPath = path.resolveSibling(filename.replace(".png", ".json"));
            }
            // 分解した画像の格納先
            Path spriteDir = pngPath.resolveSibling(filename.substring(0, filename.lastIndexOf('.')));

            this.sprite(spriteDir, pngPath, jsonPath);
        }
    }

    /**
     * 画像ファイルをスプライト情報に基づいて分解します。
     * 
     * @param storeDir 分解した画像の格納先ディレクトリ
     * @param imageSrc スプライト画像（元画像）のパス
     * @param jsonSrc  スプライト情報（JSON）のパス
     * @throws IOException 入出力例外が発生した場合
     */
    private void sprite(Path storeDir, Path imageSrc, Path jsonSrc) throws IOException {
        if (!Files.exists(imageSrc) || !Files.exists(jsonSrc)) {
            return;
        }
        if (!Files.exists(storeDir)) {
            Files.createDirectories(storeDir);
        }
        // 画像ファイルとスプライト情報のFileTimeを比較し、双方の時間が近い場合だけ画像ファイルを分解します。
        try {
            FileTime imageTime = Files.getLastModifiedTime(imageSrc);
            FileTime jsonTime = Files.getLastModifiedTime(jsonSrc);
            if (Duration.between(imageTime.toInstant(), jsonTime.toInstant())
                    .abs()
                    .compareTo(Duration.ofMinutes(10)) > 0) {
                return;
            }
        } catch (NoSuchFileException ne) {
            LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました NoSuchFileException [src=" + imageSrc + "]");
        } catch (Exception e) {
            LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました[src=" + imageSrc + "]", e);
        }
        Spritesmith sprite;
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(jsonSrc))) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            sprite = mapper.readValue(is, Spritesmith.class);
        }
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(imageSrc))) {
            BufferedImage image = ImageIO.read(is);

            BiConsumer<String, Spritesmith.Frame> action = (k, v) -> {
                Spritesmith.Rect rect = v.getFrame();
                BufferedImage subimage = image.getSubimage(rect.getX(), rect.getY(), rect.getW(), rect.getH());
                try {
                    Path temp = this.tempFile();
                    Path to = storeDir.resolve(k + ".png");
                    try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(temp))) {
                        ImageIO.write(subimage, "png", out);
                    }
                    try {
                        this.move(temp, to);
                    } catch (IOException e) {
                        Files.deleteIfExists(temp);
                        throw e;
                    }
                } catch (NoSuchFileException ne) {
                    LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました NoSuchFileException [src=" + imageSrc + "]");
                } catch (AccessDeniedException ae) {
                    LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました AccessDeniedException [src=" + imageSrc + "]");
                } catch (FileAlreadyExistsException fe) {
                    LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました FileAlreadyExistsException [src=" + imageSrc + "]");
                } catch (Exception e) {
                    LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました[src=" + imageSrc + "]", e);
                }
            };
            sprite.getFrames().forEach(action);
        }
    }

    private Path tempFile() throws IOException {
        return Files.createTempFile("ImageListener-", "");
    }

    private void write(InputStream from, Path to, String md5) throws IOException {
        Path temp = this.tempFile();
        Files.copy(from, temp, StandardCopyOption.REPLACE_EXISTING);
        try {
            this.move(temp, to);
        } catch (IOException e) {
            LoggerHolder.get().warn("画像ファイル処理中に例外が発生しました", e);
            Files.deleteIfExists(temp);
        }

        if (!"".equals(md5)) {
            try {
                Files.writeString(this.getMd5Path(to), md5);
            } catch (IOException e) {
                LoggerHolder.get().warn("MD5ファイル書き込み中に例外が発生しました", e);
            }
        }
    }

    private void move(Path from, Path to) throws IOException {
        Path parent = to.getParent();
        if (parent != null) {
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        }
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 指定されたパスのファイル名に ".md5" を付与したパスを返却します。
     * 
     * @param path 元のパス
     * @return ".md5" が付与されたパス
     */
    private Path getMd5Path(Path path) {
        return path.resolveSibling(path.getFileName().toString() + ".md5");
    }

    /**
     * 画像をjpeg形式で再圧縮します。
     *
     * @param in InputStream
     * @return InputStream
     */
    private byte[] compressImage(InputStream in) {
        try {
            BufferedImage image = ImageIO.read(in);

            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics gc = canvas.createGraphics();
            gc.setColor(Color.WHITE);
            gc.fillRect(0, 0, width, height);
            gc.drawImage(image, 0, 0, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                try {

                    ImageWriteParam iwp = writer.getDefaultWriteParam();
                    if (iwp.canWriteCompressed()) {
                        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        iwp.setCompressionQuality(0.8f);
                    }
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(canvas, null, null), iwp);
                } finally {
                    writer.dispose();
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
