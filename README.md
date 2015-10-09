# A simple circular slider for Android

What is this?
-------------

This repository contains source code for the circular slider view, a custom UI control for the Android OS.
It works similarly to the regular slider control (SeekBar), just goes around in a circular fashion - simple enough :)

_Note_ that the thumb scroller (the thing you drag around) can be either a solid-color circle or a custom drawable.

Requirements
------------
- Android 2.3 or later SDK level
- Android Studio (to compile it and use)

What does it look like?
----------------------

![Circular Slider Image](http://i.imgur.com/au9sYb5.png)

Sample usage
------------

    <?xml version="1.0" encoding="utf-8"?>
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:circular="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#303030">

        <me.angrybyte.circularslider.CircularSlider
            android:id="@+id/circular"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="30dp"
            circular:angle="3.14"
            circular:border_color="#505090"
            circular:border_thickness="14dp"
            circular:thumb_color="#30AEFF"
            circular:thumb_size="24dp" />

    </RelativeLayout>

Explanation of attributes
-------------------------

- *Angle*: A Math.PI-based value of the angle
- *Border Color*: The outline of the circle
- *Border Thickness*: How thick should the circle outline be
- *Thumb Color*: Color of the thumb scroller (the thing you drag around)
- *Thumb Image*: A drawable to use instead of a solid color for the thumb scroller
- *Thumb Size*: How big should the thumb scroller be

_Note_ that _thumb color_ and _thumb image_ are mutually exclusive, you can't use both.
