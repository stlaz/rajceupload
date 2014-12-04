/**
 * Nazev: APIStateGetAlbumList.java
 * Autor: Tomas Kunovsky
 * Popis: Rozhrani pro zpetne informovani o vysledku operace ziskani seznamu alb.
 */

package rajce.rajceUploader.network.info;

import rajce.rajceUploader.XML.AlbumListResponse;

public interface APIStateGetAlbumList extends APIState {
    /**
     * Preda seznam alb.
     * @param albumList seznam alb
     * @return
     */
    void setAlbumList(AlbumListResponse albumList);
}
