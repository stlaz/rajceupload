/**
 * Nazev: APIStateUpload.java
 * Autor: Tomas Kunovsky
 * Popis: Rozhrani pro zpetne informovani o vysledku operace uploadu.
 */

package rajce.rajceUploader.network.info;

public interface APIStateUpload extends APIState {
    /**
     * Prubezne informuje o stavu uploadu.
     * @param newStat hodnota v procentech (0-100)
     * @return
     */
    void changeStat(int newStat);
}
