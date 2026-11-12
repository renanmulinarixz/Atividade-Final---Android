package br.com.pokespawn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.FrameLayout;

// Small original pixel landscape, drawn without external background assets.
public final class PixelScene extends FrameLayout {
    private final Paint paint = new Paint();
    public PixelScene(Context context) { super(context); setWillNotDraw(false); }
    private void block(Canvas canvas, int color, float x, float y, float w, float h) {
        paint.setColor(color); canvas.drawRect(x, y, x + w, y + h, paint);
    }
    @Override protected void onDraw(Canvas original) {
        super.onDraw(original);
        int save = original.save();
        original.scale(getWidth() / 160f, getHeight() / 112f);
        block(original, Color.rgb(191, 224, 200), 0, 0, 160, 112);
        block(original, Color.rgb(231, 245, 217), 94, 11, 29, 6);
        block(original, Color.rgb(231, 245, 217), 101, 7, 14, 14);
        block(original, Color.rgb(231, 245, 217), 24, 22, 20, 5);
        block(original, Color.rgb(117, 164, 113), 0, 64, 160, 48);
        block(original, Color.rgb(145, 187, 113), 0, 74, 160, 38);
        for (int i = 0; i < 18; i++) {
            float x = (i * 29 + 7) % 160, y = 80 + (i * 7) % 29;
            block(original, Color.rgb(108, 157, 89), x, y, 2, 4);
            block(original, Color.rgb(177, 204, 130), x + 2, y + 2, 2, 2);
        }
        tree(original, -7, 15); tree(original, 133, 25);
        block(original, Color.rgb(84, 131, 83), 39, 87, 83, 7);
        block(original, Color.rgb(179, 205, 130), 46, 82, 69, 7);
        original.restoreToCount(save);
    }
    private void tree(Canvas canvas, int x, int y) {
        block(canvas, Color.rgb(113, 104, 65), x + 14, y + 33, 8, 23);
        block(canvas, Color.rgb(51, 112, 76), x + 5, y + 11, 28, 31);
        block(canvas, Color.rgb(51, 112, 76), x, y + 23, 38, 13);
        block(canvas, Color.rgb(73, 139, 85), x + 10, y + 4, 18, 29);
        block(canvas, Color.rgb(90, 153, 92), x + 15, y, 8, 19);
    }
}
