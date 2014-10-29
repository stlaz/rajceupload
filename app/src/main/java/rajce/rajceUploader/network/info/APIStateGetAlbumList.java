package rajce.rajceUploader.network.info;

import rajce.rajceUploader.XML.AlbumListResponse;

public interface APIStateGetAlbumList extends APIState {
    void setAlbumList(AlbumListResponse albumList);
}
