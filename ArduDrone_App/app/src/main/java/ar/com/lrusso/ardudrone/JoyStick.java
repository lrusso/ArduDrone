package ar.com.lrusso.ardudrone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class JoyStick extends View implements GestureDetector.OnGestureListener {

    public static final int DIRECTION_CENTER = -1;
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_UP_RIGHT = 1;
    public static final int DIRECTION_UP_LEFT = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_RIGHT = 4;
    public static final int DIRECTION_DOWN_RIGHT = 5;
    public static final int DIRECTION_DOWN_LEFT = 6;
    public static final int DIRECTION_DOWN = 7;

    public static final int TYPE_8_AXIS = 11;
    public static final int TYPE_4_AXIS = 22;
    public static final int TYPE_2_AXIS_LEFT_RIGHT = 33;
    public static final int TYPE_2_AXIS_UP_DOWN = 44;

    private JoyStickListener listener;
    private Paint paint;
    private RectF temp;
    private GestureDetector gestureDetector;
    private int direction = DIRECTION_CENTER;
    private int type = TYPE_8_AXIS;
    private float centerX;
    private float centerY;
    private float posX;
    private float posY;
    private float radius;
    private float buttonRadius;
    private double power = 0;
    private double angle = 0;

    //Background Color
    private int padColor;

    //Stick Color
    private int buttonColor;

    //Keeps joystick in last position
    private boolean stayPut;

    //Button Size percentage of the minimum(width, height)
    private int percentage = 40;

    //Background Bitmap
    private Bitmap padBGBitmap = null;

    //Button Bitmap
    private Bitmap buttonBitmap = null;

    public interface JoyStickListener {
        void onMove(JoyStick joyStick, double angle, double power, int direction);

    }

    public JoyStick(Context context) {
        this(context, null);
    }

    public JoyStick(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        temp = new RectF();

        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setIsLongpressEnabled(false);
        
        padColor = Color.BLACK;
        buttonColor = Color.RED;

        stayPut = false;
        if (percentage > 50) percentage = 50;
        if (percentage < 25) percentage = 25;
    	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);
        centerX = width / 2;
        centerY = height / 2;
        float min = Math.min(width, height);
        posX = centerX;
        posY = centerY;
        buttonRadius = (min / 2f * (percentage / 100f));
        radius = (min / 2f * ((100f - percentage) / 100f));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (canvas == null) return;
        if (padBGBitmap == null) {
            
        	//paint.setColor(Color.parseColor("#79a6d2"));
        	paint.setColor(Color.parseColor("#3d556d"));
        	paint.setStyle(Paint.Style.STROKE);
        	paint.setStrokeWidth(8);            
        	canvas.drawCircle(centerX, centerY, radius, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(padColor);
            canvas.drawCircle(centerX, centerY, radius, paint);
        } else {
            temp.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawBitmap(padBGBitmap, null, temp, paint);
        }
        if (buttonBitmap == null) {
            paint.setColor(buttonColor);
            canvas.drawCircle(posX, posY, buttonRadius, paint);
        } else {
            temp.set(posX - buttonRadius, posY - buttonRadius, posX + buttonRadius, posY + buttonRadius);
            canvas.drawBitmap(buttonBitmap, null, temp, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (this.isEnabled()==true)
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        posX = event.getX();
                        posY = event.getY();

                        if (type == TYPE_2_AXIS_LEFT_RIGHT) {
                            posY = centerY;
                        } else if (type == TYPE_2_AXIS_UP_DOWN) {
                            posX = centerX;
                        } else if (type == TYPE_4_AXIS) {
                            if (Math.abs(posX - centerX) > Math.abs(posY - centerY)) posY = centerY;
                            else posX = centerX;
                        }

                        float abs = (float) Math.sqrt((posX - centerX) * (posX - centerX)
                                + (posY - centerY) * (posY - centerY));
                        if (abs > radius) {
                            posX = ((posX - centerX) * radius / abs + centerX);
                            posY = ((posY - centerY) * radius / abs + centerY);
                        }

                        angle = Math.atan2(centerY - posY, centerX - posX);

                        power = (100 * Math.sqrt((posX - centerX)
                                * (posX - centerX) + (posY - centerY)
                                * (posY - centerY)) / radius);

                        direction = calculateDirection(Math.toDegrees(angle));

                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!stayPut) {
                            posX = centerX;
                            posY = centerY;
                            direction = DIRECTION_CENTER;
                            angle = 0;
                            power = 0;
                            invalidate();
                        }
                        break;
                }

                if (listener != null) {
                    listener.onMove(this, angle, power, direction);
                }
            }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
 
    private static int calculateDirection(double degrees) {
        if ((degrees >= 0 && degrees < 22.5) || (degrees < 0 && degrees > -22.5)) {
            return DIRECTION_LEFT;
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return DIRECTION_UP_LEFT;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return DIRECTION_UP;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return DIRECTION_UP_RIGHT;
        } else if ((degrees >= 157.5 && degrees <= 180) || (degrees >= -180 && degrees < -157.5)) {
            return DIRECTION_RIGHT;
        } else if (degrees >= -157.5 && degrees < -112.5) {
            return DIRECTION_DOWN_RIGHT;
        } else if (degrees >= -112.5 && degrees < -67.5) {
            return DIRECTION_DOWN;
        } else if (degrees >= -67.5 && degrees < -22.5) {
            return DIRECTION_DOWN_LEFT;
        } else {
            return DIRECTION_CENTER;
        }
    }

    public void setListener(JoyStickListener listener) {
        this.listener = listener;
    }

    public double getPower() {
        return power;
    }

    public double getAngle() {
        return angle;
    }

    public double getAngleDegrees() {
        return Math.toDegrees(angle);
    }

    public int getDirection() {
        return direction;
    }

    public int getType() {
        return type;
    }

    //Customization ----------------------------------------------------------------

    public void setPadColor(int padColor) {
        this.padColor = padColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    //size of button is a percentage of the minimum(width, height)
    //percentage must be between 25 - 50
    public void setButtonRadiusScale(int scale) {
        percentage = scale;
        if (percentage > 50) percentage = 50;
        if (percentage < 25) percentage = 25;
    }

    public void enableStayPut(boolean enable) {
        this.stayPut = enable;
    }

    public void setPadBackground(int resId) {
        this.padBGBitmap = BitmapFactory.decodeResource(getResources(), resId);
    }

    public void setPadBackground(Bitmap bitmap) {
        this.padBGBitmap = bitmap;
    }

    public void setButtonDrawable(int resId) {
        this.buttonBitmap = BitmapFactory.decodeResource(getResources(), resId);
    }

    public void setButtonDrawable(Bitmap bitmap) {
        this.buttonBitmap = bitmap;
    }

    public void setType(int type) {
        this.type = type;
    }
}