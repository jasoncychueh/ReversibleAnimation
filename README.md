[![GitHub license](https://img.shields.io/github/license/dcendents/android-maven-gradle-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
# ReversibleAnimationDrawable
> Have you ever created two AnimationDrawable for reverse animation or encountered OOM issue when the sequence animation contains too many frames. This library aims to solve the two issues.


#### Reverse animation
> [AnimationDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimationDrawable.html) does not support reverse animation. [ReverseAnimationDrawable](ReversibleAnimationDrawable/library/src/main/java/com/foureach/graphics/drawable/ReversibleAnimationDrawable.java) extends AnimationDrawable and provides reversibility.

###### Example:

```xml
<!-- res/drawable/anim.png -->
<?xml version="1.0" encoding="utf-8"?>
<animation-list xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:custom="http://schemas.android.com/apk/res-auto"
        android:id="@+id/anim" android:oneshot="false" custom:reverse="true">
    <item android:drawable="@drawable/frame_001" android:duration="33" />
    <item android:drawable="@drawable/frame_002" android:duration="33" />
    <item android:drawable="@drawable/frame_003" android:duration="33" />
    <item android:drawable="@drawable/frame_004" android:duration="33" />
    <item android:drawable="@drawable/frame_005" android:duration="33" />
    <item android:drawable="@drawable/frame_006" android:duration="33" />
    <!-- skipped more frames -->
</animation-list>
```

```java
private ImageView mAnimImage;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAnimImage = (ImageView) findViewById(R.id.animImage);

    Drawable d = getResources().getDrawable(R.drawable.anim);
    ReversibleAnimationDrawable anim = new ReversibleAnimationDrawable(d);
    anim.setReverse(true);
    anim.setOneShot(false);

    mAnimImage.setImageDrawable(anim);
    anim.start();
}
```


#### Lazy load frames
> Lazy loading images from resources is easy. [LazyLoadingAnimationDrawable](https://github.com/JasonCYChueh/ReversibleAnimationDrawable/blob/master/library/src/main/java/com/foureach/graphics/drawable/LazyLoadingAnimationDrawable.java) is a subclass of ReversibleAnimationDrawable. You can use its static method `loadFromResource(Resources, int)` to consturct an instance or add frame by frame in code.


###### Example:

```java
private ImageView mAnimImage;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAnimImage = (ImageView) findViewById(R.id.animImage);

    ReversibleAnimationDrawable anim = LazyLoadingAnimationDrawable
            .loadFromResource(getResources(), R.drawable.anim);

    mAnimImage.setImageDrawable(anim);
    anim.start();
}
```


# License

```
Copyright 2016 ChihYu Chueh (Jason Chueh)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
