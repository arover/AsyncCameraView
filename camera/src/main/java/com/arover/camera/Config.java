package com.arover.camera;

/**
 * @author minstrel
 *         created at 19/07/2017 11:14
 */

public interface Config {
    AspectRatio DEFAULT_ASPECT_RATIO = AspectRatio.of(4, 3);

    int FACING_BACK = 0;
    int FACING_FRONT = 1;

    int FLASH_OFF = 0;
    int FLASH_ON = 1;
    int FLASH_TORCH = 2;
    int FLASH_AUTO = 3;
    int FLASH_RED_EYE = 4;
}
