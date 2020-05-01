/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mlkit.sample.transactor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hmf.tasks.Task;
import com.mlkit.sample.views.graphic.LocalFaceGraphic;
import com.mlkit.sample.views.graphic.CameraImageGraphic;
import com.mlkit.sample.camera.FrameMetadata;
import com.mlkit.sample.views.overlay.GraphicOverlay;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;


import java.io.IOException;
import java.util.List;

public class LocalFaceTransactor extends BaseTransactor<List<MLFace>> {
    private static final String TAG = "LocalFaceTransactor";

    private final MLFaceAnalyzer detector;
    private boolean isOpenFeatures;
    private Context mContext;

    public LocalFaceTransactor(MLFaceAnalyzerSetting options, Context context, boolean isOpenFeatures) {
        this.detector = MLAnalyzerFactory.getInstance().getFaceAnalyzer(options);
        this.isOpenFeatures = isOpenFeatures;
        this.mContext = context;
    }

    @Override
    public void stop() {
        try {
            this.detector.stop();
        } catch (IOException e) {
            Log.e(LocalFaceTransactor.TAG, "Exception thrown while trying to close face transactor: " + e.getMessage());
        }
    }

    @Override
    protected Task<List<MLFace>> detectInImage(MLFrame image) {
        return this.detector.asyncAnalyseFrame(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<MLFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.addGraphic(imageGraphic);
        }
        LocalFaceGraphic hmsMLLocalFaceGraphic = new LocalFaceGraphic(graphicOverlay, faces, mContext, this.isOpenFeatures);
        graphicOverlay.addGraphic(hmsMLLocalFaceGraphic);
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(LocalFaceTransactor.TAG, "Face detection failed: " + e.getMessage());
    }

    @Override
    public boolean isFaceDetection() {
        return true;
    }
}
