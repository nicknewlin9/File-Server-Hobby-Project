package com.newlin.gui;

import java.awt.*;

@SuppressWarnings("unused")
public enum MyColor
{
    GRAY1(new Color(32,32,32)),
    GRAY2(new Color(44,44,44)),
    GRAY3(new Color(66,66,66)),
    GRAY4(new Color(99,99,99)),
    GRAY5(new Color(132,132,132)),
    GRAY6(new Color(165,165,165)),
    ORANGE(new Color(197,144,114)),
    AQUA(new Color(85,169,182)),
    YELLOW(new Color(177,173,106)),
    GREEN(new Color(121,169,120)),
    BLUE(new Color(97,149,213));

    final Color color;

    MyColor(Color color)
    {
        this.color = color;
    }
}
