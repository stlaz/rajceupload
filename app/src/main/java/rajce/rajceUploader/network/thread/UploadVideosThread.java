/**
 * Nazev: UploadVideosThread.java
 * Autor: Tomas Kunovsky
 * Popis: Vlakno pro upload libovolneho poctu videi do zadaneho alba prihlaseneho uzivatele.
 */

package rajce.rajceUploader.network.thread;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;

import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.VideoAPI;
import rajce.rajceUploader.XML.BeginVideoUploadRequest;
import rajce.rajceUploader.XML.BeginVideoUploadResponse;
import rajce.rajceUploader.XML.BlocksVideoUploadRequest;
import rajce.rajceUploader.XML.BlocksVideoUploadResponse;
import rajce.rajceUploader.XML.CloseAlbumRequest;
import rajce.rajceUploader.XML.CloseAlbumResponse;
import rajce.rajceUploader.XML.EndVideoUploadRequest;
import rajce.rajceUploader.XML.EndVideoUploadResponse;
import rajce.rajceUploader.XML.OpenAlbumRequest;
import rajce.rajceUploader.XML.OpenAlbumResponse;
import rajce.rajceUploader.network.info.APIStateUpload;

import android.os.Handler;
import android.util.Log;

public class UploadVideosThread  extends UploadThread {
    private String albumToken;
    private ArrayList<RajceAPI.Video> videos;
    private boolean errorUpload;
    private ArrayList<Integer> blocksSize;
    private ArrayList<String> blocksMD5;
    private ArrayList<Integer> missingBlocks;
    private final int BLOCK_SIZE = 512 * 1024;
    private VideoAPI videoAPI;
    private Handler mHandler;

    static public class Blocks {
        private byte[] aBlock;
        private String path;
        private int aBlockIndex;
        private int blockSize;
        private FileInputStream fin;
        private int blocksCount;
        private long fileSize;

        public Blocks(String path, int blockSize) {
            this.path = path;
            this.blockSize = blockSize;
        }

        public String getFullName() {
            return path;
        }

        public int getAIndex() {
            return aBlockIndex;
        }

        public int getBlocksCount() {
            return blocksCount;
        }

        public void open() throws Exception {
            aBlockIndex = -1;
            File file = new File(path);
            fileSize = file.length();
            blocksCount = getNumberBlocks(fileSize, blockSize);
            fin = new FileInputStream(file);
        }

        public void setBlock(int index) throws Exception {
            if ((index >= blocksCount) || (index < 0)) {
                throw new Exception("Pozadovano chybne cislo bloku.");
            } else if (aBlockIndex == -1) { //ctu poprve
                fin.skip(index * blockSize);
                if ((index == (blocksCount - 1)) && ((fileSize % blockSize) != 0) ) { //posledni blok, ten je mensi
                    int sizeLastBlock = (int)(this.fileSize % this.blockSize);
                    aBlock = new byte[sizeLastBlock];
                } else {
                    aBlock = new byte[this.blockSize];
                }
                fin.read(aBlock);
                aBlockIndex = index;
            } else if (index > aBlockIndex) {
                fin.skip((index - aBlockIndex - 1) * blockSize);
                if ((index == (blocksCount - 1)) && ((fileSize % blockSize) != 0) ) { //posledni blok, ten je mensi
                    int sizeLastBlock = (int)(this.fileSize % this.blockSize);
                    aBlock = new byte[sizeLastBlock];
                } else {
                    aBlock = new byte[this.blockSize];
                }
                fin.read(aBlock);
                aBlockIndex = index;
            } else if (index < aBlockIndex) {
                close();
                open();
                setBlock(index);
            }
        }

        public byte[] getBlock() throws Exception {
            return aBlock;
        }

        public void close() throws Exception {
            if (fin != null) {
                fin.close();
            }
        }

    }

    public UploadVideosThread(int albumID, RajceAPI rajceAPI, String token, APIStateUpload stat, ArrayList<RajceAPI.Video> videos, Handler mHandler) {
        super(albumID, rajceAPI, token, stat);
        this.videos = videos;
        this.videoAPI = new VideoAPI();
        this.mHandler = mHandler;
    }

    public void run() {
        albumToken = openAlbum(albumID);
        if (albumToken == null) {
            mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(result);
                }
            });
        } else {
            errorUpload = false;
            int totalNumberBlocks = getTotalNumberBlocks(videos);
            StateUploadVideos stateUploadVideos = new StateUploadVideos(totalNumberBlocks, stat, rajceAPI, mHandler);
            for (int i = 0; i < videos.size(); i++)  {
                String uploadID = beginVideoUpload();
                if (uploadID == null) {
                    errorUpload = true;
                    break;
                }
                blocksSize = new ArrayList<Integer>();
                blocksMD5 = new ArrayList<String>();

                Blocks blocks = new Blocks(videos.get(i).fullFileName, this.BLOCK_SIZE);
                int res = uploadVideoBlocks(blocks, token,uploadID,stateUploadVideos);
                if (res != 0) {
                    errorUpload = true;
                    break;
                }
                do {
                    missingBlocks = new ArrayList<Integer>();
                    res = this.endVideoUpload(token, uploadID, videos.get(i), blocksSize, blocksMD5);
                    if (res == 1) {
                        this.uploadVideoMissingBlocks(blocks, token,uploadID);
                    } else if (res != 0) {
                        errorUpload = true;
                        break;
                    }

                } while(res == 1);
            }

            if (closeAlbum(albumToken) != 0) {
                errorUpload = true;
            }
            if (errorUpload) {
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(result);
                    }
                });
            } else {
                rajceAPI.setSessionToken(this.token);
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                    }
                });
            }
        }
    }

    /**
     * Spocita kolik bloku budou mit vsechny videa dohromady.
     * @return pocet bloku
     */
    private int getTotalNumberBlocks(ArrayList<RajceAPI.Video> videos) {
        int result = 0;
        for (int i = 0; i < videos.size(); i++) {
            result += getNumberBlocksVideo(videos.get(i).fullFileName);
        }
        return result;
    }

    private int getNumberBlocksVideo(String path) {
        return getNumberBlocks((new File(path)).length(), BLOCK_SIZE );
    }

    /**
     * Prepocte velikost na pocet bloku.
     * @return pocet bloku
     */
    static private int getNumberBlocks(long size, int blockSize) {
        long result = ((size % blockSize) == 0) ? (size / blockSize) : ((size / blockSize) + 1);
        return (int) result;
    }

    /**
     * Provede upload vsech bloku jednoho videa.
     * @return 0 pokud vse probehne v poradku
     */
    public int uploadVideoBlocks(Blocks blocks, String token, String uploadID, StateUploadVideos stateUploadVideos) {
        try {
            blocks.open();
            for (int j = 0; j < blocks.getBlocksCount(); j++) {
                int result;
                do {
                    result = uploadVideoBlock(blocks, j, token, uploadID);
                } while (result == 1);
                if (result != 0) {
                    return result;
                }
                stateUploadVideos.incUploaded();
            }
            blocks.close();
        } catch (Exception e) {
            this.result = e.toString();
            return 2;
        }
        return 0;
    }

    public int uploadVideoMissingBlocks(Blocks blocks, String token, String uploadID) {
        try {
            blocks.open();
            for (int j = 0; j < this.missingBlocks.size(); j++) {
                int result;
                do {
                    result = uploadVideoBlock(blocks, missingBlocks.get(j), token, uploadID);
                } while (result == 1);
                if (result != 0) {
                    return result;
                }
            }
            blocks.close();
        } catch (Exception e) {
            this.result = e.toString();
            return 2;
        }
        return 0;
    }

    /**
     * Provede upload bloku videa.
     * @return 0 pokud vse probehne v poradku
     */
    public int uploadVideoBlock(Blocks blocks, int indexBlock, String token, String uploadID) {
        Serializer serializer = new Persister();

        try {
            blocks.setBlock(indexBlock);
            StringWriter sw = new StringWriter();
            ArrayList<BlocksVideoUploadRequest.Parameters.Block> blocksXML = new ArrayList<BlocksVideoUploadRequest.Parameters.Block>();
            blocksXML.add(new BlocksVideoUploadRequest.Parameters.Block(indexBlock, blocks.getBlock().length, MD5(blocks.getBlock())));
            BlocksVideoUploadRequest blocksVideoUploadRequest = new BlocksVideoUploadRequest(token, uploadID, blocksXML);
            serializer.write(blocksVideoUploadRequest, sw);
            String result = rajceHttp.sendVideoBlock(sw.toString(), blocks);

            BlocksVideoUploadResponse blocksVideoUploadResponse  = serializer.read(BlocksVideoUploadResponse.class, new StringReader( result ), false );
            if (blocksVideoUploadResponse.errorCode == null) {
                rajceAPI.setSessionToken(blocksVideoUploadResponse.sessionToken);
                this.token = blocksVideoUploadResponse.sessionToken;
                boolean ack = false;
                for (int m = 0; m < blocksVideoUploadResponse.blocks.size(); m++) {
                    if (blocksVideoUploadResponse.blocks.get(m).index == indexBlock) {
                        ack = true;
                        break;
                    }
                }

                if (ack) {
                    blocksSize.add(blocks.getBlock().length);
                    blocksMD5.add(MD5(blocks.getBlock()));
                    return 0;
                } else {
                    return 1;
                }

            } else {
                this.result = blocksVideoUploadResponse.result;
                return 2;
            }
        } catch (Exception e) {
            this.result = e.toString();
            return 3;
        }
    }


    /**
     * Provede MD5 hash dat.
     * @param md5 data na ktery se ma aplikovat hash
     * @return hash jako retezec
     */
    public static String MD5(byte[] md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    /**
     * Započatí nahrávání videa.
     * @return uploadID nebo null pokud dojde k chybe
     */
    private String beginVideoUpload() {
        Serializer serializer = new Persister();
        try {
            StringWriter sw = new StringWriter();
            serializer.write(new BeginVideoUploadRequest(token, albumToken, generateClientVideoID()), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            BeginVideoUploadResponse beginVideoUploadResponse = serializer.read(BeginVideoUploadResponse.class, new StringReader( result ), false );
            if (beginVideoUploadResponse.errorCode == null) {
                rajceAPI.setSessionToken(beginVideoUploadResponse.sessionToken);
                this.token = beginVideoUploadResponse.sessionToken;
                return beginVideoUploadResponse.uploadID;
            } else {
                this.result = beginVideoUploadResponse.result;
                return null;
            }
        } catch (Exception e) {
            this.result = e.toString();
            return null;
        }
    }

    private int endVideoUpload(String token, String uploadID, RajceAPI.Video video, ArrayList<Integer> blocksSize, ArrayList<String> blocksMD5) {
        Serializer serializer = new Persister();

        try {
            videoAPI.setVideo(video.fullFileName);
            StringWriter sw = new StringWriter();
            ArrayList<EndVideoUploadRequest.Parameters.Block> blocksXML = new ArrayList<EndVideoUploadRequest.Parameters.Block>();
            for (int i = 0; i < blocksSize.size(); i++) {
                blocksXML.add(new EndVideoUploadRequest.Parameters.Block(i, blocksSize.get(i), blocksMD5.get(i)));
            }
            EndVideoUploadRequest.Parameters.VideoInfo videoInfo = new EndVideoUploadRequest.Parameters.VideoInfo();
            videoAPI.setVideo(video.fullFileName);
            videoInfo.name = video.name;
            videoInfo.description = video.description;
            int fileSize = (int) (new File(video.fullFileName)).length();
            videoInfo.fileSizeOriginal = fileSize;
            videoInfo.width = videoAPI.width;
            videoInfo.height = videoAPI.height;
            videoInfo.sourceWidth = videoAPI.width;
            videoInfo.sourceHeight = videoAPI.height;
            videoInfo.rotate = videoAPI.rotate;
            videoInfo.fullFileName = video.fullFileName;
            videoInfo.dateTime = videoAPI.dateTime;
            videoInfo.vcodec = videoAPI.vcodec;
            videoInfo.vcodecSrc = videoAPI.vcodec;
            videoInfo.duration = videoAPI.duration;
            videoInfo.durationSrc = videoAPI.duration;
            videoInfo.begin = "0.000";
            videoInfo.end = videoAPI.duration;
            videoInfo.thumbpos = "0.000";
            videoInfo.vbitrate = videoAPI.bitrate;
            videoInfo.vbitrateSrc = videoAPI.bitrate;
            videoInfo.framerate = videoAPI.frameRate;
            videoInfo.framerateSrc = videoAPI.frameRate;
            Log.i("framerate", videoInfo.framerate);

            EndVideoUploadRequest endVideoUploadRequest = new EndVideoUploadRequest(token, uploadID, blocksXML, videoInfo);

            serializer.write(endVideoUploadRequest, sw);
            Bitmap thumb =  Bitmap.createScaledBitmap (videoAPI.preview, 100, 100, true);
            String result = rajceHttp.sendEndVideo(sw.toString(), videoAPI.preview, thumb, video);
            EndVideoUploadResponse endVideoUploadResponse  = serializer.read(EndVideoUploadResponse.class, new StringReader( result ), false );
            if (endVideoUploadResponse.errorCode == null) {
                rajceAPI.setSessionToken(endVideoUploadResponse.sessionToken);
                this.token = endVideoUploadResponse.sessionToken;
                boolean ack = true;
                if (endVideoUploadResponse.blocks.size() != 0) {
                    ack = false;
                    for (int m = 0; m < endVideoUploadResponse.blocks.size(); m++) {
                        missingBlocks.add(endVideoUploadResponse.blocks.get(m).index);
                    }
                }

                if (ack) {
                    return 0;
                } else {
                    return 1;
                }

            } else {
                this.result = endVideoUploadResponse.result;
                return 2;
            }
        } catch (Exception e) {
            this.result = e.toString();
            return 3;
        }
    }

    /**
     * Vygeneruje zcela unikatni clientVideoID (pokud je na zarizeni nastaven spravny cas,
     * v opacnem pripade se to muze dost dobre zacyklit, ale tohle stejne nikdo nikdy cist nebude).
     * @return clientVideoID
     */
    private String generateClientVideoID() {
        long time = new Date().getTime();
        return Long.toString(time, 36);
    }
}
