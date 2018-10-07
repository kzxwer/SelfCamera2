package com.example.android.camera2basic;

import android.media.effect.EffectFactory;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.example.android.camera2basic.ImageEffect.BLACKWHITE;
import static com.example.android.camera2basic.ImageEffect.CROSSPROCESS;
import static com.example.android.camera2basic.ImageEffect.DUOTONEBW;
import static com.example.android.camera2basic.ImageEffect.DUOTONEPY;
import static com.example.android.camera2basic.ImageEffect.FILLIGHT;
import static com.example.android.camera2basic.ImageEffect.LOMOISH;
import static com.example.android.camera2basic.ImageEffect.NEGATIVE;
import static com.example.android.camera2basic.ImageEffect.NONE;
import static com.example.android.camera2basic.ImageEffect.SEPIA;

/**
 * Created by Pankaj Soni <pankajsoni@softwarejoint.com> on 15/03/18.
 * Copyright (c) 2018 Software Joint. All rights reserved.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({NONE, SEPIA, NEGATIVE, CROSSPROCESS,
        BLACKWHITE, LOMOISH, DUOTONEPY, DUOTONEBW, FILLIGHT})
//POSTERIZE, DOCUMENTARY, GRAYSCALE,VIGNETTE
public @interface ImageEffect {
    String NONE = "none";
    String CROSSPROCESS = EffectFactory.EFFECT_CROSSPROCESS;
    String SEPIA = EffectFactory.EFFECT_SEPIA;
   // String GRAYSCALE = EffectFactory.EFFECT_GRAYSCALE;
  //  String POSTERIZE = EffectFactory.EFFECT_POSTERIZE;
    String NEGATIVE = EffectFactory.EFFECT_NEGATIVE;
    String BLACKWHITE = EffectFactory.EFFECT_BLACKWHITE;
    //String DOCUMENTARY = EffectFactory.EFFECT_DOCUMENTARY;
    String LOMOISH = EffectFactory.EFFECT_LOMOISH;
   // String VIGNETTE = EffectFactory.EFFECT_VIGNETTE;
    String DUOTONEPY = EffectFactory.EFFECT_DUOTONE;
    String DUOTONEBW = EffectFactory.EFFECT_DUOTONE + "bw";
    String FILLIGHT = EffectFactory.EFFECT_FILLLIGHT;

}
