package utot.utot.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by elysi on 12/14/2016.
 */

public class TextViewPlus extends TextView {
    public TextViewPlus(Context context) {
        super(context);
    }

    public TextViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

    public TextViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

}