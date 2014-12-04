/**
 * Nazev: APIState.java
 * Autor: Tomas Kunovsky
 * Popis: Rozhrani pro zpetne informovani o vysledku operace (napr prihlaseni).
 */

package rajce.rajceUploader.network.info;

public interface APIState {
    /**
     * Informuje, ze operace skoncila s chybou.
     * @param error text chyby
     * @return
     */
    void error(String error);
    /**
     * Informuje, ze operace skoncila uspesne.
     * @param error text chyby
     * @return
     */
    void finish();
}