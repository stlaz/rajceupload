/**
 * Nazev: APIStateNewAlbum.java
 * Autor: Tomas Kunovsky
 * Popis: Rozhrani pro zpetne informovani o vysledku operace vytvoreni noveho alba.
 */

package rajce.rajceUploader.network.info;

public interface APIStateNewAlbum extends APIState {
    /**
     * Preda identifikator vytvoreneho alba.
     * @param id identifikator alba
     * @return
     */
    void setAlbumID(int id);    
}


