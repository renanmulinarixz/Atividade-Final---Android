package br.com.pokespawn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

// Figma 1:2: original ellipse assets, rectangular lower half, and stepped grass trim.
public final class LoginBanner extends View {
    private final Paint paint = new Paint();
    private final Bitmap ellipse, center;
    public LoginBanner(Context context) {
        super(context);
        ellipse = BitmapFactory.decodeResource(getResources(), R.drawable.login_ellipse);
        center = BitmapFactory.decodeResource(getResources(), R.drawable.login_center);
        setContentDescription("Pokébola sobre o cenário verde do PokéSpawn");
    }
    private void rect(Canvas canvas, int color, float x, float y, float width, float height) {
        paint.setColor(color); canvas.drawRect(x, y, x + width, y + height, paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int save = canvas.save(); canvas.scale(getWidth() / 390f, getHeight() / 220f);
        rect(canvas, 0xff4e8866, 0, 0, 390, 220);
        for (int i = 0; i < 13; i++) rect(canvas, 0xff244c40, i * 33, 205 + (i % 3) * 7, 28, 13);
        for (int i = 0; i < 7; i++) {
            rect(canvas, i % 2 == 0 ? 0xffb8dda2 : 0xff467d5c, 8 + i * 12, 48, 6, 6);
            rect(canvas, i % 2 == 0 ? 0xff467d5c : 0xffb8dda2, 312 + i * 10, 48, 5, 5);
        }
        canvas.drawBitmap(ellipse, null, new RectF(151, 131, 239, 219), paint);
        rect(canvas, 0xfffff9e9, 153, 174, 84, 45);
        rect(canvas, 0xff263b37, 152, 173, 86, 4);
        canvas.drawBitmap(center, null, new RectF(188, 168, 202, 182), paint);
        rect(canvas, 0xff213b39, 0, 0, 390, 4);
        canvas.restoreToCount(save);
    }
}
