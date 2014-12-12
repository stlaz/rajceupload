package rajce.rajceUploader.network;

import java.net.*;
import java.io.*;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.thread.StateUploadVideos;
import rajce.rajceUploader.network.thread.UploadVideosThread;

import android.graphics.Bitmap;

public class RajceHttp {
    final private String rajceAPIUrl = "http://www.rajce.idnes.cz/liveAPI/index.php"; //adresa vstupniho bodu pro komunikaci
    final private String boundary = "---------------------------7d226f700d0"; //unikatni oddelovat v protokolu http
    private boolean monitor = false;
    private StatePhotoUpload stat;

    public static interface StatePhotoUpload {
        void changeStat(int newStat);
    }

    public RajceHttp() {
        super();
    }

    /**
     * Posle XML dokument serveru a prijme od nej XML dokument.
     * @param request XML dokument
     * @return prijaty XML dokument
     */
    public String sendRequest(String request) throws Exception { 
        request = request.replaceAll("&amp;", "%26amp%3B");//prekodoje v XML vsechny ampersandy
        String result = "";
        String urlParameters = "data=" + request; //pripoji k http pozadovane XML
        URL url = new URL(rajceAPIUrl);
        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Accept-Charset", "UTF-8"); 
        
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

        writer.write(urlParameters);
        writer.close();

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));        
        while ((line = reader.readLine()) != null) {
            result += line;
        }

        reader.close(); 
        return result;
    }
    
    private void writeImage(Bitmap image, String name, String filename, DataOutputStream out) throws Exception {
        out.writeBytes("Content-Disposition: form-data; name=\"" + name +"\"; filename=\"" + filename + "\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n");
        out.writeBytes("\r\n");
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bas);
        byte[] bytes = bas.toByteArray();
        
        if (monitor) {
            int totalSize = bytes.length;
            int bytesTransferred = 0;
            int chunkSize = 1024*512;
    
            while (bytesTransferred < totalSize) {
                int nextChunkSize = totalSize - bytesTransferred;
                if (nextChunkSize > chunkSize) {
                    nextChunkSize = chunkSize;
                }

                out.write(bytes, bytesTransferred, nextChunkSize);
                bytesTransferred += nextChunkSize;
                stat.changeStat((100 * bytesTransferred) / totalSize);
            }
        } else {
            out.write(bytes);
        }
        
        out.writeBytes("\r\n");
        out.writeBytes("--" + boundary + "\r\n");
    }

    private void writeImage(String name, String filename, DataOutputStream out) throws Exception {
        out.writeBytes("Content-Disposition: form-data; name=\"" + name +"\"; filename=\"" + filename + "\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n");
        out.writeBytes("\r\n");

        File file = new File(filename);
        FileInputStream fin = new FileInputStream(file);
        long totalSize = file.length();
        long bytesTransferred = 0;
        int chunkSize = 1024*512;

        while (bytesTransferred < totalSize) {
            long nextChunkSize = totalSize - bytesTransferred;
            if (nextChunkSize > chunkSize) {
                nextChunkSize = chunkSize;
            }
            byte[] bytes = new byte[(int) nextChunkSize];
            fin.read(bytes);
            out.write(bytes);
            out.flush();
            bytesTransferred += nextChunkSize;
            if (monitor) {
                int newStat = (int) ((100 * bytesTransferred) / totalSize);
                stat.changeStat(newStat);
            }
        }
        if (fin != null) {
            fin.close();
        }

        out.writeBytes("\r\n");
        out.writeBytes("--" + boundary + "\r\n");
    }
    
    private void writeXML(String xml, DataOutputStream out) throws Exception {
        out.writeBytes("Content-Disposition: form-data; name=\"data\"\r\n");
        out.writeBytes("Content-Type: text/xml\r\n");
        out.writeBytes("\r\n");
        out.writeBytes(xml);
        out.writeBytes("\r\n");
        out.writeBytes("--" + boundary + "\r\n");
    }

    /**
     * Posle fotku na rajce a prijme odpoved ve formatu XML dokumentu.
     * @param xml XML dokument s pozadovanymi informacemi
     * @param image fotografie
     * @param thumb nahled fotografie
     * @param stat rozhrani pro zasilani informaci o stavu uploadu
     * @return odpoved (XML) od serveru
     */
    public String sendPhoto(String xml, Bitmap thumb, RajceAPI.Photo photo, StatePhotoUpload stat) throws Exception  {
        this.stat = stat;
        URL url = new URL(rajceAPIUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept-Charset", "UTF-8"); 
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type","multipart/form-data; boundary=" + boundary);
        DataOutputStream dataStream = new DataOutputStream(conn.getOutputStream()); 
        dataStream.writeBytes("--" + boundary + "\r\n");
        monitor = false; //rika, zda ma informovat o stavu nahravani
        writeImage(thumb, "thumb", photo.fullFileName, dataStream);
        monitor = true;
        writeImage("photo", photo.fullFileName, dataStream);
        monitor = false;
        writeXML(xml, dataStream);
        dataStream.flush();
        dataStream.close();
        String result = "";
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));        
        while ((line = reader.readLine()) != null) {
            result += line;
        }

        reader.close();
        return result;
    }

    public String sendEndVideo(String xml, Bitmap image, Bitmap thumb, RajceAPI.Video video) throws Exception   {
        URL url = new URL(rajceAPIUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type","multipart/form-data; boundary=" + boundary);
        DataOutputStream dataStream = new DataOutputStream(conn.getOutputStream());
        dataStream.writeBytes("--" + boundary + "\r\n");
        monitor = false; //rika, zda ma informovat o stavu nahravani
        writeImage(thumb, "thumb", video.fullFileName, dataStream);
        writeImage(image, "image", video.fullFileName, dataStream);
        writeXML(xml, dataStream);
        dataStream.flush();
        dataStream.close();
        String result = "";
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        while ((line = reader.readLine()) != null) {
            result += line;
        }

        reader.close();
        return result;

    }

    public String sendVideoBlock(String xml,  UploadVideosThread.Blocks blocks) throws Exception {
        URL url = new URL(rajceAPIUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type","multipart/form-data; boundary=" + boundary);
        DataOutputStream dataStream = new DataOutputStream(conn.getOutputStream());
        dataStream.writeBytes("--" + boundary + "\r\n");
        writeVideoBlock(blocks.getBlock(), "data" + blocks.getAIndex(), blocks.getFullName(), dataStream);
        writeXML(xml, dataStream);
        dataStream.flush();
        dataStream.close();
        String result = "";
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        while ((line = reader.readLine()) != null) {
            result += line;
        }

        reader.close();
        return result;

    }

    private void writeVideoBlock(byte[] block, String name, String filename, DataOutputStream out) throws Exception  {
        out.writeBytes("Content-Disposition: form-data; name=\"" + name +"\"; filename=\"" + filename + "\"\r\n");
        out.writeBytes("Content-Type: application/octet-stream\r\n");
        out.writeBytes("\r\n");
        out.write(block);
        out.writeBytes("\r\n");
        out.writeBytes("--" + boundary + "\r\n");
    }

}
