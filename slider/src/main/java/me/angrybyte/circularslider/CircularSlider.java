
package me.angrybyte.circularslider;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

public class CircularSlider extends View {

    /**
     * Listener interface used to detect when slider moves around.
     */
    @SuppressWarnings("WeakerAccess")
    public interface OnSliderMovedListener {

        /**
         * This method is invoked when slider moves, providing position of the slider thumb.
         *
         * @param pos Value between 0 and 1 representing the current angle.<br>
         *            {@code pos = (Angle - StartingAngle) / (2 * Pi)}
         */
        void onSliderMoved(double pos);
    }

    private int mThumbX;
    private int mThumbY;

    private int mCircleCenterX;
    private int mCircleCenterY;
    private int mCircleRadius;

    private Drawable mThumbImage;
    private int mPadding;
    private int mThumbSize;
    private int mThumbColor;
    private int mBorderColor;
    private int[] mBorderGradientColors;
    private int mBorderThickness;
    private double mStartAngle;
    private double mAngle = mStartAngle;
    private boolean mIsThumbSelected = false;

    private Paint mPaint = new Paint();
    private SweepGradient mGradientShader;
    private OnSliderMovedListener mListener;

    public CircularSlider(Context context) {
        this(context, null);
    }

    public CircularSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    // common initializer method
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularSlider, defStyleAttr, 0);

        // read all available attributes
        float startAngle = a.getFloat(R.styleable.CircularSlider_start_angle, (float) Math.PI / 2);
        float angle = a.getFloat(R.styleable.CircularSlider_angle, (float) Math.PI / 2);
        int thumbSize = a.getDimensionPixelSize(R.styleable.CircularSlider_thumb_size, 50);
        int thumbColor = a.getColor(R.styleable.CircularSlider_thumb_color, Color.GRAY);
        int borderThickness = a.getDimensionPixelSize(R.styleable.CircularSlider_border_thickness, 20);
        int borderColor = a.getColor(R.styleable.CircularSlider_border_color, Color.RED);
        String borderGradientColors = a.getString(R.styleable.CircularSlider_border_gradient_colors);
        Drawable thumbImage = a.getDrawable(R.styleable.CircularSlider_thumb_image);

        // save those to fields (really, do we need setters here..?)
        setStartAngle(startAngle);
        setAngle(angle);
        setBorderThickness(borderThickness);
        setBorderColor(borderColor);
        if (borderGradientColors != null) {
            setBorderGradientColors(borderGradientColors.split(";"));
        }
        setThumbSize(thumbSize);
        setThumbImage(thumbImage);
        setThumbColor(thumbColor);

        // assign padding - check for version because of RTL layout compatibility
        int padding;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int all = getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop() + getPaddingEnd() + getPaddingStart();
            padding = all / 6;
        } else {
            padding = (getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop()) / 4;
        }
        setPadding(padding);

        a.recycle();
    }

    /* ***** Setters ***** */
    public void setStartAngle(double startAngle) {
        mStartAngle = startAngle;
    }

    public void setAngle(double angle) {
        mAngle = angle;
    }

    public void setThumbSize(int thumbSize) {
        mThumbSize = thumbSize;
    }

    public void setBorderThickness(int circleBorderThickness) {
        mBorderThickness = circleBorderThickness;
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
    }

    public void setBorderGradientColors(String[] colors) {
        mBorderGradientColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mBorderGradientColors[i] = Color.parseColor(colors[i]);
        }
    }

    @SuppressWarnings("unused")
    public void setBorderGradientColors(int[] colors) {
        if (colors == null) {
            mBorderGradientColors = null;
            mGradientShader = null;
        } else {
            mBorderGradientColors = Arrays.copyOf(colors, colors.length);
            mGradientShader = new SweepGradient(mCircleRadius, mCircleRadius, mBorderGradientColors, null);
        }
    }

    public void setThumbImage(Drawable drawable) {
        mThumbImage = drawable;
    }

    public void setThumbColor(int color) {
        mThumbColor = color;
    }

    public void setPadding(int padding) {
        mPadding = padding;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // use smaller dimension for calculations (depends on parent size)
        int smallerDim = w > h ? h : w;

        // find circle's rectangle points
        int largestCenteredSquareLeft = (w - smallerDim) / 2;
        int largestCenteredSquareTop = (h - smallerDim) / 2;
        int largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim;
        int largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim;

        // save circle coordinates and radius in fields
        mCircleCenterX = largestCenteredSquareRight / 2 + (w - largestCenteredSquareRight) / 2;
        mCircleCenterY = largestCenteredSquareBottom / 2 + (h - largestCenteredSquareBottom) / 2;
        mCircleRadius = smallerDim / 2 - mBorderThickness / 2 - mPadding;

        if (mBorderGradientColors != null) {
            mGradientShader = new SweepGradient(mCircleRadius, mCircleRadius, mBorderGradientColors, null);
        }

        // works well for now, should we call something else here?
        super.onSizeChanged(w, h, oldW, oldH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // outer circle (ring)
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderThickness);
        mPaint.setAntiAlias(true);
        if (mGradientShader != null) {
            mPaint.setShader(mGradientShader);
        }
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mPaint);

        // find thumb position
        mThumbX = (int) (mCircleCenterX + mCircleRadius * Math.cos(mAngle));
        mThumbY = (int) (mCircleCenterY - mCircleRadius * Math.sin(mAngle));

        if (mThumbImage != null) {
            // draw png
            mThumbImage.setBounds(mThumbX - mThumbSize / 2, mThumbY - mThumbSize / 2, mThumbX + mThumbSize / 2, mThumbY + mThumbSize / 2);
            mThumbImage.draw(canvas);
        } else {
            // draw colored circle
            mPaint.setColor(mThumbColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mThumbX, mThumbY, mThumbSize, mPaint);
        }
    }

    /**
     * Invoked when slider starts moving or is currently moving. This method calculates and sets position and angle of the thumb.
     *
     * @param touchX Where is the touch identifier now on X axis
     * @param touchY Where is the touch identifier now on Y axis
     */
    private void updateSliderState(int touchX, int touchY) {
        int distanceX = touchX - mCircleCenterX;
        int distanceY = mCircleCenterY - touchY;
        //noinspection SuspiciousNameCombination
        double c = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        mAngle = Math.acos(distanceX / c);
        if (distanceY < 0) {
            mAngle = -mAngle;
        }

        if (mListener != null) {
            // notify slider moved listener of the new position which should be in [0..1] range
            mListener.onSliderMoved((mAngle - mStartAngle) / (2 * Math.PI));
        }
    }

    /**
     * Position setter. This method should be used to manually position the slider thumb.<br>
     * Note that counterclockwise {@link #mStartAngle} is used to determine the initial thumb position.
     *
     * @param pos Value between 0 and 1 used to calculate the angle. {@code Angle = StartingAngle + pos * 2 * Pi}<br>
     *            Note that angle will not be updated if the position parameter is not in the valid range [0..1]
     */
    @SuppressWarnings("unused")
    public void setPosition(double pos) {
        if (pos >= 0 && pos <= 1) {
            mAngle = mStartAngle + pos * 2 * Math.PI;
        }
    }

    /**
     * Saves a new slider moved listener. Set {@link CircularSlider.OnSliderMovedListener} to {@code null} to remove it.
     *
     * @param listener Instance of the slider moved listener, or null when removing it
     */
    @SuppressWarnings("unused")
    public void setOnSliderMovedListener(OnSliderMovedListener listener) {
        mListener = listener;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // start moving the thumb (this is the first touch)
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize && y > mThumbY - mThumbSize) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mIsThumbSelected = true;
                    updateSliderState(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // still moving the thumb (this is not the first touch)
                if (mIsThumbSelected) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    updateSliderState(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                // finished moving (this is the last touch)
                getParent().requestDisallowInterceptTouchEvent(false);
                mIsThumbSelected = false;
                break;
            }
        }

        // redraw the whole component
        invalidate();
        return true;
    }

}
