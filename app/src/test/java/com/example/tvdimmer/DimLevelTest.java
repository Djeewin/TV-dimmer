package com.example.tvdimmer;
import org.junit.Test;
import static org.junit.Assert.*;
public class DimLevelTest {
 @Test public void normalBrightnessIsTransparent(){assertEquals(0f,DimLevel.alpha(10),0.0001f);}
 @Test public void darkestLevelStillVisible(){assertEquals(0.95f,DimLevel.alpha(1),0.0001f);}
 @Test public void everySliderStepBrightens(){for(int i=1;i<10;i++)assertTrue(DimLevel.alpha(i)>DimLevel.alpha(i+1));}
 @Test public void clampInvalidSavedLevels(){assertEquals(DimLevel.alpha(1),DimLevel.alpha(-10),0f);assertEquals(0f,DimLevel.alpha(100),0f);}
}
