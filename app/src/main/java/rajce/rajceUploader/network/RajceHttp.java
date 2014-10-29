package rajce.rajceUploader.network;

import java.net.*;
import java.io.*;
import rajce.rajceUploader.RajceAPI;
import android.graphics.Bitmap;


public class RajceHttp {
    final private String rajceAPIUrl = "http://www.rajce.idnes.cz/liveAPI/index.php"; //adresa vstupniho bodu pro komunikaci
    final private String boundary = "---------------------------7d226f700d0";
    private boolean monitor = false;
    public static interface StateUpload {
        void changeStat(int newStat);
    }
    private StateUpload stat;

    public RajceHttp() {
        super();
    }
    
    public String sendRequest(String request) throws Exception { 
        request = request.replaceAll("&amp;", "%26amp%3B");
        String result = "";
        String urlParameters = "data=" + request;
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
                stat.changeStat((100 * bytesTransferred) / totalSize);
                int nextChunkSize = totalSize - bytesTransferred;
                if (nextChunkSize > chunkSize) {
                    nextChunkSize = chunkSize;
                }

                out.write(bytes, bytesTransferred, nextChunkSize);
                bytesTransferred += nextChunkSize;
            }
        } else {
            out.write(bytes);
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
    
    public String sendPhoto(String xml, Bitmap image, Bitmap thumb, RajceAPI.Photo photo, StateUpload stat) throws Exception  {
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
        writeImage(thumb, "thumb", photo.fullFileName, dataStream);
        monitor = true;
        writeImage(image, "photo", photo.fullFileName, dataStream);
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
        stat.changeStat(100);
        return result;
    }

}
