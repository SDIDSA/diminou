package org.luke.diminou.app.pages.game;

import androidx.annotation.RawRes;

import org.luke.diminou.R;
import org.luke.diminou.app.avatar.Avatar;

public enum PlaySound {
    SOUND_1(R.raw.play_1),
    SOUND_2(R.raw.play_2),
    SOUND_3(R.raw.play_3),
    SOUND_4(R.raw.play_4),
    SOUND_5(R.raw.play_5),
    SOUND_6(R.raw.play_6),
    SOUND_7(R.raw.play_7),
    SOUND_8(R.raw.play_8),
    SOUND_9(R.raw.play_9),
    SOUND_10(R.raw.play_10),
    SOUND_11(R.raw.play_11),
    SOUND_12(R.raw.play_12),
    SOUND_13(R.raw.play_13),
    SOUND_14(R.raw.play_14),
    SOUND_15(R.raw.play_15),
    SOUND_16(R.raw.play_16),
    SOUND_17(R.raw.play_17),
    SOUND_18(R.raw.play_18),
    SOUND_19(R.raw.play_19),
    SOUND_20(R.raw.play_20),
    SOUND_21(R.raw.play_21),
    SOUND_22(R.raw.play_22);

    private final @RawRes int res;
    PlaySound(@RawRes int res) {
        this.res = res;
    }

    public int getRes() {
        return res;
    }

    public static PlaySound random() {
        return values()[(int) (Math.random() * values().length)];
    }
}
