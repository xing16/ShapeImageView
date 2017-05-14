package com.xing.shapeimageviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/4/29.
 */

public class ShapeImageView extends ImageView {

    private static final int SHAPE_CIRCLE = 0;

    private static final int SHAPE_ROUND_RECTANGLE = 1;

    private ScaleType scaleType = ScaleType.CENTER_CROP;

    private int DEFAULT_SHAPE = 0;       // 默认形状

    private int DEFAULT_BORDER_COLOR = 0xffffffff;   // 默认边框颜色

    private int DEFAULT_BORDER_WIDTH = dp2Px(0);     // 默认边框宽度

    private int DEFAULT_BORDER_RADIUS = dp2Px(10);    // 默认圆角半径大小

    private int DEFAULT_PRESSED_COLOR = 0xccd0d1d1;  // 默认按下蒙版颜色

    private int shape;  // 形状

    private int borderColor;  // 边框颜色

    private int borderWidth;  // 边框宽度

    private int roundRadius;  // 圆角半径大小

    private int pressedColor;    // 按下蒙版颜色

    private Context context;

    /**
     * 显示图片bitmap
     */
    private Bitmap bitmap;

    /**
     * bitmap paint
     */
    private Paint bitmapPaint;

    /**
     * 边框border paint
     */
    private Paint borderPaint;

    /**
     * 当前是否是按下的状态
     */
    private boolean isPressed;

    private Shader bitmapShader;

    private RectF bitmapRectF;  // 图片 RectF

    private RectF borderRectF;  // 边框 RectF

    private int circleRadius;   // 圆形半径

    private int borderRaius;    // 边框半径

    private boolean mReady;

    private boolean mSetupPending;


    /**
     * 变换矩阵
     */
    private Matrix matrix;


    public ShapeImageView(Context context) {
        this(context, null);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("debug", "ShapeImageView");
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
        shape = typedArray.getInteger(R.styleable.ShapeImageView_shape, DEFAULT_SHAPE);
        borderColor = typedArray.getColor(R.styleable.ShapeImageView_borderColor, DEFAULT_BORDER_COLOR);
        borderWidth = (int) typedArray.getDimension(R.styleable.ShapeImageView_borderWidth, DEFAULT_BORDER_WIDTH);
        roundRadius = (int) typedArray.getDimension(R.styleable.ShapeImageView_borderRadius, DEFAULT_BORDER_RADIUS);
        pressedColor = typedArray.getColor(R.styleable.ShapeImageView_pressedColor, DEFAULT_PRESSED_COLOR);
        typedArray.recycle();

        init();
    }

    // 初始化paint等操作
    private void init() {
        super.setScaleType(scaleType);  //初始化时设置scaleType
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStrokeCap(Paint.Cap.ROUND);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        matrix = new Matrix();
        bitmapRectF = new RectF();
        borderRectF = new RectF();

        mReady = true;

        if (mSetupPending) {
            preDraw();
            mSetupPending = false;
        }

    }

    @Override
    public ScaleType getScaleType() {
        return scaleType;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != ScaleType.CENTER_CROP) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        preDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);   // 不需要调用父类ImageView中的onDraw方法，否则原始图片也会显示
        if (getDrawable() == null) {
            return;
        }
        if (shape == SHAPE_CIRCLE) {
            // 圆心为宽高一半
            // 绘制图片
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, circleRadius, bitmapPaint);
            // 绘制圆形边框
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, borderRaius, borderPaint);
        } else if (shape == SHAPE_ROUND_RECTANGLE) {
            // 绘制图片
            canvas.drawRoundRect(bitmapRectF, roundRadius, roundRadius, bitmapPaint);
            // 绘制圆角方形边框
            canvas.drawRoundRect(borderRectF, roundRadius, roundRadius, borderPaint);
        }
    }

    /**
     * 覆盖ImageView中的设置图片的方法
     *
     * @param resId
     */
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Log.i("debug", "setImageResource");
        if (resId != 0) {
            try {
                bitmap = getBitmapFromDrawable(context.getResources().getDrawable(resId));
            } catch (Exception e) {
            }
        }
        preDraw();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        Log.i("debug", "setImageBitmap");
        if (bm == null) {
            return;
        }
        this.bitmap = bm;
        preDraw();
    }

    /**
     * 该方法在构造方法之前执行
     *
     * @param drawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        Log.i("debug", "setImageDrawable");
        bitmap = getBitmapFromDrawable(drawable);
        preDraw();
    }


    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }


    //
    private void preDraw() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }
        if (bitmap == null) {
            return;
        }
        // 渲染器拉伸图片
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapPaint.setShader(bitmapShader);
        /***
         * 在xml中设置该控件layout_width,layout_height值包括最终显示图片宽高加上边框宽高,
         * 即边框所在矩形大小即为控件设置的大小，而真正显示出图片的大小为控件大小减去边框的宽度
         */
        int width = getWidth();
        int heigth = getHeight();
        if (borderWidth > 0) {    // 有边框
            // 边框矩形局域将边框宽度考虑在内，防止大宽度边框时，只显示一半宽度的边框
            borderRectF.set(borderWidth / 2, borderWidth / 2, width - borderWidth / 2, heigth - borderWidth / 2);
            // 边框半径
            borderRaius = (int) Math.min(borderRectF.width() / 2, borderRectF.height() / 2);
            // 本应该使用borderWidth
            bitmapRectF.set(borderWidth, borderWidth, width - borderWidth, heigth - borderWidth);
        } else {
            // 无边框，只需要bitmapRectF
            bitmapRectF.set(0, 0, width, heigth);
        }
        // 圆形图片半径.圆形半径为真正显示出图片的宽高较小值的一半
        circleRadius = (int) Math.min(bitmapRectF.width() / 2, bitmapRectF.height() / 2);
        Log.i("debug", "circleRadius = " + circleRadius);

        updateShaderMatrix();
        invalidate();
    }


    /**
     * 伸缩变换
     */
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;
        matrix.set(null);
        if (bitmap.getWidth() * bitmapRectF.height() > bitmapRectF.width() * bitmap.getHeight()) {
            scale = bitmapRectF.height() / (float) bitmap.getHeight();
            dx = (bitmapRectF.width() - bitmap.getWidth() * scale) * 0.5f;
        } else {
            scale = bitmapRectF.width() / (float) bitmap.getWidth();
            dy = (bitmapRectF.height() - bitmap.getHeight() * scale) * 0.5f;
        }
        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f) + borderWidth, (int) (dy + 0.5f) + borderWidth);
        bitmapShader.setLocalMatrix(matrix);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                if (onClickListener != null) {
                    onClickListener.onClick(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!bitmapRectF.contains(event.getX(), event.getY())) {
                    setPressed(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                break;
        }
        return true;
    }


    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (isPressed == pressed) {
            return;
        }
        isPressed = pressed;
        if (isPressed) {
            bitmapPaint.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.SRC_ATOP));
        } else {
            bitmapPaint.setColorFilter(null);
        }
        invalidate();
    }

    OnClickListener onClickListener;

    interface OnClickListener {
        void onClick(View view);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * dp转换成px
     *
     * @param dpValue
     * @return
     */
    private int dp2Px(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }


    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        if (this.borderColor == borderColor) {
            return;
        }
        this.borderColor = borderColor;
        invalidate();
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == borderWidth) {
            return;
        }
        this.borderWidth = borderWidth;
        preDraw();
    }

    public int getPressedColor() {
        return pressedColor;
    }

    public void setPressedColor(int pressedColor) {
        if (this.pressedColor == pressedColor) {
            return;
        }
        this.pressedColor = pressedColor;
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
        preDraw();
    }

    public float getRoundRadius() {
        return roundRadius;
    }

    public void setRoundRadius(int roundRadius) {
        this.roundRadius = roundRadius;
        preDraw();
    }


}
