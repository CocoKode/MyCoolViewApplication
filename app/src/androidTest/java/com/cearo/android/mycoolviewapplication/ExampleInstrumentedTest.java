package com.cearo.android.mycoolviewapplication;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.cearo.android.mycoolviewapplication.CustomViews.ThumbUpView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.cearo.android.mycoolviewapplication", appContext.getPackageName());
    }

    @Test
    public void testSplit() {
        String[] result = ThumbUpView.splitNextNum(3452345);
        assertArrayEquals(result, new String[] {"345234", "6"});
    }
}
