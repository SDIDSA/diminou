package org.luke.diminou.app.avatar;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.Store;

public class AvatarSelect extends AvatarDisplay {
    public AvatarSelect(App owner) {
        super(owner);

        setValue(Store.getAvatar());

        AvatarSelectOverlay overlay = new AvatarSelectOverlay(owner, () -> setValue(Store.getAvatar()));

        setOnClick(overlay::show);
    }
}
